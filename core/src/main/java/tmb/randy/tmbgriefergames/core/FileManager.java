package tmb.randy.tmbgriefergames.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.labymod.api.Laby;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileManager {
    private static final File storageFile = new File(new File(Laby.labyAPI().labyModLoader().getGameDirectory().toFile(), "labymod-neo/configs/tmbgriefergames"), "tmbgriefergames.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Map<UUID, Map<String, Object>> playerData = new HashMap<>();

    static {
        loadData();
    }

    private static void saveData() {
        try {
            if (!storageFile.getParentFile().exists()) {
                storageFile.getParentFile().mkdirs();
            }
            if (!storageFile.exists()) {
                storageFile.createNewFile();
            }

            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(playerData, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadData() {
        if (!storageFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(storageFile)) {
            Type type = new TypeToken<Map<UUID, Map<String, Object>>>() {}.getType();
            playerData = gson.fromJson(reader, type);
            if (playerData == null) {
                playerData = new HashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setValue(String key, Object value) {
        if(Laby.labyAPI().minecraft().getClientPlayer() != null) {
            UUID playerUUID = Laby.labyAPI().minecraft().getClientPlayer().profile().getUniqueId();
            Map<String, Object> values = playerData.computeIfAbsent(playerUUID, k -> new HashMap<>());
            values.put(key, value);
            saveData();
        }
    }

    public static Object getValue(String key) {
        if(Laby.labyAPI().minecraft().getClientPlayer() != null) {
            UUID playerUUID = Laby.labyAPI().minecraft().getClientPlayer().profile().getUniqueId();
            Map<String, Object> values = playerData.get(playerUUID);
            return values != null ? values.get(key) : null;
        }
        return null;
    }
}
