package tmb.randy.tmbgriefergames.core.flow.node;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.IConnect;
import tmb.randy.tmbgriefergames.core.flow.FlowNode;
import tmb.randy.tmbgriefergames.core.flow.FlowRunner;

/** Everything a block needs while it is executed. */
public final class NodeContext {
    /** Returned by a block that is not finished yet; it is executed again in the next tick. */
    public static final String WAIT = null;

    private static final Pattern VARIABLE = Pattern.compile("\\$\\{([^}]+)}");

    private final FlowRunner runner;
    private final FlowNode node;

    public NodeContext(FlowRunner runner, FlowNode node) {
        this.runner = runner;
        this.node = node;
    }

    public IConnect game() {
        return Addon.getConnection();
    }

    public FlowRunner runner() {
        return runner;
    }

    /** True in the first tick the flow is at this block. */
    public boolean isFirstTick() {
        return runner.getTicksInNode() == 0;
    }

    /** Ticks since the flow reached this block. */
    public int ticksInNode() {
        return runner.getTicksInNode();
    }

    /** Chat messages received since the flow reached this block. */
    public List<String> chatMessages() {
        return runner.getChatMessages();
    }

    /** A parameter with all ${variable} placeholders replaced. */
    public String text(String key) {
        return resolve(node.getParam(key));
    }

    /** The parameter without replacing placeholders (e.g. the name of a variable to write). */
    public String rawText(String key) {
        return node.getParam(key).trim();
    }

    public int number(String key) {
        return toNumber(text(key));
    }

    public boolean bool(String key) {
        return Boolean.parseBoolean(text(key).trim());
    }

    public String variable(String name) {
        return runner.getVariable(name);
    }

    public void setVariable(String name, Object value) {
        if (!name.isBlank())
            runner.setVariable(name, String.valueOf(value));
    }

    /** A counter of this block that survives while the flow runs (e.g. loop iterations). */
    public int counter() {
        return runner.getCounter(node.getId());
    }

    public void setCounter(int value) {
        runner.setCounter(node.getId(), value);
    }

    public String resolve(String text) {
        Matcher matcher = VARIABLE.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find())
            matcher.appendReplacement(result, Matcher.quoteReplacement(runner.getVariable(matcher.group(1).trim())));
        matcher.appendTail(result);
        return result.toString();
    }

    public static int toNumber(String text) {
        try {
            return (int) Math.round(Double.parseDouble(text.trim().replace(',', '.')));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
