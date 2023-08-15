package lzu.utils;

import lzu.service.ComponentLoader;

import java.util.ArrayList;
import java.util.List;

public class LoadBalancer {
    private int currentLoader = 0;
    private final List<ComponentLoader> componentLoaders = new ArrayList<>();

    public LoadBalancer(int numLoaders) {
        for (int i = 0; i < numLoaders; i++) {
            componentLoaders.add(new ComponentLoader("loader" + i));
        }
    }

    public ComponentLoader getNextLoader() {
        ComponentLoader loader = componentLoaders.get(currentLoader);
        currentLoader = (currentLoader + 1) % componentLoaders.size();
        return loader;
    }

    public List<ComponentLoader> getAllLoaders() {
        return componentLoaders;
    }
}

