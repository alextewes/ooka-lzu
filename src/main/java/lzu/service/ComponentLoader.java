package lzu.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import lzu.utils.Component;
import logger.Inject;
import logger.Logger;
import logger.LoggerFactory;
import lzu.model.ComponentState;
import lzu.utils.ComponentThread;
import lzu.model.ComponentInstance;
import lzu.utils.MessageQueue;
import lzu.utils.UniqueIdGenerator;
import org.json.JSONArray;
import org.json.JSONObject;

public class ComponentLoader {

    private Method startMethod;
    private Method stopMethod;
    private Object startMethodInstance;
    private Object stopMethodInstance;
    private Map<String, ComponentInstance> components;
    private String prefix;
    private final MessageQueue messageBus = MessageQueue.getInstance();

    public ComponentLoader() {

    }

    public ComponentLoader(String prefix) {
        components = new ConcurrentHashMap<>();
        this.prefix = prefix;
    }

    public void startRuntime() {
    }

    public void stopRuntime() {
        stopAllComponents();
    }

    public List<Class<?>> deployComponent(Path jarFilePath, String name) {
        int id = UniqueIdGenerator.generateNewId();
        String uniqueId = prefix + "-" + id;
        List<Class<?>> loadedClasses = new ArrayList<>();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFilePath.toUri().toURL()})) {
            JarFile jarFile = new JarFile(jarFilePath.toFile());
            Enumeration<JarEntry> entries = jarFile.entries();
            Class<?> startingClass = null;

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    Class<?> clazz = classLoader.loadClass(className);
                    loadedClasses.add(clazz);
                    findAnnotatedMethods(clazz);
                    if (startMethod != null && stopMethod != null) {
                        startingClass = clazz;
                        break;
                    }
                }
            }

            if (startingClass != null) {
                injectLogger(startMethodInstance);
                injectMessageBus(startMethodInstance);
                ComponentInstance componentInstance = new ComponentInstance(uniqueId, name, startingClass, startMethod, stopMethod, startMethodInstance, stopMethodInstance, jarFilePath);
                components.put(uniqueId, componentInstance);
            } else {
                System.out.println("Starting class not found in the JAR file.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return loadedClasses;
    }


    private void findAnnotatedMethods(Class<?> clazz) {
        try {
            startMethod = null;
            stopMethod = null;
            startMethodInstance = null;
            stopMethodInstance = null;
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Component.class)) {
                    Component componentAnnotation = method.getAnnotation(Component.class);
                    if (componentAnnotation.value() == Component.Lifecycle.START) {
                        startMethod = method;
                        startMethodInstance = clazz.getDeclaredConstructor().newInstance();
                    } else if (componentAnnotation.value() == Component.Lifecycle.STOP) {
                        stopMethod = method;
                        stopMethodInstance = clazz.getDeclaredConstructor().newInstance();
                    }
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void injectLogger(Object componentInstance) {
        Field[] fields = componentInstance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class) && field.getType().equals(Logger.class)) {
                field.setAccessible(true);
                try {
                    Logger logger = LoggerFactory.createLogger();
                    field.set(componentInstance, logger);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void injectMessageBus(Object componentInstance) {
        Field[] fields = componentInstance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class) && field.getType().equals(MessageQueue.class)) {
                field.setAccessible(true);
                try {
                    field.set(componentInstance, messageBus);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startAllComponents() {
        for (String componentID : components.keySet()) {
            startComponentById(componentID);
        }
    }

    public void stopAllComponents() {
        for (String componentID : components.keySet()) {
            stopComponentById(componentID);
        }
    }

    public void startComponentById(String componentID) {
        ComponentInstance componentInstance = components.get(componentID);
        if (componentInstance != null) {
            ComponentThread newThread = new ComponentThread(componentInstance, startMethod, stopMethod, startMethodInstance, stopMethodInstance);
            componentInstance.setComponentThread(newThread);
            newThread.start();
        } else {
            System.out.println("Component not found: " + componentID);
            System.out.println("Available components: " + components);
        }
    }


    public void stopComponentById(String componentID) {
        ComponentInstance componentInstance = components.get(componentID);
        if (componentInstance != null) {
            findAnnotatedMethods(componentInstance.getStartClass());
            if (stopMethod != null && stopMethodInstance != null) {
                try {
                    stopMethod.invoke(stopMethodInstance);
                    componentInstance.setState(ComponentState.STOPPED);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Stop method not found or not properly initialized.");
            }
        } else {
            System.out.println("Component not found: " + componentID);
        }
    }

    public void removeComponentById(String componentID) {
        ComponentInstance componentInstance = components.get(componentID);
        if (componentInstance != null) {
            if (componentInstance.getState() == ComponentState.STOPPED || componentInstance.getState() == ComponentState.INITIALIZED) {
                components.remove(componentID);
            } else {
                this.stopComponentById(componentID);
                components.remove(componentID);
            }
        } else {
            System.out.println("Component not found: " + componentID);
        }
    }

    public List<Map<String, Object>> getAllComponentStatuses() {
        List<Map<String, Object>> componentStatusList = new ArrayList<>();
        for (ComponentInstance componentInstance : components.values()) {
            Map<String, Object> componentStatus = new HashMap<>();
            componentStatus.put("ID", componentInstance.getID());
            componentStatus.put("name", componentInstance.getName());
            componentStatus.put("state", componentInstance.getState());
            componentStatusList.add(componentStatus);
        }
        return componentStatusList;
    }

    public JSONArray saveState() {
        JSONArray componentInstancesArray = new JSONArray();

        for (ComponentInstance componentInstance : components.values()) {
            JSONObject componentInstanceObject = new JSONObject();
            componentInstanceObject.put("ID", componentInstance.getID());
            componentInstanceObject.put("name", componentInstance.getName());
            componentInstanceObject.put("state", componentInstance.getState());
            componentInstanceObject.put("jarFilePath", componentInstance.getJarFilePath());
            componentInstancesArray.put(componentInstanceObject);
        }

        return componentInstancesArray;
    }


    public boolean hasComponent(String componentId) {
        return components.containsKey(componentId);
    }

}
