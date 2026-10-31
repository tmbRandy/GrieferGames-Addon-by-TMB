package tmb.randy.tmbgriefergames.core.flow;

import java.util.ArrayList;
import java.util.List;
import net.labymod.api.client.gui.screen.key.Key;

/**
 * A player made program: blocks ({@link FlowNode}) connected by {@link FlowConnection}s.
 * Stored as JSON, so all fields are plain data.
 */
public class Flow {
    private String name;
    /** Keys that start/stop the flow (LabyMod key names), may be empty. */
    private List<String> hotkey = new ArrayList<>();
    /** No chat messages when the flow starts or ends, for flows that react to events all the time. */
    private boolean silent;
    /** Optional: name of an addon setting that holds the hotkey (built-in flows use the key the player configured). */
    private String hotkeySetting;
    private List<FlowNode> nodes = new ArrayList<>();
    private List<FlowConnection> connections = new ArrayList<>();
    /** Shipped with the addon: read only, never saved. Transient, so it is not part of the JSON. */
    private transient boolean builtin;

    public Flow(String name) {
        this.name = name;
    }

    public boolean isSilent() {
        return silent;
    }

    public boolean isBuiltin() {
        return builtin;
    }

    void markBuiltin() {
        builtin = true;
    }

    /** A modifiable copy, e.g. to use a built-in flow as template for an own one. */
    public Flow copy(String newName) {
        Flow copy = new Flow(newName);
        copy.hotkey = new ArrayList<>(hotkey);
        copy.nodes = new ArrayList<>(nodes.stream().map(FlowNode::copy).toList());
        copy.connections = new ArrayList<>(connections);
        return copy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getHotkey() {
        return hotkey;
    }

    public void setHotkey(List<String> hotkey) {
        this.hotkey = hotkey;
    }

    /** The keys that toggle the flow: the configured setting if the flow names one, otherwise its own hotkey. */
    public Key[] getHotkeyKeys() {
        if (hotkeySetting != null && !hotkeySetting.isBlank())
            return FlowSettings.getKeys(hotkeySetting);
        return hotkey.stream().map(Key::getByName).filter(key -> key != null && key != Key.NONE).toArray(Key[]::new);
    }

    public List<FlowNode> getNodes() {
        return nodes;
    }

    public List<FlowConnection> getConnections() {
        return connections;
    }

    public FlowNode getNode(String id) {
        for (FlowNode node : nodes) {
            if (node.getId().equals(id))
                return node;
        }
        return null;
    }

    /** The node the given output of a node is connected to, null if the output is not connected. */
    public FlowNode getTarget(FlowNode from, String output) {
        for (FlowConnection connection : connections) {
            if (connection.from().equals(from.getId()) && connection.output().equals(output))
                return getNode(connection.to());
        }
        return null;
    }

    /** Connects an output to a node; an output can only lead to one node, so an old connection is replaced. */
    public void connect(FlowNode from, String output, FlowNode to) {
        connections.removeIf(connection -> connection.from().equals(from.getId()) && connection.output().equals(output));
        connections.add(new FlowConnection(from.getId(), output, to.getId()));
    }

    public void removeNode(FlowNode node) {
        nodes.remove(node);
        connections.removeIf(connection -> connection.from().equals(node.getId()) || connection.to().equals(node.getId()));
    }

    /** Gson leaves fields missing in older files null, this restores the defaults. */
    void ensureDefaults() {
        if (hotkey == null)
            hotkey = new ArrayList<>();
        if (nodes == null)
            nodes = new ArrayList<>();
        if (connections == null)
            connections = new ArrayList<>();
        nodes.forEach(FlowNode::ensureDefaults);
    }
}
