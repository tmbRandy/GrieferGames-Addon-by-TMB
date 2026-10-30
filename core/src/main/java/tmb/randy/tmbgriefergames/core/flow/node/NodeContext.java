package tmb.randy.tmbgriefergames.core.flow.node;

import java.util.List;
import java.util.Map;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.IConnect;
import tmb.randy.tmbgriefergames.core.flow.FlowNode;

/** Everything a block needs while it is executed. */
public final class NodeContext {
    /** Returned by a block that is not finished yet; it is executed again in the next tick. */
    public static final String WAIT = null;

    private final FlowNode node;
    private final int ticksInNode;
    private final List<String> chatMessages;
    private final Map<String, Integer> counters;

    public NodeContext(FlowNode node, int ticksInNode, List<String> chatMessages, Map<String, Integer> counters) {
        this.node = node;
        this.ticksInNode = ticksInNode;
        this.chatMessages = chatMessages;
        this.counters = counters;
    }

    public IConnect game() {
        return Addon.getConnection();
    }

    /** True in the first tick the flow is at this block. */
    public boolean isFirstTick() {
        return ticksInNode == 0;
    }

    /** Ticks since the flow reached this block. */
    public int ticksInNode() {
        return ticksInNode;
    }

    /** Chat messages received since the flow reached this block. */
    public List<String> chatMessages() {
        return chatMessages;
    }

    public String text(String key) {
        return node.getParam(key);
    }

    public int number(String key) {
        try {
            return Integer.parseInt(node.getParam(key).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean bool(String key) {
        return Boolean.parseBoolean(node.getParam(key).trim());
    }

    /** A counter of this block that survives while the flow runs (e.g. loop iterations). */
    public int counter() {
        return counters.getOrDefault(node.getId(), 0);
    }

    public void setCounter(int value) {
        counters.put(node.getId(), value);
    }
}
