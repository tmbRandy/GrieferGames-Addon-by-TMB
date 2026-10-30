package tmb.randy.tmbgriefergames.core.flow.node.nodes;

import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.number;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.text;

import java.util.List;
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

    public static void register() {
        NodeTypes.register(NodeTypes.START, Category.TRIGGER, List.of(), List.of(NodeType.NEXT), true, context -> NodeType.NEXT);

        NodeTypes.trigger("on_inventory_full", List.of(), (context, event) -> event.type() == Type.INVENTORY_FULL);
        NodeTypes.trigger("on_chat", List.of(text("contains", "")),
            (context, event) -> event.type() == Type.CHAT && NodeTypes.textMatches(event.data(), context.text("contains")));
        NodeTypes.trigger("on_menu_open", List.of(text("title", "")),
            (context, event) -> event.type() == Type.MENU_OPEN && NodeTypes.textMatches(event.data(), context.text("title")));
        NodeTypes.trigger("on_cb_change", List.of(text("cb", "")),
            (context, event) -> event.type() == Type.CB_CHANGE && (context.text("cb").isBlank() || event.data().equalsIgnoreCase(context.text("cb").trim())));
        NodeTypes.trigger("on_key", List.of(text("key", "ESCAPE")),
            (context, event) -> event.type() == Type.KEY && event.data().equalsIgnoreCase(context.text("key").trim()));
        NodeTypes.trigger("on_fish_bite", List.of(), (context, event) -> event.type() == Type.FISH_BITE);
        NodeTypes.trigger("on_timer", List.of(number("ticks", 200)), (context, event) -> {
            int interval = context.number("ticks");
            return event.type() == Type.TICK && interval > 0 && context.runner().getTicksRunning() % interval == 0;
        });
    }
}
