package lzu;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        ComponentLoader componentLoader = new ComponentLoader();

        // Deploy two components from JAR files
        componentLoader.deployComponent(Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\jar-test\\target\\jar-test-1.0-SNAPSHOT.jar"), "First Component");
        componentLoader.deployComponent(Paths.get("C:\\Users\\Alex\\IdeaProjects\\H-BRS\\jar-test-B\\target\\jar-test-B-1.0-SNAPSHOT.jar"), "Second Component");

        List<Map<String, Object>> componentStatusList = componentLoader.getAllComponentStatuses();
        for (Map<String, Object> componentStatus : componentStatusList) {
            System.out.println("ID: " + componentStatus.get("ID") + ", Name: " + componentStatus.get("name") + ", State: " + componentStatus.get("state"));
        }

        // Start all components concurrently
        componentLoader.startAllComponents();

        // Add a delay to let the components run for a while
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        componentStatusList = componentLoader.getAllComponentStatuses();
        for (Map<String, Object> componentStatus : componentStatusList) {
            System.out.println("ID: " + componentStatus.get("ID") + ", Name: " + componentStatus.get("name") + ", State: " + componentStatus.get("state"));
        }

        // Stop all components concurrently
        componentLoader.stopAllComponents();

        // Retrieve and print the statuses of all components
        componentStatusList = componentLoader.getAllComponentStatuses();
        for (Map<String, Object> componentStatus : componentStatusList) {
            System.out.println("ID: " + componentStatus.get("ID") + ", Name: " + componentStatus.get("name") + ", State: " + componentStatus.get("state"));
        }
    }
}


