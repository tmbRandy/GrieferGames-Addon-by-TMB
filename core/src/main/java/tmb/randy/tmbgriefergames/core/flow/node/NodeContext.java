package tmb.randy.tmbgriefergames.core.flow.node;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.IConnect;
import tmb.randy.tmbgriefergames.core.flow.FlowNode;
import tmb.randy.tmbgriefergames.core.flow.FlowRunner;
import tmb.randy.tmbgriefergames.core.flow.FlowSettings;

/** Everything a block needs while it is executed. */
public final class NodeContext {
    /** Returned by a block that is not finished yet; it is executed again in the next tick. */
    public static final String WAIT = null;

    private static final String TIME_NOW = "time.now";
    /** Only placeholders without other placeholders inside, so nested ones are resolved from the inside out. */
    private static final Pattern VARIABLE = Pattern.compile("\\$\\{([^${}]+)}");
    private static final int MAX_NESTING = 5;

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

    /** Clicks are only sent while an inventory, chest or crafting table is open; without one they are skipped. */
    public boolean canClick() {
        String screen = game().getScreenType();
        return screen.equals("inventory") || screen.equals("chest") || screen.equals("crafting");
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

    /**
     * Replaces all ${name} placeholders. Inner placeholders are replaced first, so ${cb.${index}} reads the
     * variable whose name contains the value of ${index}.
     */
    public String resolve(String text) {
        String current = text;
        for (int depth = 0; depth < MAX_NESTING && current.contains("${"); depth++) {
            Matcher matcher = VARIABLE.matcher(current);
            StringBuilder result = new StringBuilder();
            while (matcher.find())
                matcher.appendReplacement(result, Matcher.quoteReplacement(lookup(matcher.group(1).trim())));
            matcher.appendTail(result);

            if (result.toString().equals(current))
                break;
            current = result.toString();
        }
        return current;
    }

    /** A variable of the flow, one of the addon settings ("setting.x") or the current time in seconds ("time.now"). */
    private String lookup(String name) {
        if (name.equals(TIME_NOW))
            return String.valueOf(System.currentTimeMillis() / 1000);
        return name.startsWith(FlowSettings.PREFIX) ? FlowSettings.get(name.substring(FlowSettings.PREFIX.length())) : runner.getVariable(name);
    }

    public static int toNumber(String text) {
        try {
            return (int) Math.round(Double.parseDouble(text.trim().replace(',', '.')));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
