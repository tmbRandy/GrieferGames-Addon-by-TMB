package tmb.randy.tmbgriefergames.core.flow.node;

import static tmb.randy.tmbgriefergames.core.flow.node.NodeContext.WAIT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeType.NEXT;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.bool;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.number;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.text;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Category;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Executor;
import tmb.randy.tmbgriefergames.core.helper.BaseHelper;
import tmb.randy.tmbgriefergames.core.helper.Commander;

/** Registry of all block types. New blocks only have to be registered here. */
public final class NodeTypes {
    public static final String START = "start";

    private static final String YES = "yes";
    private static final String NO = "no";
    private static final String OK = "ok";
    private static final String TIMEOUT = "timeout";
    private static final String LOOP = "loop";
    private static final String DONE = "done";

    private static final Map<String, NodeType> TYPES = new LinkedHashMap<>();

    static {
        register(START, Category.TRIGGER, List.of(), List.of(NEXT), true, context -> NEXT);

        // Actions in the world
        action("command", List.of(text("command", "/p h")), context -> {
            Commander.queue(context.text("command"));
            return NEXT;
        });
        action("message", List.of(text("text", "Hallo!")), context -> {
            Addon.displayNotification(context.text("text"));
            return NEXT;
        });
        action("look_at", List.of(number("x", 0), number("y", 64), number("z", 0)), context -> {
            context.game().lookAtBlock(context.number("x"), context.number("y"), context.number("z"));
            return NEXT;
        });
        action("right_click_block", List.of(), context -> {
            context.game().rightClickBlockLookingAt();
            return NEXT;
        });
        action("use_item", List.of(), context -> {
            context.game().useHeldItem();
            return NEXT;
        });
        action("select_slot", List.of(number("slot", 1)), context -> {
            context.game().changeSlot(Math.max(0, Math.min(8, context.number("slot") - 1)));
            return NEXT;
        });

        // Menus
        register("click_slot", Category.MENU, List.of(number("slot", 0), bool("shift", true)), List.of(NEXT), false, context -> {
            if (context.isFirstTick()) {
                if (context.bool("shift"))
                    BaseHelper.shiftClick(context.number("slot"));
                else
                    BaseHelper.click(context.number("slot"));
            }
            return BaseHelper.isClickQueueEmpty() ? NEXT : WAIT;
        });
        register("close_menu", Category.MENU, List.of(), List.of(NEXT), false, context -> {
            context.game().closeScreen();
            return NEXT;
        });
        register("wait_for_menu", Category.MENU, List.of(text("title", ""), number("timeout", 100)), List.of(OK, TIMEOUT), false, context -> {
            if (titleMatches(context.game().getChestTitle(), context.text("title")))
                return OK;
            return context.ticksInNode() >= context.number("timeout") ? TIMEOUT : WAIT;
        });
        register("if_menu", Category.MENU, List.of(text("title", "")), List.of(YES, NO), true,
            context -> titleMatches(context.game().getChestTitle(), context.text("title")) ? YES : NO);
        register("if_slot", Category.MENU, List.of(number("slot", 0), text("item", "")), List.of(YES, NO), true,
            context -> slotMatches(context, context.number("slot"), context.text("item")) ? YES : NO);

        // Logic
        register("wait", Category.LOGIC, List.of(number("ticks", 20)), List.of(NEXT), false,
            context -> context.ticksInNode() >= context.number("ticks") ? NEXT : WAIT);
        register("wait_for_chat", Category.LOGIC, List.of(text("contains", ""), number("timeout", 200)), List.of(OK, TIMEOUT), false, context -> {
            String expected = context.text("contains").toLowerCase();
            for (String message : context.chatMessages()) {
                if (message.toLowerCase().contains(expected))
                    return OK;
            }
            return context.ticksInNode() >= context.number("timeout") ? TIMEOUT : WAIT;
        });
        register("if_inventory_full", Category.LOGIC, List.of(), List.of(YES, NO), true,
            context -> context.game().isInventoryFull() ? YES : NO);
        register("repeat", Category.LOGIC, List.of(number("count", 3)), List.of(LOOP, DONE), true, context -> {
            int done = context.counter();
            if (done < context.number("count")) {
                context.setCounter(done + 1);
                return LOOP;
            }
            context.setCounter(0);
            return DONE;
        });
        register("stop", Category.LOGIC, List.of(), List.of(), true, context -> DONE);
    }

    private NodeTypes() {}

    private static void action(String id, List<Parameter> parameters, Executor executor) {
        register(id, Category.ACTION, parameters, List.of(NEXT), false, executor);
    }

    private static void register(String id, Category category, List<Parameter> parameters, List<String> outputs, boolean instant, Executor executor) {
        TYPES.put(id, new NodeType(id, category, parameters, outputs, instant, executor));
    }

    public static NodeType get(String id) {
        return TYPES.get(id);
    }

    public static Collection<NodeType> all() {
        return Collections.unmodifiableCollection(TYPES.values());
    }

    /** Case insensitive "contains" without color codes, so players can type titles without the § codes. An empty title matches every chest. */
    static boolean titleMatches(String title, String expected) {
        if (title == null)
            return false;
        return expected.isBlank() || stripColors(title).toLowerCase().contains(stripColors(expected).toLowerCase().trim());
    }

    /** Matches the registry name ("stone" or "minecraft:stone") or a part of the display name. An empty item matches every non empty slot. */
    private static boolean slotMatches(NodeContext context, int slot, String expected) {
        String item = context.game().getSlotItem(slot);
        if (item == null)
            return false;
        if (expected.isBlank())
            return true;

        String lowerCase = expected.toLowerCase().trim();
        String name = context.game().getSlotItemName(slot);
        return item.equals(lowerCase) || item.equals("minecraft:" + lowerCase) || (name != null && stripColors(name).toLowerCase().contains(lowerCase));
    }

    private static String stripColors(String text) {
        return text.replaceAll("§.", "");
    }
}
