package tmb.randy.tmbgriefergames.core.flow.editor;

import java.util.List;
import net.labymod.api.client.gfx.pipeline.renderer.text.TextRenderingOptions;
import net.labymod.api.client.gui.screen.state.ScreenCanvas;
import tmb.randy.tmbgriefergames.core.flow.FlowNode;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType;
import tmb.randy.tmbgriefergames.core.flow.node.Parameter;

/** Position and size of a block on the screen; used for rendering and for hit tests. */
final class NodeLayout {
    static final float WIDTH = 120;
    private static final float HEADER = 13;
    private static final float ROW = 10;
    private static final float PORT_RADIUS = 3.5F;
    private static final float PORT_HIT_RADIUS = 6;

    private static final int BODY_COLOR = 0xEE2C2C34;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int PARAM_COLOR = 0xFFB8B8C0;
    private static final int PORT_COLOR = 0xFFE0E0E0;
    private static final int SELECTED_COLOR = 0xFFFFD54F;
    private static final int RUNNING_COLOR = 0xFF66BB6A;

    private final FlowNode node;
    private final NodeType type;
    private final float x;
    private final float y;
    private final float height;

    NodeLayout(FlowNode node, float x, float y) {
        this.node = node;
        this.type = node.getType();
        this.x = x;
        this.y = y;
        int rows = Math.max(1, Math.max(type.parameters().size(), type.outputs().size()));
        this.height = HEADER + rows * ROW + 3;
    }

    boolean contains(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + height;
    }

    float inputX() {
        return x;
    }

    float inputY() {
        return y + HEADER / 2;
    }

    float outputX() {
        return x + WIDTH;
    }

    float outputY(String output) {
        int index = Math.max(0, type.outputs().indexOf(output));
        return y + HEADER + index * ROW + ROW / 2 + 1;
    }

    /** The output whose port is under the mouse, null if none. */
    String getOutputAt(float mouseX, float mouseY) {
        for (String output : type.outputs()) {
            float dx = mouseX - outputX();
            float dy = mouseY - outputY(output);
            if (dx * dx + dy * dy <= PORT_HIT_RADIUS * PORT_HIT_RADIUS)
                return output;
        }
        return null;
    }

    void render(ScreenCanvas canvas, boolean selected, boolean running) {
        canvas.submitRelativeRect(x, y, WIDTH, height, BODY_COLOR);
        canvas.submitRelativeRect(x, y, WIDTH, HEADER, type.category().color());
        if (selected || running)
            canvas.submitRelativeOutlineRect(x - 1, y - 1, WIDTH + 2, height + 2, 1, selected ? SELECTED_COLOR : RUNNING_COLOR, selected ? SELECTED_COLOR : RUNNING_COLOR);

        canvas.submitText(type.displayName(), x + 4, y + 3, TEXT_COLOR, TextRenderingOptions.SHADOW);

        List<Parameter> parameters = type.parameters();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            String text = type.parameterName(parameter) + ": " + node.getParam(parameter.key());
            canvas.submitText(trim(canvas, text, WIDTH * 0.6F), x + 4, y + HEADER + i * ROW + 2, PARAM_COLOR, 0.8F, TextRenderingOptions.NONE);
        }

        if (type.hasInput())
            canvas.submitCircle(inputX(), inputY(), PORT_RADIUS, PORT_COLOR);

        for (String output : type.outputs()) {
            String label = type.outputName(output);
            float labelWidth = canvas.getTextWidth(label) * 0.8F;
            canvas.submitText(label, outputX() - labelWidth - 6, outputY(output) - 3, PARAM_COLOR, 0.8F, TextRenderingOptions.NONE);
            canvas.submitCircle(outputX(), outputY(output), PORT_RADIUS, PORT_COLOR);
        }
    }

    private static String trim(ScreenCanvas canvas, String text, float maxWidth) {
        if (canvas.getTextWidth(text) * 0.8F <= maxWidth)
            return text;

        String trimmed = text;
        while (trimmed.length() > 1 && canvas.getTextWidth(trimmed + "…") * 0.8F > maxWidth)
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        return trimmed + "…";
    }
}
