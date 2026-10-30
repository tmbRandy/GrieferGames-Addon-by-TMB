package tmb.randy.tmbgriefergames.core.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tmb.randy.tmbgriefergames.core.flow.node.NodeContext;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType;

/**
 * Executes one flow, block by block, driven by the game tick. Trigger blocks of the flow interrupt the
 * current position when their event happens and continue the flow from the trigger.
 */
public class FlowRunner {
    /** Protection against loops made only of instant blocks. */
    private static final int MAX_INSTANT_NODES_PER_TICK = 50;
    /** Variable that holds the data of the last event that fired a trigger (e.g. the chat message). */
    public static final String EVENT_VARIABLE = "event";

    private final Flow flow;
    private final Map<String, Integer> counters = new HashMap<>();
    private final Map<String, String> variables = new HashMap<>();
    private final List<String> chatMessages = new ArrayList<>();
    private FlowNode current;
    private int ticksInNode;
    private int ticksRunning;

    /** Starts the flow at the given trigger block (normally the start block). */
    public FlowRunner(Flow flow, FlowNode entry) {
        this.flow = flow;
        this.current = entry == null ? null : flow.getTarget(entry, NodeType.NEXT);
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

    public FlowNode getCurrent() {
        return current;
    }

    public int getTicksInNode() {
        return ticksInNode;
    }

    public int getTicksRunning() {
        return ticksRunning;
    }

    public List<String> getChatMessages() {
        return chatMessages;
    }

    public String getVariable(String name) {
        return variables.getOrDefault(name, "");
    }

    public void setVariable(String name, String value) {
        variables.put(name, value);
    }

    public int getCounter(String nodeId) {
        return counters.getOrDefault(nodeId, 0);
    }

    public void setCounter(String nodeId, int value) {
        counters.put(nodeId, value);
    }

    public void onChatMessage(String message) {
        chatMessages.add(message);
    }

    /** Fires all trigger blocks of this flow that react to the event. Returns true if one fired. */
    public boolean handleEvent(FlowEvent event) {
        for (FlowNode node : flow.getNodes()) {
            NodeType type = node.getType();
            if (type != null && type.trigger() != null && type.trigger().matches(new NodeContext(this, node), event)) {
                if (event.data() != null)
                    setVariable(EVENT_VARIABLE, event.data());
                jumpTo(flow.getTarget(node, NodeType.NEXT));
                return true;
            }
        }
        return false;
    }

    private void jumpTo(FlowNode node) {
        current = node;
        ticksInNode = 0;
        chatMessages.clear();
    }

    /** Runs the current block; instant blocks are chained within the same tick. */
    public void tick() {
        ticksRunning++;

        for (int i = 0; i < MAX_INSTANT_NODES_PER_TICK && current != null; i++) {
            NodeType type = current.getType();
            if (type == null) {
                stop();
                return;
            }

            String output = type.executor().execute(new NodeContext(this, current));
            if (output == NodeContext.WAIT) {
                ticksInNode++;
                return;
            }

            jumpTo(flow.getTarget(current, output));
            if (!type.instant())
                return;
        }
    }
}
