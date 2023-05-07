package lzu;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import custom.annotation.component.Component;
import logger.ConsoleLogger;
import logger.Inject;
import logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class ComponentLoader {
    private Method startMethod;
    private Method stopMethod;
    private Object startMethodInstance;
    private Object stopMethodInstance;
    private Map<Integer, ComponentInstance> components;
    private AtomicInteger componentIDCounter;

    public ComponentLoader() {
        components = new ConcurrentHashMap<>();
        componentIDCounter = new AtomicInteger(0);
    }

    public void startRuntime() {
    }

    public void stopRuntime() {
        stopAllComponents();
    }

    public List<Class<?>> deployComponent(Path jarFilePath, String name) {
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
                int componentID = componentIDCounter.incrementAndGet();
                injectLogger(startMethodInstance);
                ComponentInstance componentInstance = new ComponentInstance(componentID, name, startingClass, startMethod, stopMethod, startMethodInstance, stopMethodInstance, jarFilePath);
                components.put(componentID, componentInstance);
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
                    Logger logger = new ConsoleLogger();
                    field.set(componentInstance, logger);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startAllComponents() {
        for (int componentID : components.keySet()) {
            startComponentById(componentID);
        }
    }

    public void stopAllComponents() {
        for (int componentID : components.keySet()) {
            stopComponentById(componentID);
        }
    }

    public void startComponentById(int componentID) {
        ComponentInstance componentInstance = components.get(componentID);
        if (componentInstance != null) {
            componentInstance.getComponentThread().start();
        } else {
            System.out.println("Component not found: " + componentID);
        }
    }


    public void stopComponentById(int componentID) {
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

    public void removeComponentById(int componentID) {
        ComponentInstance componentInstance = components.get(componentID);
        if (componentInstance != null) {
            if (componentInstance.getState() == ComponentState.STOPPED || componentInstance.getState() == ComponentState.INITIALIZED) {
                components.remove(componentID);
            } else {
                System.out.println("Component is not stopped. Please stop the component before removing it.");
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

    public void saveState(String apiUrl) {
        try {
            JSONArray componentInstancesArray = new JSONArray();

            for (ComponentInstance componentInstance : components.values()) {
                JSONObject componentInstanceObject = new JSONObject();
                componentInstanceObject.put("ID", componentInstance.getID());
                componentInstanceObject.put("name", componentInstance.getName());
                componentInstanceObject.put("state", componentInstance.getState());
                componentInstanceObject.put("jarFilePath", componentInstance.getJarFilePath());
                componentInstancesArray.put(componentInstanceObject);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("componentInstances", componentInstancesArray);

            String jsonState = jsonObject.toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonState))
                    .header("Content-Type", "application/json")
                    .build();
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public void loadState(String apiUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            String jsonState = response.body();

            JSONObject jsonObject = new JSONObject(jsonState);
            JSONArray componentInstancesArray = jsonObject.getJSONArray("componentInstances");

            components.clear();

            for (int i = 0; i < componentInstancesArray.length(); i++) {
                JSONObject componentInstanceObject = componentInstancesArray.getJSONObject(i);
                String name = componentInstanceObject.getString("name");
                Path jarFilePath = Path.of(componentInstanceObject.getString("jarFilePath"));

                deployComponent(jarFilePath, name);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }


}
