package lzu.utils;

import lzu.service.ComponentLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StateLoaderUtils {

    public static List<Map<String, String>> loadComponentStates(String filename) throws IOException {
        String jsonStr = new String(Files.readAllBytes(Paths.get(filename)));
        JSONObject jsonObj = new JSONObject(jsonStr);
        JSONArray componentInstances = jsonObj.getJSONArray("componentInstances");

        List<Map<String, String>> componentStates = new ArrayList<>();
        for (int i = 0; i < componentInstances.length(); i++) {
            JSONObject instance = componentInstances.getJSONObject(i);
            String name = instance.getString("name");
            String jarFilePath = instance.getString("jarFilePath");

            componentStates.add(Map.of("name", name, "jarFilePath", jarFilePath));
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
}
