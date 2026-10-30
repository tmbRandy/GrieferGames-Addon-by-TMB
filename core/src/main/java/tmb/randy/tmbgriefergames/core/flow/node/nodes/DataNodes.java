package tmb.randy.tmbgriefergames.core.flow.node.nodes;

import static tmb.randy.tmbgriefergames.core.flow.node.NodeType.NEXT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.FOUND;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.NONE;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.number;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.text;

import java.util.List;
import tmb.randy.tmbgriefergames.core.flow.node.NodeContext;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Category;
import tmb.randy.tmbgriefergames.core.flow.node.NodeTypes;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;

/**
 * Variables. Every text parameter of every block can use ${name} to insert a variable; these blocks write them.
 * Blocks that store several values use the name as prefix, e.g. "chest" becomes ${chest.x}, ${chest.y}, ${chest.z}.
 */
public final class DataNodes {

    private DataNodes() {}

    public static void register() {
        NodeTypes.register("set_variable", Category.DATA, List.of(text("name", "counter"), text("value", "0")), List.of(NEXT), true, context -> {
            context.setVariable(context.rawText("name"), context.text("value"));
            return NEXT;
        });

        NodeTypes.register("calculate", Category.DATA, List.of(text("name", "counter"), text("operator", "+"), text("value", "1")), List.of(NEXT), true, context -> {
            int current = NodeContext.toNumber(context.variable(context.rawText("name")));
            int value = context.number("value");
            int result = switch (context.text("operator").trim()) {
                case "-" -> current - value;
                case "*" -> current * value;
                case "/" -> value == 0 ? current : current / value;
                case "%" -> value == 0 ? current : current % value;
                case "min" -> Math.min(current, value);
                case "max" -> Math.max(current, value);
                case "=" -> value;
                default -> current + value;
            };
            context.setVariable(context.rawText("name"), result);
            return NEXT;
        });

        NodeTypes.register("store_looked_block", Category.DATA, List.of(text("name", "block")), List.of(FOUND, NONE), true, context -> {
            int[] pos = context.game().getBlockLookingAt();
            if (pos == null)
                return NONE;

            String name = context.rawText("name");
            context.setVariable(name + ".x", pos[0]);
            context.setVariable(name + ".y", pos[1]);
            context.setVariable(name + ".z", pos[2]);
            context.setVariable(name + ".id", context.game().getBlockAt(pos[0], pos[1], pos[2]));
            return FOUND;
        });

        NodeTypes.register("store_slot", Category.DATA, List.of(number("slot", 0), text("name", "item")), List.of(FOUND, NONE), true, context -> {
            int slot = context.number("slot");
            String item = context.game().getSlotItem(slot);
            String name = context.rawText("name");
            context.setVariable(name + ".id", item == null ? "" : item);
            context.setVariable(name + ".name", item == null ? "" : NodeTypes.stripColors(context.game().getSlotItemName(slot)));
            context.setVariable(name + ".count", context.game().getSlotCount(slot));
            context.setVariable(name + ".level", context.game().getSlotCompressionLevel(slot));
            return item == null ? NONE : FOUND;
        });

        NodeTypes.register("store_position", Category.DATA, List.of(text("name", "position")), List.of(NEXT), true, context -> {
            double[] position = context.game().getPlayerPosition();
            String name = context.rawText("name");
            context.setVariable(name + ".x", (int) Math.floor(position[0]));
            context.setVariable(name + ".y", (int) Math.floor(position[1]));
            context.setVariable(name + ".z", (int) Math.floor(position[2]));
            return NEXT;
        });

        NodeTypes.register("store_info", Category.DATA, List.of(text("name", "info")), List.of(NEXT), true, context -> {
            String name = context.rawText("name");
            String title = context.game().getChestTitle();
            context.setVariable(name + ".menu", title == null ? "" : NodeTypes.stripColors(title));
            context.setVariable(name + ".screen", context.game().getScreenType());
            context.setVariable(name + ".cb", CBtracker.getCurrentCB().getName());
            context.setVariable(name + ".hotbar", context.game().getHotbarSlot() + 1);
            String held = context.game().getHeldItem();
            context.setVariable(name + ".held", held == null ? "" : held);
            context.setVariable(name + ".durability", context.game().getHeldItemDurability());
            return NEXT;
        });
    }
}
