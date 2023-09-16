package lzu.api;

import lzu.service.ComponentLoader;
import lzu.utils.ComponentLoadBalancer;
import lzu.utils.StateLoaderUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

    private ComponentLoadBalancer loadBalancer = new ComponentLoadBalancer(new ComponentLoader());

    @PostMapping("/start-runtime")
    public ResponseEntity<String> startRuntime() {
        try {
            loadBalancer.getComponentLoader().startRuntime();
            return new ResponseEntity<>("Runtime started successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error starting runtime: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/stop-runtime")
    public ResponseEntity<String> stopRuntime() {
        try {
            loadBalancer.getComponentLoader().stopRuntime();
            return new ResponseEntity<>("Runtime stopped successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error stopping runtime: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deploy")
    public ResponseEntity<String> deployComponent(@RequestBody Map<String, String> payload) {
        String componentJarPath = payload.get("componentJarPath");
        String componentName = payload.get("componentName");
        int instanceCount = Integer.parseInt(payload.get("instanceCount"));
        try {
            loadBalancer.deployAndBalanceComponent(Path.of(componentJarPath), componentName, instanceCount);
            return new ResponseEntity<>(instanceCount + "Component(s) deployed with name: " + componentName, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deploying component: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startComponent(@RequestBody Map<String, String> payload) {
        String componentName = payload.get("componentName");
        Map<String, String> response = new HashMap<>();
        String id = loadBalancer.getNextComponentInstance(componentName);

        try {
            loadBalancer.getComponentLoader().startComponentById(id);
            response.put("message", "Component started with ID: " + id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopComponent(@RequestBody Map<String, String> payload) {
        String componentId = payload.get("componentId");
        Map<String, String> response = new HashMap<>();
        ComponentLoader loader = loadBalancer.getComponentLoader();

        try {
            loader.stopComponentById(componentId);
            response.put("message", "Component stopped with ID: " + componentId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save-state")
    public ResponseEntity<String> saveState(@RequestBody Map<String, String> payload) {
        String filename = payload.get("filename");
        if (filename == null || filename.isEmpty()) {
            return ResponseEntity.badRequest().body("Filename is required");
        }
        try {
            loadBalancer.getComponentLoader().saveState(filename);
            return ResponseEntity.ok("State saved successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error saving state: " + e.getMessage());
        }

    }

    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, String>> removeComponent(@RequestBody Map<String, String> payload) {
        String componentId = payload.get("componentId");
        Map<String, String> response = new HashMap<>();
        ComponentLoader loader = loadBalancer.getComponentLoader();

        try {
            boolean wasRemoved = loader.removeComponentById(componentId);
            if (wasRemoved) {
                response.put("message", "Component removed with ID: " + componentId);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("error", "Component not found: " + componentId);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put("error", "Error removing component: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/load-state")
    public ResponseEntity<String> loadState(@RequestBody Map<String, String> payload) {
        String filename = payload.get("filename");
        if (filename == null || filename.isEmpty()) {
            return ResponseEntity.badRequest().body("Filename is required");
        }
        try {
            List<Map<String, String>> componentStates = StateLoaderUtils.loadComponentStates(filename);
            ComponentLoader loader = loadBalancer.getComponentLoader();
            StateLoaderUtils.loadStateForLoader(loader, componentStates);
            loadBalancer.reloadQueues();
            return ResponseEntity.ok("State loaded successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error loading state: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/status")
    public ResponseEntity<List<Map<String, Object>>> getStatuses() {
        try {

            return new ResponseEntity<>(loadBalancer.getComponentLoader().getAllComponentStatuses(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}