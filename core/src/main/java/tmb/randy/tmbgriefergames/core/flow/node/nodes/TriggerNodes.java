package tmb.randy.tmbgriefergames.core.flow.node.nodes;

import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.number;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.text;

import java.util.Arrays;
import java.util.List;
import net.labymod.api.client.gui.screen.key.Key;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.flow.FlowSettings;
import tmb.randy.tmbgriefergames.core.flow.node.NodeContext;
import tmb.randy.tmbgriefergames.core.flow.FlowEvent.Type;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Category;
import tmb.randy.tmbgriefergames.core.flow.node.NodeTypes;

/**
 * Entry points of a flow. "Start" is used when the flow is started by command or hotkey; all other triggers
 * interrupt the running flow when their event happens (e.g. to empty a full inventory) and can optionally
 * start the flow on their own (autostart).
 */
public final class TriggerNodes {

    private TriggerNodes() {}

    /** True if the pressed key belongs to the combination and all its keys are held down. */
    private static boolean keysMatch(NodeContext context, String pressedKey) {
        Key[] keys = context.text("setting").isBlank()
            ? Arrays.stream(context.text("key").split("\\+")).map(name -> Key.getByName(name.trim().toUpperCase())).filter(key -> key != null).toArray(Key[]::new)
            : FlowSettings.getKeys(context.text("setting").trim());
        if (keys.length == 0)
            return context.text("key").trim().equalsIgnoreCase(pressedKey);
        return Arrays.stream(keys).anyMatch(key -> key.getActualName().equalsIgnoreCase(pressedKey)) && Addon.allKeysPressed(keys);
    }

    public static void register() {
        NodeTypes.register(NodeTypes.START, Category.TRIGGER, List.of(), List.of(NodeType.NEXT), true, context -> NodeType.NEXT);

        NodeTypes.trigger("on_inventory_full", List.of(), (context, event) -> event.type() == Type.INVENTORY_FULL);
        NodeTypes.trigger("on_chat", List.of(text("contains", "")),
            (context, event) -> event.type() == Type.CHAT && NodeTypes.textMatches(event.data(), context.text("contains")));
        NodeTypes.trigger("on_menu_open", List.of(text("title", "")),
            (context, event) -> event.type() == Type.MENU_OPEN && NodeTypes.textMatches(event.data(), context.text("title")));
        NodeTypes.trigger("on_cb_change", List.of(text("cb", "")),
            (context, event) -> event.type() == Type.CB_CHANGE && (context.text("cb").isBlank() || event.data().equalsIgnoreCase(context.text("cb").trim())));
        // Key combinations like "L_SHIFT+K" or the hotkey of an addon setting (e.g. "inventoryDrop.hotkey")
        NodeTypes.trigger("on_key", List.of(text("key", "ESCAPE"), text("setting", "")),
            (context, event) -> event.type() == Type.KEY && keysMatch(context, event.data()));
        NodeTypes.trigger("on_mouse", List.of(text("button", "RIGHT"), text("action", "RELEASE")),
            (context, event) -> event.type() == Type.MOUSE && event.data().equalsIgnoreCase(context.text("button").trim() + "_" + context.text("action").trim()));
        NodeTypes.trigger("on_chat_sent", List.of(text("contains", "")),
            (context, event) -> event.type() == Type.CHAT_SENT && NodeTypes.textMatches(event.data(), context.text("contains")));
        NodeTypes.trigger("on_fish_bite", List.of(), (context, event) -> event.type() == Type.FISH_BITE);
        NodeTypes.trigger("on_timer", List.of(number("ticks", 200)), (context, event) -> {
            int interval = context.number("ticks");
            return event.type() == Type.TICK && interval > 0 && context.runner().getTicksRunning() % interval == 0;
        });
    }
}
