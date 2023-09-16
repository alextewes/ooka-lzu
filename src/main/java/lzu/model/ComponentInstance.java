package lzu.model;

import lzu.service.ComponentThread;

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

    public String getName() {
        return name;
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

    public void setComponentThread(ComponentThread componentThread) {
        this.componentThread = componentThread;
    }

    public ComponentThread getComponentThread() {
        return componentThread;
    }

    public Path getJarFilePath() {
        return jarFilePath;
    }
}

