package lzu.utils;

import lzu.service.ComponentLoader;
import org.json.JSONArray;
import org.json.JSONObject;

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

    public static void loadStateForLoader(ComponentLoader loader, List<Map<String, String>> componentStates) {
        for (Map<String, String> componentState : componentStates) {
            String name = componentState.get("name");
            Path jarFilePath = Path.of(componentState.get("jarFilePath"));
            String state = componentState.get("state");
            String componentId = componentState.get("ID");

            List<Class<?>> loadedClasses = loader.deployComponent(jarFilePath, name);

            if ("RUNNING".equals(state) && !loadedClasses.isEmpty()) {
                loader.startComponentById(componentId);
            } else if ("STOPPED".equals(state) && !loadedClasses.isEmpty()) {
                loader.startComponentById(componentId);
                // timeout to make sure the component is started
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                loader.stopComponentById(componentId);
            }
        }
    }


}
