package tmb.randy.tmbgriefergames.core.flow.node.nodes;

import static tmb.randy.tmbgriefergames.core.flow.node.NodeContext.WAIT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeType.NEXT;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.OK;
import static tmb.randy.tmbgriefergames.core.flow.node.NodeTypes.TIMEOUT;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.number;
import static tmb.randy.tmbgriefergames.core.flow.node.Parameter.text;

import java.util.List;
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

        NodeTypes.action("run_flow", Category.LOGIC, List.of(text("flow", "")), context -> {
            Flow flow = FlowManager.get().getFlow(context.text("flow").trim());
            if (flow != null && !FlowManager.get().isRunning(flow.getName()))
                FlowManager.get().start(flow);
            return NEXT;
        });
        NodeTypes.action("stop_flow", Category.LOGIC, List.of(text("flow", "")), context -> {
            FlowManager.get().stop(context.text("flow").trim());
            return NEXT;
        });
        NodeTypes.register("stop", Category.LOGIC, List.of(), List.of(), true, context -> DONE);
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
