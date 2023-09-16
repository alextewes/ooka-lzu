package lzu.service;

import lzu.model.ComponentInstance;
import lzu.model.ComponentState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ComponentThread extends Thread {
    private ComponentInstance componentInstance;
    private Method startMethod;
    private Method stopMethod;
    private Object startMethodInstance;
    private Object stopMethodInstance;

    public ComponentThread(ComponentInstance componentInstance, Method startMethod, Method stopMethod, Object startMethodInstance, Object stopMethodInstance) {
        this.componentInstance = componentInstance;
        this.startMethod = startMethod;
        this.stopMethod = stopMethod;
        this.startMethodInstance = startMethodInstance;
        this.stopMethodInstance = stopMethodInstance;
    }

    @Override
    public void run() {
        try {
            componentInstance.setState(ComponentState.RUNNING);
            startMethod.invoke(startMethodInstance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
