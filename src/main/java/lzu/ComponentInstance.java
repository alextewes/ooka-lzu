package lzu;

import java.lang.reflect.Method;

public class ComponentInstance {
    private int ID;
    private String name;
    private ComponentState state;
    private Class<?> startClass;
    private ComponentThread componentThread;

    public ComponentInstance(int ID, String name, Class<?> startClass, Method startMethod, Method stopMethod, Object startMethodInstance, Object stopMethodInstance) {
        this.ID = ID;
        this.name = name;
        this.startClass = startClass;
        this.state = ComponentState.INITIALIZED;
        this.componentThread = new ComponentThread(this, startMethod, stopMethod, startMethodInstance, stopMethodInstance);
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ComponentState getState() {
        return state;
    }

    public void setState(ComponentState state) {
        this.state = state;
    }

    public Class<?> getStartClass() {
        return startClass;
    }

    public void setStartClass(Class<?> startClass) {
        this.startClass = startClass;
    }

    public ComponentThread getComponentThread() {
        return componentThread;
    }

    public void setComponentThread(ComponentThread componentThread) {
        this.componentThread = componentThread;
    }
}

