package tmb.randy.tmbgriefergames.core.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.labymod.api.Laby;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.activities.plotwheel.PlotWheelPlot;
import tmb.randy.tmbgriefergames.core.enums.CBs;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FileManager {
    private static final File storageFile = new File(new File(Laby.labyAPI().labyModLoader().getGameDirectory().toFile(), "labymod-neo/configs/" + Addon.getNamespace()), Addon.getNamespace() + ".json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, Object> globalData = new HashMap<>();
    private static Map<UUID, Map<String, Object>> playerData = new HashMap<>();

    static {
        loadData();
    }

    private static void saveData() {
        try {
            if (!storageFile.getParentFile().exists())
                storageFile.getParentFile().mkdirs();

            if (!storageFile.exists())
                storageFile.createNewFile();


            Map<String, Object> allData = new HashMap<>();
            Map<String, Map<String, Object>> playerDataAsStringKeys = new HashMap<>();
            for (Map.Entry<UUID, Map<String, Object>> entry : playerData.entrySet()) {
                playerDataAsStringKeys.put(entry.getKey().toString(), entry.getValue());
            }

            allData.put("global", globalData);
            allData.put("player", playerDataAsStringKeys);

            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(allData, writer);
            }
        } catch (IOException e) {
            Addon.getSharedInstance().logger().error("FileManager", e);
        }
    }

    private static void loadData() {
        if (!storageFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(storageFile)) {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> allData = gson.fromJson(reader, type);

            if (allData != null) {
                globalData = (Map<String, Object>) allData.getOrDefault("global", new HashMap<>());

                Map<String, Map<String, Object>> rawPlayerData = (Map<String, Map<String, Object>>) allData.getOrDefault("player", new HashMap<>());
                playerData = new HashMap<>();
                for (Map.Entry<String, Map<String, Object>> entry : rawPlayerData.entrySet()) {
                    UUID playerUUID = UUID.fromString(entry.getKey());
                    playerData.put(playerUUID, entry.getValue());
                }
            } else {
                globalData = new HashMap<>();
                playerData = new HashMap<>();
            }
        } catch (IOException e) {
            Addon.getSharedInstance().logger().error("FileManager", e);
        } catch (ClassCastException e) {
            Addon.getSharedInstance().logger().error("FileManager - Invalid format", e);
        }
    }

    public static void setGlobalValue(String key, Object value) {
        globalData.put(key, value);
        saveData();
    }

    public static Object getGlobalValue(String key) {
        return globalData.get(key);
    }

    public static void addPlot(PlotWheelPlot plot) {
        List<PlotWheelPlot> plots = loadPlots();
        plots.add(plot);
        setGlobalValue("PlotWheel", plots);
    }

    public static ArrayList<PlotWheelPlot> loadPlots() {
        Object value = getGlobalValue("PlotWheel");
        ArrayList<PlotWheelPlot> plots = new ArrayList<>();

        if (value instanceof List<?>) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> plotDataList = gson.fromJson(gson.toJson(value), listType);

            for (Map<String, Object> plotData : plotDataList) {
                CBs cb = CBs.valueOf((String) plotData.get("cb"));
                String name = (String) plotData.get("name");
                String command = (String) plotData.get("command");
                UUID account = plotData.get("account") != null ? UUID.fromString((String) plotData.get("account")) : null;

                plots.add(new PlotWheelPlot(cb, name, command, account));
            }
        }

        return plots;
    }

    public static void deletePlot(PlotWheelPlot plot) {
        List<PlotWheelPlot> plots = loadPlots();
        plots.removeIf(plott -> plott.toString().equals(plot.toString()));
        setGlobalValue("PlotWheel", plots);
    }

    public static void setPlayerValue(String key, Object value) {
        if (Laby.labyAPI().minecraft().getClientPlayer() != null) {
            UUID playerUUID = Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).profile().getUniqueId();
            Map<String, Object> values = playerData.computeIfAbsent(playerUUID, k -> new HashMap<>());
            values.put(key, value);
            saveData();
        }
    }

    public static Object getPlayerValue(String key) {
        if (Laby.labyAPI().minecraft().getClientPlayer() != null) {
            UUID playerUUID = Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).profile().getUniqueId();
            Map<String, Object> values = playerData.get(playerUUID);
            return values != null ? values.get(key) : null;
        }
        return null;
    }
}
