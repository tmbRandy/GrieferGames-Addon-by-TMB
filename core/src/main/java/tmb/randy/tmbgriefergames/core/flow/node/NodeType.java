package tmb.randy.tmbgriefergames.core.flow.node;

import java.util.List;
import tmb.randy.tmbgriefergames.core.Addon;

/**
 * Describes a kind of block: its parameters, its outputs and what it does when the flow reaches it.
 *
 * @param instant true for pure logic blocks, the flow continues with the next block in the same tick
 */
public record NodeType(String id, Category category, List<Parameter> parameters, List<String> outputs, boolean instant, Executor executor) {

    /** Output name used by all blocks that simply continue. */
    public static final String NEXT = "next";

    public enum Category {
        TRIGGER(0xFF2E7D32),
        ACTION(0xFF1565C0),
        MENU(0xFF6A1B9A),
        LOGIC(0xFFE65100);

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

    public String displayName() {
        return Addon.translate("flow.node." + id + ".name");
    }

    public String parameterName(Parameter parameter) {
        return Addon.translate("flow.node." + id + ".param." + parameter.key());
    }

    public String outputName(String output) {
        return Addon.translate("flow.output." + output);
    }

    /** Blocks without an input (only the start block) can not be the target of a connection. */
    public boolean hasInput() {
        return category != Category.TRIGGER;
    }
}
