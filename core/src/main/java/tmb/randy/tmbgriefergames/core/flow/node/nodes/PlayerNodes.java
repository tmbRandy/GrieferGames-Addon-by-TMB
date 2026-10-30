package tmb.randy.tmbgriefergames.core.flow.node.nodes;

import static tmb.randy.tmbgriefergames.core.flow.node.NodeContext.WAIT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeType.NEXT;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.bool;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.number;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.text;

import java.util.List;
import net.labymod.api.Laby;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Category;
import tmb.randy.tmbgriefergames.core.flow.node.NodeTypes;
import tmb.randy.tmbgriefergames.core.helper.Commander;

/** Chat, movement and interaction with the world. */
public final class PlayerNodes {

    private PlayerNodes() {}

    public static void register() {
        // Chat
        NodeTypes.action("command", Category.ACTION, List.of(text("command", "/p h")), context -> {
            Commander.queue(context.text("command"));
            return NEXT;
        });
        NodeTypes.action("chat", Category.ACTION, List.of(text("text", "")), context -> {
            Laby.labyAPI().minecraft().chatExecutor().chat(context.text("text"));
            return NEXT;
        });
        NodeTypes.action("message", Category.ACTION, List.of(text("text", "Hallo!")), context -> {
            Addon.displayNotification(context.text("text"));
            return NEXT;
        });

        // Looking
        NodeTypes.action("look_at", Category.PLAYER, List.of(number("x", 0), number("y", 64), number("z", 0)), context -> {
            context.game().lookAtBlock(context.number("x"), context.number("y"), context.number("z"));
            return NEXT;
        });
        NodeTypes.action("look_direction", Category.PLAYER, List.of(number("yaw", 0), number("pitch", 0)), context -> {
            context.game().setRotation(context.number("yaw"), context.number("pitch"));
            return NEXT;
        });

        // Interaction
        NodeTypes.action("right_click_block", Category.PLAYER, List.of(), context -> {
            context.game().rightClickBlockLookingAt();
            return NEXT;
        });
        NodeTypes.action("left_click_block", Category.PLAYER, List.of(), context -> {
            context.game().leftClickBlockLookingAt();
            return NEXT;
        });
        NodeTypes.action("break_block", Category.PLAYER, List.of(number("ticks", 20)), context -> {
            if (context.isFirstTick())
                context.game().leftClickBlockLookingAt();
            else
                context.game().breakBlockLookingAt();
            return context.ticksInNode() >= context.number("ticks") ? NEXT : WAIT;
        });
        NodeTypes.action("use_item", Category.PLAYER, List.of(), context -> {
            context.game().useHeldItem();
            return NEXT;
        });
        NodeTypes.action("release_item", Category.PLAYER, List.of(), context -> {
            context.game().releaseUseItem();
            return NEXT;
        });
        NodeTypes.action("select_slot", Category.PLAYER, List.of(number("slot", 1)), context -> {
            context.game().changeSlot(Math.max(0, Math.min(8, context.number("slot") - 1)));
            return NEXT;
        });
        NodeTypes.action("hold_key", Category.PLAYER, List.of(text("key", "sneak"), bool("pressed", true)), context -> {
            context.game().setKeyPressed(context.text("key").trim(), context.bool("pressed"));
            return NEXT;
        });
    }
}
