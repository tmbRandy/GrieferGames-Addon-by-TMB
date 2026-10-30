package tmb.randy.tmbgriefergames.core.flow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import tmb.randy.tmbgriefergames.core.Addon;

/** Loads, saves, triggers and runs the flows of the player. */
public final class FlowManager {
    private static final FlowManager INSTANCE = new FlowManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, Flow> flows = new LinkedHashMap<>();
    private final Map<String, FlowRunner> running = new LinkedHashMap<>();
    private boolean loaded;

    private FlowManager() {}

    public static FlowManager get() {
        return INSTANCE;
    }

    // ---------------------------------------------------------------- Storage

    private static Path getDirectory() {
        return Laby.labyAPI().labyModLoader().getGameDirectory().resolve("labymod-neo/configs/" + Addon.getNamespace() + "/flows");
    }

    private static Path getFile(String name) {
        return getDirectory().resolve(name.replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".json");
    }

    private void ensureLoaded() {
        if (loaded)
            return;

        loaded = true;
        Path directory = getDirectory();
        if (!Files.isDirectory(directory))
            return;

        try (Stream<Path> files = Files.list(directory)) {
            files.filter(file -> file.toString().endsWith(".json")).sorted().forEach(this::load);
        } catch (IOException e) {
            Addon.log().error("Could not list the flows", e);
        }
    }

    private void load(Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Flow flow = GSON.fromJson(reader, Flow.class);
            if (flow != null && flow.getName() != null) {
                flow.ensureDefaults();
                flows.put(flow.getName(), flow);
            }
        } catch (Exception e) {
            Addon.log().error("Could not load flow " + file, e);
        }
    }

    public void save(Flow flow) {
        ensureLoaded();
        flows.put(flow.getName(), flow);
        try {
            Files.createDirectories(getDirectory());
            try (Writer writer = Files.newBufferedWriter(getFile(flow.getName()), StandardCharsets.UTF_8)) {
                GSON.toJson(flow, writer);
            }
        } catch (IOException e) {
            Addon.log().error("Could not save flow " + flow.getName(), e);
        }
    }

    public void delete(Flow flow) {
        stop(flow.getName());
        flows.remove(flow.getName());
        try {
            Files.deleteIfExists(getFile(flow.getName()));
        } catch (IOException e) {
            Addon.log().error("Could not delete flow " + flow.getName(), e);
        }
    }

    public void rename(Flow flow, String newName) {
        delete(flow);
        flow.setName(newName);
        save(flow);
    }

    public List<Flow> getFlows() {
        ensureLoaded();
        List<Flow> list = new ArrayList<>(flows.values());
        list.sort(Comparator.comparing(Flow::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public Flow getFlow(String name) {
        ensureLoaded();
        for (Flow flow : flows.values()) {
            if (flow.getName().equalsIgnoreCase(name))
                return flow;
        }
        return null;
    }

    // ---------------------------------------------------------------- Running

    public boolean isRunning(String name) {
        return running.containsKey(name);
    }

    public boolean start(Flow flow) {
        FlowRunner runner = new FlowRunner(flow);
        if (runner.isFinished()) {
            Addon.displayNotification(Addon.translate("flow.noStart", flow.getName()));
            return false;
        }

        running.put(flow.getName(), runner);
        Addon.displayNotification(Addon.translate("flow.started", flow.getName()));
        return true;
    }

    public void stop(String name) {
        if (running.remove(name) != null)
            Addon.displayNotification(Addon.translate("flow.stopped", name));
    }

    public void stopAll() {
        List.copyOf(running.keySet()).forEach(this::stop);
    }

    public void toggle(Flow flow) {
        if (isRunning(flow.getName()))
            stop(flow.getName());
        else
            start(flow);
    }

    // ---------------------------------------------------------------- Events

    @Subscribe
    public void onTick(GameTickEvent event) {
        if (running.isEmpty() || Laby.labyAPI().minecraft().getClientPlayer() == null)
            return;

        for (FlowRunner runner : List.copyOf(running.values())) {
            runner.tick();
            if (runner.isFinished() && running.remove(runner.getFlow().getName()) != null)
                Addon.displayNotification(Addon.translate("flow.finished", runner.getFlow().getName()));
        }
    }

    @Subscribe
    public void onKey(KeyEvent event) {
        if (event.state() != State.PRESS || Addon.isGUIOpen())
            return;

        for (Flow flow : getFlows()) {
            Key[] keys = flow.getHotkey().stream().map(Key::getByName).filter(key -> key != null && key != Key.NONE).toArray(Key[]::new);
            if (keys.length > 0 && List.of(keys).contains(event.key()) && Addon.allKeysPressed(keys))
                toggle(flow);
        }
    }

    @Subscribe
    public void onChat(ChatReceiveEvent event) {
        if (running.isEmpty())
            return;

        String message = event.chatMessage().getPlainText();
        running.values().forEach(runner -> runner.onChatMessage(message));
    }

    @Subscribe
    public void onDisconnect(ServerDisconnectEvent event) {
        stopAll();
    }
}
