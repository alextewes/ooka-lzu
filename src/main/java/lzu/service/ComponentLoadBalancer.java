package lzu.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import lzu.model.ComponentInstance;
import lzu.service.ComponentLoader;

public class ComponentLoadBalancer {

    private final ComponentLoader componentLoader;
    private final Map<String, Queue<String>> componentQueues;

    public ComponentLoadBalancer(ComponentLoader componentLoader) {
        this.componentLoader = componentLoader;
        this.componentQueues = new ConcurrentHashMap<>();
    }

    public void deployAndBalanceComponent(Path jarFilePath, String name, int instanceCount) throws IOException, ClassNotFoundException {
        Queue<String> instanceQueue = new LinkedBlockingQueue<>();
        for (int i = 0; i < instanceCount; i++) {
            componentLoader.deployComponent(jarFilePath, name);
            String lastInsertedID = componentLoader.getLastInsertedComponentID(name);
            if (lastInsertedID != null) {
                instanceQueue.add(lastInsertedID);
            }
        }
        componentQueues.put(name, instanceQueue);
    }

    public String getNextComponentInstance(String componentName) {
        Queue<String> queue = componentQueues.get(componentName);
        if (queue != null && !queue.isEmpty()) {
            String componentId = queue.poll();
            queue.add(componentId);
            return componentId;
        }
        return null;
    }

    public ComponentLoader getComponentLoader() {
        return componentLoader;
    }

    public void reloadQueues() {
        componentQueues.clear();
        for (ComponentInstance instance : componentLoader.getComponents().values()) {
            componentQueues.computeIfAbsent(instance.getName(), k -> new LinkedBlockingQueue<>()).add(instance.getID());
        }
    }
}