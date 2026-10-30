package tmb.randy.tmbgriefergames.core.flow.node.nodes;

import static tmb.randy.tmbgriefergames.core.flow.node.NodeContext.WAIT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeType.NEXT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.OK;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.TIMEOUT;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.bool;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.number;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.text;

import java.util.List;
import tmb.randy.tmbgriefergames.core.flow.node.NodeContext;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Category;
import tmb.randy.tmbgriefergames.core.flow.node.NodeTypes;
import tmb.randy.tmbgriefergames.core.helper.BaseHelper;
import tmb.randy.tmbgriefergames.core.menu.RezepteMenu;

/** Opening, reading and clicking menus (chest GUIs). */
public final class MenuNodes {
    private static final String REACHED = "reached";

    private MenuNodes() {}

    public static void register() {
        NodeTypes.register("click_slot", Category.MENU, List.of(number("slot", 0), bool("shift", true)), List.of(NEXT), false, context -> {
            if (context.isFirstTick()) {
                if (context.bool("shift"))
                    BaseHelper.shiftClick(context.number("slot"));
                else
                    BaseHelper.click(context.number("slot"));
            }
            return BaseHelper.isClickQueueEmpty() ? NEXT : WAIT;
        });
        NodeTypes.register("drop_slot", Category.MENU, List.of(number("slot", 0)), List.of(NEXT), false, context -> {
            if (context.isFirstTick())
                BaseHelper.dropClick(context.number("slot"));
            return BaseHelper.isClickQueueEmpty() ? NEXT : WAIT;
        });
        NodeTypes.register("wait_for_clicks", Category.MENU, List.of(), List.of(NEXT), false,
            context -> BaseHelper.isClickQueueEmpty() ? NEXT : WAIT);
        NodeTypes.action("close_menu", Category.MENU, List.of(), context -> {
            context.game().closeScreen();
            return NEXT;
        });
        NodeTypes.action("open_inventory", Category.MENU, List.of(), context -> {
            context.game().openInventory();
            return NEXT;
        });

        NodeTypes.register("wait_for_menu", Category.MENU, List.of(text("title", ""), number("timeout", 100)), List.of(OK, TIMEOUT), false, context -> {
            if (NodeTypes.textMatches(context.game().getChestTitle(), context.text("title")))
                return OK;
            return context.ticksInNode() >= context.number("timeout") ? TIMEOUT : WAIT;
        });
        NodeTypes.condition("if_menu", Category.MENU, List.of(text("title", "")),
            context -> NodeTypes.textMatches(context.game().getChestTitle(), context.text("title")));
        NodeTypes.condition("if_screen", Category.MENU, List.of(text("screen", "inventory")),
            context -> context.game().getScreenType().equalsIgnoreCase(context.text("screen").trim()));
        NodeTypes.condition("if_slot", Category.MENU, List.of(number("slot", 0), text("item", "")),
            context -> NodeTypes.slotMatches(context, context.number("slot"), context.text("item")));

        // Paged menus like the compression levels or the recipe variants of /rezepte
        NodeTypes.register("navigate_page", Category.MENU, List.of(
            number("indicator", RezepteMenu.PAGE_INDICATOR), text("prefix", "Komprimierungsstufe "), number("target", 1),
            number("previous", RezepteMenu.PREVIOUS_PAGE), number("next", RezepteMenu.NEXT_PAGE), number("timeout", 200)
        ), List.of(REACHED, TIMEOUT), false, MenuNodes::navigatePage);
    }

    private static String navigatePage(NodeContext context) {
        if (context.ticksInNode() >= context.number("timeout"))
            return TIMEOUT;
        if (!BaseHelper.isClickQueueEmpty())
            return WAIT;

        String indicator = NodeTypes.stripColors(context.game().getSlotItemName(context.number("indicator")));
        String prefix = NodeTypes.stripColors(context.text("prefix"));
        int page = indicator.startsWith(prefix) ? BaseHelper.parseNumberAfter(indicator, prefix) : -1;
        if (page < 0)
            return WAIT;

        int target = context.number("target");
        if (page == target)
            return REACHED;

        BaseHelper.shiftClick(page < target ? context.number("next") : context.number("previous"));
        return WAIT;
    }
}
