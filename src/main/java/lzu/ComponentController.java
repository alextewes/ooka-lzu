package lzu;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

    private static final ComponentLoader componentLoader = new ComponentLoader();

    @PostMapping("/start-runtime")
    public ResponseEntity<String> startRuntime() {
        componentLoader.startRuntime();
        return new ResponseEntity<>("Runtime started", HttpStatus.OK);
    }

    @PostMapping("/deploy")
    public ResponseEntity<String> deployComponent(@RequestBody Map<String, String> payload) {
        String componentJarPath = payload.get("componentJarPath");
        String componentName = payload.get("componentName");
        try {
            componentLoader.deployComponent(Path.of(componentJarPath), componentName);
            return new ResponseEntity<>("Component deployed with name: " + componentName, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deploying component: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startComponent(@RequestBody Map<String, Integer> payload) {
        int componentId = payload.get("componentId");
        System.out.println("Starting component with ID: " + componentId);
        Map<String, String> response = new HashMap<>();
        try {
            componentLoader.startComponentById(componentId);
            response.put("message", "Component started with ID: " + componentId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", "Error starting component: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopComponent(@RequestBody Map<String, Integer> payload) {
        int componentId = payload.get("componentId");
        Map<String, String> response = new HashMap<>();
        try {
            componentLoader.stopComponentById(componentId);
            response.put("message", "Component stopped with ID: " + componentId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", "Error stopping component: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save-state")
    public ResponseEntity<String> saveState() {
        try {
            componentLoader.saveState();
            return new ResponseEntity<>("State saved successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error saving state: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, String>> removeComponent(@RequestBody Map<String, Integer> payload) {
        int componentId = payload.get("componentId");
        Map<String, String> response = new HashMap<>();
        try {
            componentLoader.removeComponentById(componentId);
            response.put("message", "Component removed with ID: " + componentId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", "Error removing component: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/load-state")
    public ResponseEntity<String> loadState() {
        try {
            componentLoader.loadState();
            return new ResponseEntity<>("State loaded successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error loading state: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<List<Map<String, Object>>> getStatuses() {
        try {
            List<Map<String, Object>> statuses = componentLoader.getAllComponentStatuses();
            return new ResponseEntity<>(statuses, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}


