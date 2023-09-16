
package lzu.test;

import lzu.service.ComponentLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComponentLoaderTest {

    private ComponentLoader componentLoader;

    @BeforeEach
    void setUp() {
        componentLoader = new ComponentLoader();
    }

    @Test
    void deployComponentSuccess() throws IOException, ClassNotFoundException {
        Path mockJarPath = Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\ooka-lzu\\componentA\\target\\componentA-1.0-SNAPSHOT.jar");
        List<Class<?>> loadedClasses = componentLoader.deployComponent(mockJarPath, "mock-component");
        assertFalse(loadedClasses.isEmpty(), "Loaded classes list should not be empty");
    }

    @Test
    void testClassIsolation() throws IOException, ClassNotFoundException {
        Path mockJarPath = Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\ooka-lzu\\componentA\\target\\componentA-1.0-SNAPSHOT.jar");
        Class<?> loadedClass1 = componentLoader.deployComponent(mockJarPath, "mock-component-1").get(0);
        Class<?> loadedClass2 = componentLoader.deployComponent(mockJarPath, "mock-component-2").get(0);
        assertFalse(loadedClass1.isInstance(loadedClass2), "Instances of classes should be different.");
    }

    @Test
    void startAndStopComponentById() throws Exception {
        Path mockJarPath = Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\ooka-lzu\\componentA\\target\\componentA-1.0-SNAPSHOT.jar");
        List<Class<?>> loadedClasses = componentLoader.deployComponent(mockJarPath, "mock-component");
        assertFalse(loadedClasses.isEmpty(), "Loaded classes list should not be empty");
        componentLoader.startComponentById("1");
        componentLoader.stopComponentById("1");
    }

    @Test
    void removeComponentById() throws Exception {
        Path mockJarPath = Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\ooka-lzu\\componentA\\target\\componentA-1.0-SNAPSHOT.jar");
        List<Class<?>> loadedClasses = componentLoader.deployComponent(mockJarPath, "mock-component");
        assertFalse(loadedClasses.isEmpty(), "Loaded classes list should not be empty");

        componentLoader.removeComponentById("1");
        assertFalse(componentLoader.getComponents().containsKey("1"), "Component should be removed");
    }

    @Test
    void getAllComponentStatuses() throws IOException, ClassNotFoundException {
        Path mockJarPath = Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\ooka-lzu\\componentA\\target\\componentA-1.0-SNAPSHOT.jar");
        List<Class<?>> loadedClasses = componentLoader.deployComponent(mockJarPath, "mock-component");
        assertFalse(loadedClasses.isEmpty(), "Loaded classes list should not be empty");

        List<Map<String, Object>> componentStatuses = componentLoader.getAllComponentStatuses();
        assertFalse(componentStatuses.isEmpty(), "Component statuses list should not be empty");
    }

}
