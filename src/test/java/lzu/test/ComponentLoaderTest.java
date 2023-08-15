
package lzu.test;

import lzu.service.ComponentLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComponentLoaderTest {

    private ComponentLoader componentLoader;

    @BeforeEach
    void setUp() {
        componentLoader = new ComponentLoader("test-prefix");
    }

    @Test
    void deployComponentSuccess() {
        Path mockJarPath = Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\ooka-lzu\\componentA\\target\\componentA-1.0-SNAPSHOT.jar");
        List<Class<?>> loadedClasses = componentLoader.deployComponent(mockJarPath, "mock-component");
        assertFalse(loadedClasses.isEmpty(), "Loaded classes list should not be empty");
    }

    @Test
    void startAndStopComponentById() {
        Path mockJarPath = Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\ooka-lzu\\componentA\\target\\componentA-1.0-SNAPSHOT.jar");
        List<Class<?>> loadedClasses = componentLoader.deployComponent(mockJarPath, "mock-component");
        assertFalse(loadedClasses.isEmpty(), "Loaded classes list should not be empty");

        // Assuming you have a component with the ID "test-prefix-1" in the loaded classes
        componentLoader.startComponentById("test-prefix-1");
        componentLoader.stopComponentById("test-prefix-1");
    }

    @Test
    void removeComponentById() {
        Path mockJarPath = Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\ooka-lzu\\componentA\\target\\componentA-1.0-SNAPSHOT.jar");
        List<Class<?>> loadedClasses = componentLoader.deployComponent(mockJarPath, "mock-component");
        assertFalse(loadedClasses.isEmpty(), "Loaded classes list should not be empty");

        // Assuming you have a component with the ID "test-prefix-1" in the loaded classes
        componentLoader.removeComponentById("test-prefix-1");
        assertFalse(componentLoader.hasComponent("test-prefix-1"), "Component should be removed");
    }

    @Test
    void getAllComponentStatuses() {
        Path mockJarPath = Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\ooka-lzu\\componentA\\target\\componentA-1.0-SNAPSHOT.jar");
        List<Class<?>> loadedClasses = componentLoader.deployComponent(mockJarPath, "mock-component");
        assertFalse(loadedClasses.isEmpty(), "Loaded classes list should not be empty");

        List<Map<String, Object>> componentStatuses = componentLoader.getAllComponentStatuses();
        assertFalse(componentStatuses.isEmpty(), "Component statuses list should not be empty");
    }

}
