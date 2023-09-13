package lzu.utils;

import lzu.service.ComponentLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateLoaderUtils {

    public static List<Map<String, String>> loadComponentStates() throws IOException {
        String filePath = "state.json";
        Path file = Paths.get(filePath);

        String jsonState = Files.readString(file);
        JSONObject jsonObject = new JSONObject(jsonState);
        JSONArray componentInstancesArray = jsonObject.getJSONArray("componentInstances");

        List<Map<String, String>> componentStates = new ArrayList<>();
        for (int i = 0; i < componentInstancesArray.length(); i++) {
            JSONObject componentInstanceObject = componentInstancesArray.getJSONObject(i);
            String name = componentInstanceObject.getString("name");
            Path jarFilePath = Path.of(componentInstanceObject.getString("jarFilePath"));
            String state = componentInstanceObject.getString("state");
            String componentId = componentInstanceObject.getString("ID");

            Map<String, String> componentState = new HashMap<>();
            componentState.put("name", name);
            componentState.put("jarFilePath", jarFilePath.toString());
            componentState.put("state", state);
            componentState.put("ID", componentId);

            componentStates.add(componentState);
        }

        return componentStates;
    }

    public static void loadStateForLoader(ComponentLoader loader, List<Map<String, String>> componentStates) throws IOException, ClassNotFoundException {
        for (Map<String, String> componentState : componentStates) {
            String name = componentState.get("name");
            Path jarFilePath = Path.of(componentState.get("jarFilePath"));

            loader.deployComponent(jarFilePath, name);
        }
    }


    public static ResponseEntity<String> saveState(LoadBalancer loadBalancer) {
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

    public static ResponseEntity<String> loadState(LoadBalancer loadBalancer) {
        try {
            List<Map<String, String>> componentStates = loadComponentStates();
            List<ComponentLoader> loaders = loadBalancer.getAllLoaders();
            int numLoaders = loaders.size();

            int totalComponents = componentStates.size();
            int componentsPerLoader = totalComponents / numLoaders;
            int extraComponents = totalComponents % numLoaders;

            int startIndex = 0;
            for (int i = 0; i < numLoaders; i++) {
                int endIndex = startIndex + componentsPerLoader + (i < extraComponents ? 1 : 0);
                List<Map<String, String>> loaderComponentStates = componentStates.subList(startIndex, endIndex);
                loadStateForLoader(loaders.get(i), loaderComponentStates);
                startIndex = endIndex;
            }

            return new ResponseEntity<>("State loaded successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error loading state: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
