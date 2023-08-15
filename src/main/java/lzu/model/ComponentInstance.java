package lzu.model;

import lzu.utils.ComponentThread;

import java.lang.reflect.Method;
import java.nio.file.Path;

public class ComponentInstance {
    private String ID;
    private String name;
    private ComponentState state;
    private Class<?> startClass;
    private ComponentThread componentThread;
    private Path jarFilePath;

    public ComponentInstance(String ID, String name, Class<?> startClass, Method startMethod, Method stopMethod, Object startMethodInstance, Object stopMethodInstance, Path jarFilePath) {
        this.ID = ID;
        this.name = name;
        this.startClass = startClass;
        this.state = ComponentState.INITIALIZED;
        this.componentThread = new ComponentThread(this, startMethod, stopMethod, startMethodInstance, stopMethodInstance);
        this.jarFilePath = jarFilePath;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
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

    public Path getJarFilePath() {
        return jarFilePath;
    }

    public void setJarFilePath(Path jarFilePath) {
        this.jarFilePath = jarFilePath;
    }
}

