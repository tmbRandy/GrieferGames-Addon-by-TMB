package tmb.randy.tmbgriefergames.core.flow.node;

import java.util.List;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.flow.FlowEvent;

/**
 * Describes a kind of block: its parameters, its outputs and what it does when the flow reaches it.
 *
 * @param instant true for pure logic blocks, the flow continues with the next block in the same tick
 * @param trigger only for trigger blocks: decides whether an event fires the block, null for all other blocks
 */
public record NodeType(String id, Category category, List<Parameter> parameters, List<String> outputs, boolean instant, Executor executor, Trigger trigger) {

    /** Output name used by all blocks that simply continue. */
    public static final String NEXT = "next";
    /** Parameter of every trigger block: start the flow when the event happens while it is not running. */
    public static final String AUTOSTART = "autostart";

    public enum Category {
        TRIGGER(0xFF2E7D32),
        ACTION(0xFF1565C0),
        PLAYER(0xFF00838F),
        MENU(0xFF6A1B9A),
        INVENTORY(0xFFAD1457),
        LOGIC(0xFFE65100),
        DATA(0xFF5D4037);

        private final int color;

        Category(int color) {
            this.color = color;
        }

        public int color() {
            return color;
        }
    }

    @FunctionalInterface
    public interface Executor {
        /** Runs the block. Returns the name of the output to continue with, or {@link NodeContext#WAIT} to run again next tick. */
        String execute(NodeContext context);
    }

    @FunctionalInterface
    public interface Trigger {
        boolean matches(NodeContext context, FlowEvent event);
    }

    public boolean isTrigger() {
        return category == Category.TRIGGER;
    }

    public String displayName() {
        return Addon.translate("flow.node." + id + ".name");
    }

    public String parameterName(Parameter parameter) {
        return parameter.key().equals(AUTOSTART) ? Addon.translate("flow.param.autostart") : Addon.translate("flow.node." + id + ".param." + parameter.key());
    }

    public String outputName(String output) {
        return Addon.translate("flow.output." + output);
    }

    /** Trigger blocks have no input, they are entry points of the flow. */
    public boolean hasInput() {
        return !isTrigger();
    }
}
