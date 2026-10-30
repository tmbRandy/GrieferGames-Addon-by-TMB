package tmb.randy.tmbgriefergames.core.flow.editor;

import java.util.List;
import java.util.function.Predicate;
import net.labymod.api.client.gfx.pipeline.renderer.text.TextRenderingOptions;
import net.labymod.api.client.gui.screen.state.RoundedData;
import net.labymod.api.client.gui.screen.state.ScreenCanvas;
import tmb.randy.tmbgriefergames.core.flow.FlowNode;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType;
import tmb.randy.tmbgriefergames.core.flow.node.Parameter;

/** Position and size of a block on the screen (already zoomed); used for rendering and for hit tests. */
final class NodeLayout {
    // Unzoomed sizes
    static final float WIDTH = 120;
    private static final float HEADER = 14;
    private static final float ROW = 10;
    private static final float RADIUS = 4;
    private static final float PORT_RADIUS = 3.5F;
    private static final float PORT_HIT_RADIUS = 7;

    private static final int BODY_COLOR = 0xF0262630;
    private static final int SHADOW_COLOR = 0x90000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int PARAM_COLOR = 0xFFAEB0BA;
    private static final int PORT_RING_COLOR = 0xFF15151A;
    private static final int PORT_EMPTY_COLOR = 0xFF3A3A44;
    private static final int SELECTED_COLOR = 0xFFFFD54F;
    private static final int RUNNING_COLOR = 0xFF66BB6A;

    private final FlowNode node;
    private final NodeType type;
    private final float x;
    private final float y;
    private final float zoom;
    private final float width;
    private final float height;

    NodeLayout(FlowNode node, float x, float y, float zoom) {
        this.node = node;
        this.type = node.getType();
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        int rows = Math.max(1, Math.max(type.parameters().size(), type.outputs().size()));
        this.width = WIDTH * zoom;
        this.height = (HEADER + rows * ROW + 4) * zoom;
    }

    /** Color of the connections and the port of an output, so success and failure paths can be told apart. */
    static int outputColor(String output) {
        return switch (output) {
            case "yes", "ok", "found", "reached", "loop" -> 0xFF66BB6A;
            case "no", "timeout", "none" -> 0xFFEF5350;
            case "done" -> 0xFFFFA726;
            default -> 0xFFB0BEC5;
        };
    }

    boolean contains(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    float inputX() {
        return x;
    }

    float inputY() {
        return y + HEADER * zoom / 2;
    }

    float outputX() {
        return x + width;
    }

    float outputY(String output) {
        int index = Math.max(0, type.outputs().indexOf(output));
        return y + (HEADER + index * ROW + ROW / 2 + 2) * zoom;
    }

    /** The output whose port is under the mouse, null if none. */
    String getOutputAt(float mouseX, float mouseY) {
        float hitRadius = PORT_HIT_RADIUS * Math.max(zoom, 0.7F);
        for (String output : type.outputs()) {
            float dx = mouseX - outputX();
            float dy = mouseY - outputY(output);
            if (dx * dx + dy * dy <= hitRadius * hitRadius)
                return output;
        }
        return null;
    }

    void render(ScreenCanvas canvas, boolean selected, boolean running, Predicate<String> isConnected) {
        float radius = RADIUS * zoom;
        RoundedData corners = rounded(radius, radius, radius, radius);

        canvas.submitRelativeOuterShadowRect(x, y, width, height, corners, SHADOW_COLOR, 0, 2 * zoom, 0, 6 * zoom);
        if (selected || running) {
            float border = 1.5F * zoom;
            canvas.submitRelativeRoundedRect(x - border, y - border, width + 2 * border, height + 2 * border,
                selected ? SELECTED_COLOR : RUNNING_COLOR, rounded(radius + border, radius + border, radius + border, radius + border));
        }
        canvas.submitRelativeRoundedRect(x, y, width, height, BODY_COLOR, corners);
        canvas.submitRelativeRoundedRect(x, y, width, HEADER * zoom, type.category().color(), rounded(radius, radius, 0, 0));

        canvas.submitText(type.displayName(), x + 5 * zoom, y + 3.5F * zoom, TEXT_COLOR, zoom, TextRenderingOptions.SHADOW);

        float textScale = 0.8F * zoom;
        List<Parameter> parameters = type.parameters();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            String text = type.parameterName(parameter) + ": " + node.getParam(parameter.key());
            canvas.submitText(trim(canvas, text, width * 0.62F, textScale), x + 5 * zoom, y + (HEADER + i * ROW + 3) * zoom, PARAM_COLOR, textScale, TextRenderingOptions.NONE);
        }

        if (type.hasInput())
            renderPort(canvas, inputX(), inputY(), 0xFFE0E0E0, true);

        for (String output : type.outputs()) {
            String label = type.outputName(output);
            float labelWidth = canvas.getTextWidth(label) * textScale;
            canvas.submitText(label, outputX() - labelWidth - 7 * zoom, outputY(output) - 3 * zoom, PARAM_COLOR, textScale, TextRenderingOptions.NONE);
            renderPort(canvas, outputX(), outputY(output), outputColor(output), isConnected.test(output));
        }
    }

    /** A ring with a filled center; unconnected outputs stay hollow. */
    private void renderPort(ScreenCanvas canvas, float portX, float portY, int color, boolean filled) {
        canvas.submitCircle(portX, portY, (PORT_RADIUS + 1.2F) * zoom, PORT_RING_COLOR);
        canvas.submitCircle(portX, portY, PORT_RADIUS * zoom, color);
        if (!filled)
            canvas.submitCircle(portX, portY, (PORT_RADIUS - 1.3F) * zoom, PORT_EMPTY_COLOR);
    }

    private static RoundedData rounded(float leftTop, float rightTop, float leftBottom, float rightBottom) {
        return RoundedData.builder()
            .setLeftTopRadius(leftTop)
            .setRightTopRadius(rightTop)
            .setLeftBottomRadius(leftBottom)
            .setRightBottomRadius(rightBottom)
            .build();
    }

    private static String trim(ScreenCanvas canvas, String text, float maxWidth, float scale) {
        if (canvas.getTextWidth(text) * scale <= maxWidth)
            return text;

        String trimmed = text;
        while (trimmed.length() > 1 && canvas.getTextWidth(trimmed + "…") * scale > maxWidth)
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        return trimmed + "…";
    }
}
