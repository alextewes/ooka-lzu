package lzu.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import lzu.utils.Component;
import lzu.utils.logger.ConsoleLogger;
import lzu.utils.logger.Inject;
import lzu.model.ComponentState;
import lzu.model.ComponentInstance;
import lzu.utils.MessageQueue;
import org.json.JSONArray;
import org.json.JSONObject;

public class ComponentLoader {

    private Map<String, ComponentInstance> components = new HashMap<>();
    private final MessageQueue messageQueue;
    private Map<String, String> componentNameToLastID = new ConcurrentHashMap<>();
    private final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    public ComponentLoader() {
        this.messageQueue = MessageQueue.getInstance();
    }

    public void startRuntime() {
    }

    public void stopRuntime() {
        try {
            List<String> idsToRemove = new ArrayList<>(components.keySet());
            for (String componentID : idsToRemove) {
                removeComponentById(componentID);
                components.remove(componentID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Class<?>> deployComponent(Path jarFilePath, String name) throws IOException, ClassNotFoundException {
        String id = "" + ID_COUNTER.incrementAndGet();
        List<Class<?>> loadedClasses = new ArrayList<>();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFilePath.toUri().toURL()})) {
            JarFile jarFile = new JarFile(jarFilePath.toFile());
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    Class<?> clazz = classLoader.loadClass(className);
                    loadedClasses.add(clazz);

                    ComponentInstance componentInstance = findAnnotatedMethods(clazz, id, name, jarFilePath);
                    if (componentInstance != null) {
                        components.put(id, componentInstance);
                        componentNameToLastID.put(name, id);
                        saveState("state.json");
                        break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
        return loadedClasses;
    }

    private ComponentInstance findAnnotatedMethods(Class<?> clazz, String id, String name, Path jarFilePath) {
        Method startMethod = null;
        Method stopMethod = null;
        Object startMethodInstance = null;
        Object stopMethodInstance = null;

        try {
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

            if (startMethod != null && stopMethod != null) {
                return new ComponentInstance(
                        id, name, clazz, startMethod, stopMethod, startMethodInstance, stopMethodInstance, jarFilePath
                );
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void injectDependency(ComponentInstance componentInstance, Object dependency) {
        Field[] fields = componentInstance.getStartClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class) && field.getType().equals(dependency.getClass())) {
                field.setAccessible(true);
                try {
                    field.set(componentInstance, dependency);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startComponentById(String componentID) throws Exception {
        ComponentInstance componentInstance = components.get(componentID);
        if (componentInstance == null) {
            throw new Exception("Component not found: " + componentID);
        }
        injectDependency(componentInstance, new ConsoleLogger());
        injectDependency(componentInstance, messageQueue);
        componentInstance.getComponentThread().run();
    }
    public void stopComponentById(String componentID) throws Exception {
        ComponentInstance componentInstance = components.get(componentID);
        if (componentInstance == null) {
            throw new Exception("Component not found: " + componentID);
        }
        componentInstance.getComponentThread().stopComponent();
    }

    public boolean removeComponentById(String componentID) throws Exception {
        ComponentInstance componentInstance = components.get(componentID);
        if (componentInstance != null) {
            if (componentInstance.getState() == ComponentState.STOPPED || componentInstance.getState() == ComponentState.INITIALIZED) {
                components.remove(componentID);
            } else {
                this.stopComponentById(componentID);
                components.remove(componentID);
            }
            return true;
        }
        return false;
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

    public JSONObject saveState(String filePath) throws IOException {
        JSONArray componentInstancesArray = new JSONArray();
        JSONObject finalStateObject = new JSONObject();

        for (ComponentInstance componentInstance : components.values()) {
            JSONObject componentInstanceObject = new JSONObject();
            componentInstanceObject.put("ID", componentInstance.getID());
            componentInstanceObject.put("name", componentInstance.getName());
            componentInstanceObject.put("state", componentInstance.getState());
            componentInstanceObject.put("jarFilePath", componentInstance.getJarFilePath());
            componentInstancesArray.put(componentInstanceObject);
        }

        finalStateObject.put("componentInstances", componentInstancesArray);

        String jsonState = finalStateObject.toString();

        Path file = Paths.get(filePath);
        Files.write(file, jsonState.getBytes());

        return finalStateObject;
    }

    public String getLastInsertedComponentID(String name) {
        return componentNameToLastID.get(name);
    }

    public Map<String, ComponentInstance> getComponents() {
        return components;
    }
}
