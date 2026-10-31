package tmb.randy.tmbgriefergames.core.flow.node.nodes;

import static tmb.randy.tmbgriefergames.core.flow.node.NodeContext.WAIT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeType.NEXT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.OK;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.TIMEOUT;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.bool;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.number;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.text;

import java.util.List;
import net.labymod.api.Laby;
import net.labymod.api.client.scoreboard.Scoreboard;
import net.labymod.api.client.scoreboard.ScoreboardTeam;
import tmb.randy.tmbgriefergames.api.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.flow.Flow;
import tmb.randy.tmbgriefergames.core.flow.FlowManager;
import tmb.randy.tmbgriefergames.core.flow.node.NodeContext;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType.Category;
import tmb.randy.tmbgriefergames.core.flow.node.NodeTypes;

/** Waiting, branching, loops and controlling other flows. */
public final class LogicNodes {
    private static final String LOOP = "loop";
    private static final String DONE = "done";

    private LogicNodes() {}

    public static void register() {
        NodeTypes.action("wait", Category.LOGIC, List.of(number("ticks", 20)),
            context -> context.ticksInNode() >= context.number("ticks") ? NEXT : WAIT);

        NodeTypes.register("wait_for_chat", Category.LOGIC, List.of(text("contains", ""), number("timeout", 200), text("variable", "")), List.of(OK, TIMEOUT), false, context -> {
            for (String message : context.chatMessages()) {
                if (NodeTypes.textMatches(message, context.text("contains"))) {
                    context.setVariable(context.rawText("variable"), message);
                    return OK;
                }
            }
            return context.ticksInNode() >= context.number("timeout") ? TIMEOUT : WAIT;
        });

        NodeTypes.register("repeat", Category.LOGIC, List.of(number("count", 3)), List.of(LOOP, DONE), true, context -> {
            int done = context.counter();
            if (done < context.number("count")) {
                context.setCounter(done + 1);
                return LOOP;
            }
            context.setCounter(0);
            return DONE;
        });

        NodeTypes.condition("if_compare", Category.LOGIC, List.of(text("a", "${slot}"), text("operator", "="), text("b", "0")),
            context -> compare(context.text("a"), context.text("operator").trim(), context.text("b")));

        // Starts a built-in flow, an own flow or one of the classic addon functions (e.g. CRAFTV3)
        // Checks the scoreboard teams, which contain every player of the current CB
        NodeTypes.condition("if_player_online", Category.LOGIC, List.of(text("player", "${arg1}")), context -> {
            Scoreboard scoreboard = Laby.labyAPI().minecraft().getScoreboard();
            String player = context.text("player").trim();
            if (scoreboard == null || player.isEmpty())
                return false;
            for (ScoreboardTeam team : scoreboard.getTeams()) {
                for (String entry : team.getEntries()) {
                    if (entry.equalsIgnoreCase(player))
                        return true;
                }
            }
            return false;
        });

        NodeTypes.action("run_flow", Category.LOGIC, List.of(text("flow", ""), bool("wait", false)), context -> {
            String name = context.text("flow").trim();
            if (context.isFirstTick())
                start(name);
            return context.bool("wait") && isRunning(name) ? WAIT : NEXT;
        });
        NodeTypes.action("stop_flow", Category.LOGIC, List.of(text("flow", "")), context -> {
            String name = context.text("flow").trim();
            Flow flow = FlowManager.get().getFlow(name);
            if (flow != null)
                FlowManager.get().stop(flow.getName());
            else if (findFunction(name) instanceof ActiveFunction function)
                function.stop();
            return NEXT;
        });
        NodeTypes.register("stop", Category.LOGIC, List.of(), List.of(), true, context -> DONE);
    }

    private static void start(String name) {
        Flow flow = FlowManager.get().getFlow(name);
        if (flow != null) {
            if (!FlowManager.get().isRunning(flow.getName()))
                FlowManager.get().start(flow);
        } else if (findFunction(name) instanceof ActiveFunction function) {
            function.start();
        } else {
            Addon.displayNotification(Addon.translate("flow.unknown", name));
        }
    }

    private static boolean isRunning(String name) {
        Flow flow = FlowManager.get().getFlow(name);
        if (flow != null)
            return FlowManager.get().isRunning(flow.getName());
        return findFunction(name) instanceof ActiveFunction function && function.isEnabled();
    }

    /** A classic addon function by its id (e.g. "CRAFTV3") or its displayed name. */
    private static ActiveFunction findFunction(String name) {
        for (Functions function : Functions.values()) {
            if (function.name().equalsIgnoreCase(name) || function.getLocalizedName().equalsIgnoreCase(name))
                return Addon.getActiveFunction(function.name());
        }
        return null;
    }

    /** Compares numerically if both sides are numbers, otherwise as text. */
    private static boolean compare(String a, String operator, String b) {
        boolean numeric = a.trim().matches("-?\\d+([.,]\\d+)?") && b.trim().matches("-?\\d+([.,]\\d+)?");
        int comparison = numeric ? Integer.compare(NodeContext.toNumber(a), NodeContext.toNumber(b)) : a.trim().compareToIgnoreCase(b.trim());

        return switch (operator) {
            case "!=", "<>" -> comparison != 0;
            case "<" -> comparison < 0;
            case ">" -> comparison > 0;
            case "<=" -> comparison <= 0;
            case ">=" -> comparison >= 0;
            case "contains", "enthält" -> a.toLowerCase().contains(b.toLowerCase());
            default -> comparison == 0;
        };
    }
}
