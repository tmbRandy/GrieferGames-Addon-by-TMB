package tmb.randy.tmbgriefergames.core.flow.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Category;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Executor;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Trigger;
import tmb.randy.tmbgriefergames.core.flow.node.nodes.DataNodes;
import tmb.randy.tmbgriefergames.core.flow.node.nodes.InventoryNodes;
import tmb.randy.tmbgriefergames.core.flow.node.nodes.LogicNodes;
import tmb.randy.tmbgriefergames.core.flow.node.nodes.MenuNodes;
import tmb.randy.tmbgriefergames.core.flow.node.nodes.PlayerNodes;
import tmb.randy.tmbgriefergames.core.flow.node.nodes.TriggerNodes;

/** Registry of all block types. The blocks themselves are registered by the classes in the {@code nodes} package. */
public final class NodeTypes {
    public static final String START = "start";

    // Output names shared by many blocks
    public static final String YES = "yes";
    public static final String NO = "no";
    public static final String OK = "ok";
    public static final String TIMEOUT = "timeout";
    public static final String FOUND = "found";
    public static final String NONE = "none";

    private static final Map<String, NodeType> TYPES = new LinkedHashMap<>();

    static {
        TriggerNodes.register();
        PlayerNodes.register();
        MenuNodes.register();
        InventoryNodes.register();
        LogicNodes.register();
        DataNodes.register();
    }

    private NodeTypes() {}

    public static void register(String id, Category category, List<Parameter> parameters, List<String> outputs, boolean instant, Executor executor) {
        TYPES.put(id, new NodeType(id, category, parameters, outputs, instant, executor, null));
    }

    /** Registers a block that simply continues with "next" after it ran once. */
    public static void action(String id, Category category, List<Parameter> parameters, Executor executor) {
        register(id, category, parameters, List.of(NodeType.NEXT), false, executor);
    }

    /** Registers a yes/no condition that is checked without using up a tick. */
    public static void condition(String id, Category category, List<Parameter> parameters, Condition condition) {
        register(id, category, parameters, List.of(YES, NO), true, context -> condition.test(context) ? YES : NO);
    }

    /** Registers a trigger block; every trigger can optionally start its flow (autostart). */
    public static void trigger(String id, List<Parameter> parameters, Trigger trigger) {
        List<Parameter> withAutostart = new ArrayList<>(parameters);
        withAutostart.add(Parameter.bool(NodeType.AUTOSTART, false));
        TYPES.put(id, new NodeType(id, Category.TRIGGER, List.copyOf(withAutostart), List.of(NodeType.NEXT), true, context -> NodeType.NEXT, trigger));
    }

    @FunctionalInterface
    public interface Condition {
        boolean test(NodeContext context);
    }

    public static NodeType get(String id) {
        return TYPES.get(id);
    }

    public static Collection<NodeType> all() {
        return Collections.unmodifiableCollection(TYPES.values());
    }

    // ---------------------------------------------------------------- Matching helpers for the blocks

    public static String stripColors(String text) {
        return text == null ? "" : text.replaceAll("§.", "");
    }

    /** Case insensitive "contains" without color codes, so titles can be typed without § codes. Empty matches everything that exists. */
    public static boolean textMatches(String actual, String expected) {
        if (actual == null)
            return false;
        return expected.isBlank() || stripColors(actual).toLowerCase().contains(stripColors(expected).toLowerCase().trim());
    }

    /** Matches a slot by registry name ("stone" / "minecraft:stone") or a part of the display name. Empty matches every non empty slot. */
    public static boolean slotMatches(NodeContext context, int slot, String expected) {
        String item = context.game().getSlotItem(slot);
        if (item == null)
            return false;
        return expected.isBlank() || itemMatches(item, context.game().getSlotItemName(slot), expected);
    }

    public static boolean itemMatches(String registryName, String displayName, String expected) {
        String lowerCase = expected.toLowerCase().trim();
        return registryName.equals(lowerCase) || registryName.equals("minecraft:" + lowerCase) || stripColors(displayName).toLowerCase().contains(lowerCase);
    }
}
