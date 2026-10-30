package tmb.randy.tmbgriefergames.core.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tmb.randy.tmbgriefergames.core.flow.node.NodeContext;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType;
import tmb.randy.tmbgriefergames.core.flow.node.NodeTypes;

/** Executes one flow, block by block, driven by the game tick. */
public class FlowRunner {
    /** Protection against loops made only of instant blocks. */
    private static final int MAX_INSTANT_NODES_PER_TICK = 50;

    private final Flow flow;
    private final Map<String, Integer> counters = new HashMap<>();
    private final List<String> chatMessages = new ArrayList<>();
    private FlowNode current;
    private int ticksInNode;

    public FlowRunner(Flow flow) {
        this.flow = flow;
        this.current = flow.getNodes().stream()
            .filter(node -> NodeTypes.START.equals(node.getType() == null ? null : node.getType().id()))
            .findFirst()
            .orElse(null);
    }

    public Flow getFlow() {
        return flow;
    }

    public boolean isFinished() {
        return current == null;
    }

    public void stop() {
        current = null;
    }

    public void onChatMessage(String message) {
        chatMessages.add(message);
    }

    /** Runs the current block; instant blocks are chained within the same tick. */
    public void tick() {
        for (int i = 0; i < MAX_INSTANT_NODES_PER_TICK && current != null; i++) {
            NodeType type = current.getType();
            if (type == null) {
                stop();
                return;
            }

            String output = type.executor().execute(new NodeContext(current, ticksInNode, chatMessages, counters));
            if (output == NodeContext.WAIT) {
                ticksInNode++;
                return;
            }

            current = flow.getTarget(current, output);
            ticksInNode = 0;
            chatMessages.clear();

            if (!type.instant())
                return;
        }
    }
}
