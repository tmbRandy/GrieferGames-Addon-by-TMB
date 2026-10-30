package tmb.randy.tmbgriefergames.core.flow.node.nodes;

import static tmb.randy.tmbgriefergames.core.flow.node.NodeContext.WAIT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeType.NEXT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.FOUND;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.NONE;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.bool;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.number;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.text;

import java.util.ArrayList;
import java.util.List;
import tmb.randy.tmbgriefergames.core.flow.node.NodeContext;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Category;
import tmb.randy.tmbgriefergames.core.flow.node.NodeTypes;
import tmb.randy.tmbgriefergames.core.flow.node.Parameter;
import tmb.randy.tmbgriefergames.core.helper.BaseHelper;

/**
 * Searching, counting, moving and dropping items. Slots are always slot ids of the open container
 * (with only the inventory open: 9-35 storage, 36-44 hotbar).
 *
 * <p>Every block works on an area ("inventory", "chest" or "all"); the optional "slots" parameter
 * (e.g. "0-24") replaces the area by an exact slot range. "min_level"/"max_level" filter by compression level.
 */
public final class InventoryNodes {

    private InventoryNodes() {}

    /** The parameters every slot filtering block has after its own ones. */
    private static List<Parameter> withFilter(Parameter... own) {
        List<Parameter> parameters = new ArrayList<>(List.of(own));
        parameters.add(text("area", "inventory"));
        parameters.add(text("slots", ""));
        parameters.add(number("min_level", 0));
        parameters.add(number("max_level", 7));
        return List.copyOf(parameters);
    }

    public static void register() {
        NodeTypes.register("find_slot", Category.INVENTORY, withFilter(text("item", ""), text("variable", "slot"), bool("full_stack", false)),
            List.of(FOUND, NONE), true, context -> {
                for (int slot : slots(context)) {
                    if (matches(context, slot) && (!context.bool("full_stack") || context.game().getSlotCount(slot) >= 64)) {
                        context.setVariable(context.rawText("variable"), slot);
                        return FOUND;
                    }
                }
                return NONE;
            });

        NodeTypes.action("count_items", Category.INVENTORY, withFilter(text("item", ""), text("variable", "stacks")), context -> {
            int stacks = 0;
            int amount = 0;
            for (int slot : slots(context)) {
                if (matches(context, slot)) {
                    stacks++;
                    amount += context.game().getSlotCount(slot);
                }
            }
            context.setVariable(context.rawText("variable"), stacks);
            context.setVariable(context.rawText("variable") + "_amount", amount);
            return NEXT;
        });

        NodeTypes.register("drop_items", Category.INVENTORY, withFilter(text("item", ""), bool("keep_one", false)), List.of(NEXT), false, context -> {
            if (context.isFirstTick()) {
                boolean keep = context.bool("keep_one");
                for (int slot : slots(context)) {
                    if (!matches(context, slot))
                        continue;
                    if (keep)
                        keep = false;
                    else
                        BaseHelper.dropClick(slot);
                }
            }
            return BaseHelper.isClickQueueEmpty() ? NEXT : WAIT;
        });

        NodeTypes.register("move_items", Category.INVENTORY, withFilter(text("item", ""), number("max_stacks", 0), bool("full_only", false)),
            List.of(NEXT), false, context -> {
                if (context.isFirstTick()) {
                    int maxStacks = context.number("max_stacks");
                    int moved = 0;
                    for (int slot : slots(context)) {
                        if (maxStacks > 0 && moved >= maxStacks)
                            break;
                        if (matches(context, slot) && (!context.bool("full_only") || context.game().getSlotCount(slot) >= 64)) {
                            BaseHelper.shiftClick(slot);
                            moved++;
                        }
                    }
                }
                return BaseHelper.isClickQueueEmpty() ? NEXT : WAIT;
            });

        NodeTypes.condition("if_inventory_full", Category.INVENTORY, List.of(), context -> context.game().isInventoryFull());
        NodeTypes.condition("if_holding", Category.INVENTORY, List.of(text("item", "")), context -> {
            String held = context.game().getHeldItem();
            return held != null && (context.text("item").isBlank() || NodeTypes.itemMatches(held, "", context.text("item")));
        });
        NodeTypes.condition("if_durability", Category.INVENTORY, List.of(number("min", 1)), context -> {
            int durability = context.game().getHeldItemDurability();
            return durability == -1 || durability >= context.number("min");
        });
    }

    private static boolean matches(NodeContext context, int slot) {
        if (!NodeTypes.slotMatches(context, slot, context.text("item")))
            return false;
        int level = context.game().getSlotCompressionLevel(slot);
        return level >= context.number("min_level") && level <= context.number("max_level");
    }

    /** The slots a block works on: the "slots" range if set, otherwise the area (player inventory hotbar first). */
    private static List<Integer> slots(NodeContext context) {
        List<Integer> slots = new ArrayList<>();
        int containerSize = context.game().getContainerSize();
        String range = context.text("slots").trim();

        if (!range.isEmpty()) {
            String[] bounds = range.split("-");
            int from = NodeContext.toNumber(bounds[0]);
            int to = bounds.length > 1 ? NodeContext.toNumber(bounds[1]) : from;
            for (int slot = Math.max(0, from); slot <= Math.min(to, containerSize - 1); slot++)
                slots.add(slot);
            return slots;
        }

        int chestSize = context.game().getChestSize();
        String area = context.text("area").trim().toLowerCase();
        if (area.equals("chest") || area.equals("kiste") || area.startsWith("all")) {
            for (int slot = 0; slot < chestSize; slot++)
                slots.add(slot);
            if (area.equals("chest") || area.equals("kiste"))
                return slots;
        }

        int firstInventorySlot = Math.max(0, containerSize - BaseHelper.INVENTORY_SIZE);
        for (int i = 0; i < BaseHelper.INVENTORY_SIZE; i++)
            slots.add(BaseHelper.toContainerSlot(i, firstInventorySlot));
        return slots;
    }
}
