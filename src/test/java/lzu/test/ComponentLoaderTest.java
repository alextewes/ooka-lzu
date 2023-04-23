package lzu.test;

import lzu.ComponentLoader;
import lzu.ComponentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComponentLoaderTest {
    private ComponentLoader componentLoader;
    private final Path firstComponentPath = Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\ooka-lzu\\componentA\\target\\componentA-1.0-SNAPSHOT.jar");
    private final Path secondComponentPath = Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\ooka-lzu\\componentB\\target\\componentB-1.0-SNAPSHOT.jar");

    @BeforeEach
    void setUp() {
        componentLoader = new ComponentLoader();
    }

    @Test
    void testDeployComponent() {
        componentLoader.deployComponent(firstComponentPath, "TestComponent");

        List<Map<String, Object>> statuses = componentLoader.getAllComponentStatuses();

        assertEquals(1, statuses.size());
        assertEquals("TestComponent", statuses.get(0).get("name"));
        assertEquals(ComponentState.INITIALIZED, statuses.get(0).get("state"));
    }

    @Test
    void testDeployMultipleComponents() {
        componentLoader.deployComponent(firstComponentPath, "FirstComponent");
        componentLoader.deployComponent(secondComponentPath, "SecondComponent");

        List<Map<String, Object>> statuses = componentLoader.getAllComponentStatuses();

        assertEquals(2, statuses.size());
        assertEquals("FirstComponent", statuses.get(0).get("name"));
        assertEquals(ComponentState.INITIALIZED, statuses.get(0).get("state"));
        assertEquals("SecondComponent", statuses.get(1).get("name"));
        assertEquals(ComponentState.INITIALIZED, statuses.get(1).get("state"));
    }

    @Test
    void testDeploySameComponentTwice() {
        componentLoader.deployComponent(firstComponentPath, "TestComponentA");
        componentLoader.deployComponent(firstComponentPath, "TestComponentB");

        List<Map<String, Object>> statuses = componentLoader.getAllComponentStatuses();

        assertEquals(2, statuses.size());
        assertEquals("TestComponentA", statuses.get(0).get("name"));
        assertEquals("TestComponentB", statuses.get(1).get("name"));

        assertEquals(ComponentState.INITIALIZED, statuses.get(0).get("state"));
        assertEquals(ComponentState.INITIALIZED, statuses.get(1).get("state"));
    }

    @Test
    void testDeployComponentWithInvalidPath() {
        Path jarFilePath = Paths.get("path/to/nonexistent/file.jar");
        componentLoader.deployComponent(jarFilePath, "NonExistentComponent");
        assertEquals(0, componentLoader.getAllComponentStatuses().size());
    }

    @Test
    void testStartAndStopComponentById() throws InterruptedException {
        componentLoader.deployComponent(firstComponentPath, "TestComponent");
        List<Map<String, Object>> statuses = componentLoader.getAllComponentStatuses();
        int componentId = (int) statuses.get(0).get("ID");

        componentLoader.startComponentById(componentId);
        Thread.sleep(200);
        statuses = componentLoader.getAllComponentStatuses();
        assertEquals(ComponentState.RUNNING, statuses.get(0).get("state"));

        componentLoader.stopComponentById(componentId);
        statuses = componentLoader.getAllComponentStatuses();
        assertEquals(ComponentState.STOPPED, statuses.get(0).get("state"));
    }

    @Test
    void testRemoveComponentById() throws InterruptedException {
        componentLoader.deployComponent(firstComponentPath, "TestComponent");
        List<Map<String, Object>> statuses = componentLoader.getAllComponentStatuses();
        int componentId = (int) statuses.get(0).get("ID");

        componentLoader.startComponentById(componentId);
        Thread.sleep(200);
        componentLoader.stopComponentById(componentId);

        componentLoader.removeComponentById(componentId);
        statuses = componentLoader.getAllComponentStatuses();
        assertTrue(statuses.isEmpty());
    }

    @Test
    void testClassIsolation() {
        // Lade vom selben componentLoader dieselbe Klasse einer Komponente
        Class<?> lc1 = componentLoader.deployComponent(firstComponentPath, "Component1").get(0);
        Class<?> lc2 = componentLoader.deployComponent(firstComponentPath, "Component2").get(0);

        // Instanzen sollten verschieden sein, da sie von zwei verschiedenen ClassLoader-Instanzen geladen werden
        assertFalse(lc1.isInstance(lc2));
    }
}
