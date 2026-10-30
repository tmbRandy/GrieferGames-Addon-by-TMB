package tmb.randy.tmbgriefergames.core.flow.node.nodes;

import static tmb.randy.tmbgriefergames.core.flow.node.NodeContext.WAIT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeType.NEXT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.FOUND;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.NONE;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.bool;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.number;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.text;

import java.util.List;
import tmb.randy.tmbgriefergames.core.flow.node.NodeContext;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Category;
import tmb.randy.tmbgriefergames.core.flow.node.NodeTypes;
import tmb.randy.tmbgriefergames.core.helper.BaseHelper;

/**
 * Searching, counting, moving and dropping items. Slots are always slot ids of the open container
 * (with only the inventory open: 9-35 storage, 36-44 hotbar).
 */
public final class InventoryNodes {

    /** Part of the open container a block works on. */
    private enum Area {
        CHEST, INVENTORY, ALL;

        static Area of(String text) {
            return switch (text.trim().toLowerCase()) {
                case "chest", "kiste" -> CHEST;
                case "all", "alle", "alles" -> ALL;
                default -> INVENTORY;
            };
        }

        int from(NodeContext context) {
            return this == INVENTORY ? Math.max(0, context.game().getContainerSize() - BaseHelper.INVENTORY_SIZE) : 0;
        }

        int to(NodeContext context) {
            return this == CHEST ? context.game().getChestSize() : context.game().getContainerSize();
        }
    }

    private InventoryNodes() {}

    public static void register() {
        NodeTypes.register("find_slot", Category.INVENTORY, List.of(
            text("item", ""), text("area", "inventory"), text("variable", "slot"), bool("full_stack", false), number("max_level", 7)
        ), List.of(FOUND, NONE), true, context -> {
            Area area = Area.of(context.text("area"));
            for (int slot = area.from(context); slot < area.to(context); slot++) {
                if (matches(context, slot) && (!context.bool("full_stack") || context.game().getSlotCount(slot) >= 64)) {
                    context.setVariable(context.rawText("variable"), slot);
                    return FOUND;
                }
            }
            return NONE;
        });

        NodeTypes.action("count_items", Category.INVENTORY, List.of(text("item", ""), text("area", "inventory"), text("variable", "stacks")), context -> {
            Area area = Area.of(context.text("area"));
            int stacks = 0;
            int amount = 0;
            for (int slot = area.from(context); slot < area.to(context); slot++) {
                if (NodeTypes.slotMatches(context, slot, context.text("item"))) {
                    stacks++;
                    amount += context.game().getSlotCount(slot);
                }
            }
            context.setVariable(context.rawText("variable"), stacks);
            context.setVariable(context.rawText("variable") + "_amount", amount);
            return NEXT;
        });

        NodeTypes.register("drop_items", Category.INVENTORY, List.of(text("item", ""), bool("keep_one", false)), List.of(NEXT), false, context -> {
            if (context.isFirstTick()) {
                boolean keep = context.bool("keep_one");
                for (int slot : inventoryHotbarFirst(context)) {
                    if (NodeTypes.slotMatches(context, slot, context.text("item"))) {
                        if (keep)
                            keep = false;
                        else
                            BaseHelper.dropClick(slot);
                    }
                }
            }
            return BaseHelper.isClickQueueEmpty() ? NEXT : WAIT;
        });

        NodeTypes.register("move_items", Category.INVENTORY, List.of(
            text("item", ""), text("from", "chest"), number("max_stacks", 0), bool("full_only", false)
        ), List.of(NEXT), false, context -> {
            if (context.isFirstTick()) {
                Area area = Area.of(context.text("from"));
                int maxStacks = context.number("max_stacks");
                int moved = 0;
                for (int slot = area.from(context); slot < area.to(context) && (maxStacks <= 0 || moved < maxStacks); slot++) {
                    if (NodeTypes.slotMatches(context, slot, context.text("item")) && (!context.bool("full_only") || context.game().getSlotCount(slot) >= 64)) {
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
        return NodeTypes.slotMatches(context, slot, context.text("item")) && context.game().getSlotCompressionLevel(slot) <= context.number("max_level");
    }

    /** Slots of the player inventory part, hotbar first. */
    private static int[] inventoryHotbarFirst(NodeContext context) {
        int first = Math.max(0, context.game().getContainerSize() - BaseHelper.INVENTORY_SIZE);
        int[] slots = new int[BaseHelper.INVENTORY_SIZE];
        for (int i = 0; i < BaseHelper.INVENTORY_SIZE; i++)
            slots[i] = BaseHelper.toContainerSlot(i, first);
        return slots;
    }
}
