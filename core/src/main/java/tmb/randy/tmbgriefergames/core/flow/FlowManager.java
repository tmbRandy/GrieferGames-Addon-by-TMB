package tmb.randy.tmbgriefergames.core.flow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.Objects;
import java.util.stream.Stream;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import tmb.randy.tmbgriefergames.api.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.flow.node.NodeContext;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType;
import tmb.randy.tmbgriefergames.core.flow.node.NodeTypes;

/** Loads, saves, triggers and runs the flows of the player. */
public final class FlowManager {
    private static final FlowManager INSTANCE = new FlowManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, Flow> flows = new LinkedHashMap<>();
    private final Map<String, FlowRunner> running = new LinkedHashMap<>();
    private boolean loaded;
    private boolean wasInventoryFull;
    private boolean wasFishBiting;
    private String lastChestTitle;

    private FlowManager() {}

    public static FlowManager get() {
        return INSTANCE;
    }

    // ---------------------------------------------------------------- Storage

    /** Index of the flows shipped with the addon (one file name per line). */
    private static final String BUILTIN_INDEX = "/assets/tmbgriefergames/flows/index.txt";

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
        loadBuiltins();

        Path directory = getDirectory();
        if (!Files.isDirectory(directory))
            return;

        try (Stream<Path> files = Files.list(directory)) {
            files.filter(file -> file.toString().endsWith(".json")).sorted().forEach(file -> {
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    register(reader, false);
                } catch (Exception e) {
                    Addon.log().error("Could not load flow " + file, e);
                }
            });
        } catch (IOException e) {
            Addon.log().error("Could not list the flows", e);
        }
    }

    private void loadBuiltins() {
        try (InputStream index = FlowManager.class.getResourceAsStream(BUILTIN_INDEX)) {
            if (index == null)
                return;

            for (String file : new String(index.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                if (file.isBlank())
                    continue;

                try (InputStream stream = FlowManager.class.getResourceAsStream("/assets/tmbgriefergames/flows/" + file.trim());
                     Reader reader = new InputStreamReader(Objects.requireNonNull(stream, file), StandardCharsets.UTF_8)) {
                    register(reader, true);
                }
            }
        } catch (Exception e) {
            Addon.log().error("Could not load the built-in flows", e);
        }
    }

    /** Built-in flows win, an own flow with the same name is ignored. */
    private void register(Reader reader, boolean builtin) {
        Flow flow = GSON.fromJson(reader, Flow.class);
        if (flow == null || flow.getName() == null || (!builtin && isBuiltin(flow.getName())))
            return;

        flow.ensureDefaults();
        if (builtin)
            flow.markBuiltin();
        flows.put(flow.getName(), flow);
    }

    private boolean isBuiltin(String name) {
        Flow existing = getFlow(name);
        return existing != null && existing.isBuiltin();
    }

    /** Saves an own flow. Built-in flows are read only and never written. */
    public void save(Flow flow) {
        ensureLoaded();
        if (flow.isBuiltin())
            return;

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
        if (flow.isBuiltin())
            return;

        stop(flow.getName());
        flows.remove(flow.getName());
        try {
            Files.deleteIfExists(getFile(flow.getName()));
        } catch (IOException e) {
            Addon.log().error("Could not delete flow " + flow.getName(), e);
        }
    }

    public void rename(Flow flow, String newName) {
        if (flow.isBuiltin() || getFlow(newName) != null)
            return;

        delete(flow);
        flow.setName(newName);
        save(flow);
    }

    /** All flows, built-in flows first. */
    public List<Flow> getFlows() {
        ensureLoaded();
        List<Flow> list = new ArrayList<>(flows.values());
        list.sort(Comparator.comparing((Flow flow) -> !flow.isBuiltin()).thenComparing(Flow::getName, String.CASE_INSENSITIVE_ORDER));
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

    /** Starts the flow at its start block. */
    public boolean start(Flow flow) {
        FlowNode startNode = flow.getNodes().stream()
            .filter(node -> node.getType() != null && node.getType().id().equals(NodeTypes.START))
            .findFirst()
            .orElse(null);
        return start(flow, startNode);
    }

    private boolean start(Flow flow, FlowNode entry) {
        FlowRunner runner = new FlowRunner(flow, entry);
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

    /**
     * Passes an event to the running flows (their triggers interrupt them) and starts every other flow
     * that has a matching trigger with autostart.
     */
    private void fire(FlowEvent event) {
        for (FlowRunner runner : List.copyOf(running.values()))
            runner.handleEvent(event);

        if (event.type() == FlowEvent.Type.TICK)
            return;

        for (Flow flow : getFlows()) {
            if (isRunning(flow.getName()))
                continue;

            FlowRunner probe = new FlowRunner(flow, null);
            for (FlowNode node : flow.getNodes()) {
                NodeType type = node.getType();
                if (type != null && type.trigger() != null && Boolean.parseBoolean(node.getParam(NodeType.AUTOSTART))
                    && type.trigger().matches(new NodeContext(probe, node), event)) {
                    if (start(flow, node) && event.data() != null)
                        running.get(flow.getName()).setVariable(FlowRunner.EVENT_VARIABLE, event.data());
                    break;
                }
            }
        }
    }

    // ---------------------------------------------------------------- Events

    @Subscribe
    public void onTick(GameTickEvent event) {
        if (event.phase() != Phase.PRE || Laby.labyAPI().minecraft().getClientPlayer() == null || Addon.getConnection() == null)
            return;

        detectStateChanges();
        fire(new FlowEvent(FlowEvent.Type.TICK, null));

        for (FlowRunner runner : List.copyOf(running.values())) {
            runner.tick();
            if (runner.isFinished() && running.remove(runner.getFlow().getName()) != null)
                Addon.displayNotification(Addon.translate("flow.finished", runner.getFlow().getName()));
        }
    }

    /** Turns polled game state into events, each fires once when the state starts. */
    private void detectStateChanges() {
        boolean inventoryFull = Addon.getConnection().isInventoryFull();
        if (inventoryFull && !wasInventoryFull)
            fire(new FlowEvent(FlowEvent.Type.INVENTORY_FULL, null));
        wasInventoryFull = inventoryFull;

        boolean fishBiting = Addon.getConnection().isFishBiting();
        if (fishBiting && !wasFishBiting)
            fire(new FlowEvent(FlowEvent.Type.FISH_BITE, null));
        wasFishBiting = fishBiting;

        String title = Addon.getConnection().getChestTitle();
        if (title != null && !title.equals(lastChestTitle))
            fire(new FlowEvent(FlowEvent.Type.MENU_OPEN, title));
        lastChestTitle = title;
    }

    @Subscribe
    public void onKey(KeyEvent event) {
        if (event.state() != State.PRESS || Laby.labyAPI().minecraft().getClientPlayer() == null)
            return;

        fire(new FlowEvent(FlowEvent.Type.KEY, event.key().getActualName()));
        if (Addon.isGUIOpen())
            return;

        for (Flow flow : getFlows()) {
            Key[] keys = flow.getHotkey().stream().map(Key::getByName).filter(key -> key != null && key != Key.NONE).toArray(Key[]::new);
            if (keys.length > 0 && List.of(keys).contains(event.key()) && Addon.allKeysPressed(keys))
                toggle(flow);
        }
    }

    @Subscribe
    public void onChat(ChatReceiveEvent event) {
        String message = event.chatMessage().getPlainText();
        running.values().forEach(runner -> runner.onChatMessage(message));
        fire(new FlowEvent(FlowEvent.Type.CHAT, message));
    }

    @Subscribe
    public void onCbChanged(CbChangedEvent event) {
        fire(new FlowEvent(FlowEvent.Type.CB_CHANGE, event.CB().getName()));
    }

    @Subscribe
    public void onDisconnect(ServerDisconnectEvent event) {
        stopAll();
    }
}
