package lzu;

import java.util.List;

public class ComponentLoaderState {
    private List<ComponentInstance> componentInstances;

    public ComponentLoaderState(List<ComponentInstance> componentInstances) {
        this.componentInstances = componentInstances;
    }

    public List<ComponentInstance> getComponentInstances() {
        return componentInstances;
    }

    public void setComponentInstances(List<ComponentInstance> componentInstances) {
        this.componentInstances = componentInstances;
    }
}

