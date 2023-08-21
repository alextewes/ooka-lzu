package lzu.api;

import lzu.service.ComponentLoader;
import lzu.utils.StateLoaderUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lzu.utils.LoadBalancer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

    @Autowired
    private LoadBalancer loadBalancer;

    private ComponentLoader getLoader() {
        return loadBalancer.getNextLoader();
    }

    private ComponentLoader findLoaderByComponentId(String componentId) {
        for (ComponentLoader loader : loadBalancer.getAllLoaders()) {
            if (loader.hasComponent(componentId)) {
                return loader;
            }
        }
        return null;
    }

    @PostMapping("/start-runtime")
    public ResponseEntity<String> startRuntime() {
        try {
            List<ComponentLoader> loaders = loadBalancer.getAllLoaders();
            for (ComponentLoader loader : loaders) {
                loader.startRuntime();
            }
            return new ResponseEntity<>("Runtime started successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error starting runtime: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/stop-runtime")
    public ResponseEntity<String> stopRuntime() {
        try {
            List<ComponentLoader> loaders = loadBalancer.getAllLoaders();
            for (ComponentLoader loader : loaders) {
                loader.stopRuntime();
            }
            return new ResponseEntity<>("Runtime stopped successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error stopping runtime: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deploy")
    public ResponseEntity<String> deployComponent(@RequestBody Map<String, String> payload) {
        String componentJarPath = payload.get("componentJarPath");
        String componentName = payload.get("componentName");
        try {
            getLoader().deployComponent(Path.of(componentJarPath), componentName);
            return new ResponseEntity<>("Component deployed with name: " + componentName, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deploying component: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startComponent(@RequestBody Map<String, String> payload) {
        String componentId = payload.get("componentId");
        Map<String, String> response = new HashMap<>();
        ComponentLoader loader = findLoaderByComponentId(componentId);
        if (loader != null) {
            try {
                loader.startComponentById(componentId);
                response.put("message", "Component started with ID: " + componentId);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                response.put("error", "Error starting component: " + e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("error", "Component not found: " + componentId);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopComponent(@RequestBody Map<String, String> payload) {
        String componentId = payload.get("componentId");
        Map<String, String> response = new HashMap<>();
        ComponentLoader loader = findLoaderByComponentId(componentId);
        if (loader != null) {
            try {
                loader.stopComponentById(componentId);
                response.put("message", "Component stopped with ID: " + componentId);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                response.put("error", "Error stopping component: " + e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("error", "Component not found: " + componentId);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/save-state")
    public ResponseEntity<String> saveState() {
        try {
            JSONArray allComponentInstancesArray = new JSONArray();

            for (ComponentLoader loader : loadBalancer.getAllLoaders()) {
                JSONArray loaderComponentInstancesArray = loader.saveState();
                for (int i = 0; i < loaderComponentInstancesArray.length(); i++) {
                    allComponentInstancesArray.put(loaderComponentInstancesArray.getJSONObject(i));
                }
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("componentInstances", allComponentInstancesArray);

            String jsonState = jsonObject.toString();

            String filePath = "state.json";
            Path file = Paths.get(filePath);
            Files.write(file, jsonState.getBytes());

            return new ResponseEntity<>("States saved successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error saving state: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, String>> removeComponent(@RequestBody Map<String, String> payload) {
        String componentId = payload.get("componentId");
        Map<String, String> response = new HashMap<>();
        ComponentLoader loader = findLoaderByComponentId(componentId);
        if (loader != null) {
            try {
                loader.removeComponentById(componentId);
                response.put("message", "Component removed with ID: " + componentId);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (Exception e) {
                response.put("error", "Error removing component: " + e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("error", "Component not found: " + componentId);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/load-state")
    public ResponseEntity<String> loadState() {
        try {
            List<Map<String, String>> componentStates = StateLoaderUtils.loadComponentStates();
            List<ComponentLoader> loaders = loadBalancer.getAllLoaders();
            int numLoaders = loaders.size();

            int totalComponents = componentStates.size();
            int componentsPerLoader = totalComponents / numLoaders;
            int extraComponents = totalComponents % numLoaders;

            int startIndex = 0;
            for (int i = 0; i < numLoaders; i++) {
                int endIndex = startIndex + componentsPerLoader + (i < extraComponents ? 1 : 0);
                List<Map<String, String>> loaderComponentStates = componentStates.subList(startIndex, endIndex);
                StateLoaderUtils.loadStateForLoader(loaders.get(i), loaderComponentStates);
                startIndex = endIndex;
            }

            return new ResponseEntity<>("State loaded successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error loading state: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<List<Map<String, Object>>> getStatuses() {
        List<Map<String, Object>> allStatuses = new ArrayList<>();
        try {
            for (ComponentLoader loader : loadBalancer.getAllLoaders()) {
                allStatuses.addAll(loader.getAllComponentStatuses());
            }
            return new ResponseEntity<>(allStatuses, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


