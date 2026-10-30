package tmb.randy.tmbgriefergames.core.flow.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.activity.types.SimpleActivity;
import net.labymod.api.client.gui.screen.key.InputType;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.BoundsType;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.ScrollWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.entry.HorizontalListEntry;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.client.gui.screen.state.ScreenCanvas;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.flow.Flow;
import tmb.randy.tmbgriefergames.core.flow.FlowConnection;
import tmb.randy.tmbgriefergames.core.flow.FlowManager;
import tmb.randy.tmbgriefergames.core.flow.FlowNode;
import tmb.randy.tmbgriefergames.core.flow.node.NodeType;
import tmb.randy.tmbgriefergames.core.flow.node.NodeTypes;
import tmb.randy.tmbgriefergames.core.flow.node.Parameter;

/**
 * Node editor in the style of n8n: blocks are added from the palette on the left, moved by dragging,
 * connected by dragging from an output port onto another block and configured in the panel on the right.
 */
@AutoActivity
@Link("flow-editor.lss")
public class FlowEditorActivity extends SimpleActivity {
    // Must match flow-editor.lss
    private static final float TOOLBAR_HEIGHT = 24;
    private static final float SIDEBAR_WIDTH = 120;
    private static final float PROPERTIES_WIDTH = 160;

    private static final int BACKGROUND = 0xFF1B1B21;
    private static final int GRID = 0xFF23232B;
    private static final int GRID_STRONG = 0xFF2D2D37;
    private static final int GRID_SIZE = 20;
    private static final float MIN_ZOOM = 0.3F;
    private static final float MAX_ZOOM = 2.5F;
    private static final float ZOOM_STEP = 1.1F;
    private static final int SELECTED_COLOR = 0xFFFFD54F;

    private enum Drag { NONE, NODE, PAN, CONNECTION }

    private static FlowEditorActivity openEditor;

    private final FlowManager manager = FlowManager.get();
    private Flow flow;
    private FlowNode selected;
    private float panX = SIDEBAR_WIDTH + 40;
    private float panY = TOOLBAR_HEIGHT + 40;
    private float zoom = 1;

    private Drag drag = Drag.NONE;
    private FlowNode dragNode;
    private String dragOutput;
    private float dragOffsetX;
    private float dragOffsetY;
    private float mouseX;
    private float mouseY;

    private TextFieldWidget nameField;
    private TextFieldWidget hotkeyField;
    private final List<TextFieldWidget> textFields = new ArrayList<>();

    public FlowEditorActivity(Flow flow) {
        this.flow = flow != null ? flow : manager.getFlows().stream().findFirst().orElseGet(this::createFlow);
    }

    private Flow createFlow() {
        Flow newFlow = new Flow(createName(Addon.translate("flow.editor.newFlowName", "").trim()));
        newFlow.getNodes().add(new FlowNode(NodeTypes.get(NodeTypes.START), 0, 0));
        manager.save(newFlow);
        return newFlow;
    }

    /** "base 1", "base 2", ... the first name that is not taken yet. */
    private String createName(String base) {
        int number = 1;
        while (manager.getFlow(base + " " + number) != null)
            number++;
        return base + " " + number;
    }

    @Override
    public boolean shouldRenderBackground() {
        return false;
    }

    // ---------------------------------------------------------------- Widgets

    @Override
    public void initialize(Parent parent) {
        super.initialize(parent);
        textFields.clear();
        document().addChild(createToolbar());
        document().addChild(new ScrollWidget(createSidebar()).addId("sidebar"));
        if (hasPropertiesPanel())
            document().addChild(createProperties(selected));
    }

    private boolean hasPropertiesPanel() {
        return !flow.isBuiltin() && selected != null && selected.getType() != null && !selected.getType().parameters().isEmpty();
    }

    private HorizontalListWidget createToolbar() {
        HorizontalListWidget toolbar = new HorizontalListWidget().addId("toolbar");
        if (flow.isBuiltin())
            return createBuiltinToolbar(toolbar);

        nameField = new TextFieldWidget().addId("name-field");
        nameField.setText(flow.getName());
        nameField.submitHandler(text -> save());
        textFields.add(nameField);
        toolbar.addEntry(nameField);

        toolbar.addEntry(ComponentWidget.i18n(key("hotkey")).addId("label"));
        hotkeyField = new TextFieldWidget().placeholder(Component.text("L_SHIFT+K")).addId("hotkey-field");
        hotkeyField.setText(String.join("+", flow.getHotkey()));
        hotkeyField.submitHandler(text -> save());
        textFields.add(hotkeyField);
        toolbar.addEntry(hotkeyField);

        toolbar.addEntry(ButtonWidget.i18n(key("save"), this::save));
        toolbar.addEntry(ButtonWidget.i18n(key(manager.isRunning(flow.getName()) ? "stop" : "run"), () -> {
            save();
            manager.toggle(flow);
            reload();
        }));
        toolbar.addEntry(ButtonWidget.i18n(key("delete"), () -> {
            manager.delete(flow);
            flow = manager.getFlows().stream().findFirst().orElseGet(this::createFlow);
            selected = null;
            reload();
        }));
        return toolbar;
    }

    /** Built-in flows can only be run or copied. */
    private HorizontalListWidget createBuiltinToolbar(HorizontalListWidget toolbar) {
        nameField = null;
        hotkeyField = null;
        toolbar.addEntry(ComponentWidget.text("🔒 " + flow.getName()).addId("title"));
        toolbar.addEntry(ComponentWidget.i18n(key("readOnly")).addId("label"));
        toolbar.addEntry(ButtonWidget.i18n(key(manager.isRunning(flow.getName()) ? "stop" : "run"), () -> {
            manager.toggle(flow);
            reload();
        }));
        toolbar.addEntry(ButtonWidget.i18n(key("copy"), () -> {
            Flow copy = flow.copy(createName(flow.getName()));
            manager.save(copy);
            switchTo(copy);
        }));
        return toolbar;
    }

    private VerticalListWidget<Widget> createSidebar() {
        VerticalListWidget<Widget> sidebar = new VerticalListWidget<>().addId("sidebar-list");

        sidebar.addChild(ComponentWidget.i18n(key("flows")).addId("section"));
        for (Flow other : manager.getFlows()) {
            String prefix = (other == flow ? "» " : manager.isRunning(other.getName()) ? "▶ " : "") + (other.isBuiltin() ? "🔒 " : "");
            sidebar.addChild(ButtonWidget.text(prefix + other.getName(), () -> switchTo(other)));
        }
        sidebar.addChild(ButtonWidget.i18n(key("newFlow"), () -> switchTo(createFlow())));

        for (NodeType.Category category : flow.isBuiltin() ? new NodeType.Category[0] : NodeType.Category.values()) {
            sidebar.addChild(ComponentWidget.i18n(Addon.getNamespace() + ".flow.category." + category.name().toLowerCase()).addId("section"));
            for (NodeType type : NodeTypes.all()) {
                if (type.category() == category && !type.id().equals(NodeTypes.START))
                    sidebar.addChild(ButtonWidget.text(type.displayName(), () -> addNode(type)));
            }
        }
        return sidebar;
    }

    private VerticalListWidget<Widget> createProperties(FlowNode node) {
        VerticalListWidget<Widget> properties = new VerticalListWidget<>().addId("properties");
        properties.addChild(ComponentWidget.text(node.getType().displayName()).addId("section"));

        for (Parameter parameter : node.getType().parameters()) {
            HorizontalListWidget row = new HorizontalListWidget().addId("property-row");
            row.addEntry(ComponentWidget.text(node.getType().parameterName(parameter)).addId("label"));

            if (parameter.kind() == Parameter.Kind.BOOLEAN) {
                CheckBoxWidget checkBox = new CheckBoxWidget();
                checkBox.setState(Boolean.parseBoolean(node.getParam(parameter.key())) ? CheckBoxWidget.State.CHECKED : CheckBoxWidget.State.UNCHECKED);
                checkBox.setPressable(() -> node.setParam(parameter.key(), String.valueOf(checkBox.state() == CheckBoxWidget.State.CHECKED)));
                row.addEntry(checkBox);
            } else {
                TextFieldWidget field = new TextFieldWidget().addId("property-field");
                field.setText(node.getParam(parameter.key()));
                if (parameter.kind() == Parameter.Kind.NUMBER)
                    field.validator(text -> text.matches("-?\\d*"));
                field.updateListener(text -> node.setParam(parameter.key(), text));
                textFields.add(field);
                row.addEntry(field);
            }
            properties.addChild(row);
        }

        properties.addChild(ButtonWidget.i18n(key("deleteBlock"), () -> deleteNode(node)));
        return properties;
    }

    private static String key(String key) {
        return Addon.getNamespace() + ".flow.editor." + key;
    }

    // ---------------------------------------------------------------- Actions

    private void save() {
        if (flow.isBuiltin() || nameField == null)
            return;

        String name = nameField.getText().trim();
        flow.setHotkey(Arrays.stream(hotkeyField.getText().toUpperCase().split("\\+")).map(String::trim).filter(text -> !text.isEmpty()).toList());

        if (!name.isEmpty() && !name.equals(flow.getName()) && manager.getFlow(name) == null)
            manager.rename(flow, name);
        else
            manager.save(flow);
        reload();
    }

    private void switchTo(Flow other) {
        save();
        flow = other;
        selected = null;
        reload();
    }

    private void addNode(NodeType type) {
        float centerX = toWorldX((SIDEBAR_WIDTH + getScreenWidth() - PROPERTIES_WIDTH) / 2) - NodeLayout.WIDTH / 2;
        float centerY = toWorldY((TOOLBAR_HEIGHT + getScreenHeight()) / 2);
        FlowNode node = new FlowNode(type, snap(centerX), snap(centerY));
        flow.getNodes().add(node);
        selected = node;
        reload();
    }

    private void deleteNode(FlowNode node) {
        flow.removeNode(node);
        if (selected == node)
            selected = null;
        reload();
    }

    private static float snap(float value) {
        return Math.round(value / (GRID_SIZE / 2F)) * (GRID_SIZE / 2F);
    }

    @Override
    public void onOpenScreen() {
        super.onOpenScreen();
        openEditor = this;
    }

    @Override
    public void onCloseScreen() {
        super.onCloseScreen();
        manager.save(flow);
        if (openEditor == this)
            openEditor = null;
    }

    /** The editor that is currently open, null if none (used by the dev test bridge). */
    public static FlowEditorActivity getOpenEditor() {
        return openEditor;
    }

    /** Describes all widgets with their bounds, for the dev test bridge. */
    public void dumpWidgets(Consumer<String> output) {
        dumpWidget(document(), "", output);
    }

    private static void dumpWidget(Widget widget, String indent, Consumer<String> output) {
        Bounds bounds = widget.bounds();
        output.accept(String.format("%s%s %s x=%.0f y=%.0f w=%.0f h=%.0f", indent, widget.getClass().getSimpleName(), widget.getIds(),
            bounds.getX(BoundsType.OUTER), bounds.getY(BoundsType.OUTER), bounds.getWidth(BoundsType.OUTER), bounds.getHeight(BoundsType.OUTER)));
        for (Object child : widget.getChildren())
            if (child instanceof Widget childWidget)
                dumpWidget(childWidget, indent + "  ", output);
    }

    // ---------------------------------------------------------------- Rendering

    @Override
    public void render(ScreenContext context) {
        mouseX = context.mouse().getX();
        mouseY = context.mouse().getY();
        renderCanvas(context);
        super.render(context);
    }

    private void renderCanvas(ScreenContext context) {
        ScreenCanvas canvas = context.canvas();
        float width = getScreenWidth();
        float height = getScreenHeight();
        canvas.submitRelativeRect(0, 0, width, height, BACKGROUND);

        // Fine grid plus a stronger line every fifth cell
        float gridSize = GRID_SIZE * zoom;
        int firstX = (int) Math.floor(-panX / gridSize);
        int firstY = (int) Math.floor(-panY / gridSize);
        for (int i = firstX; i * gridSize + panX < width; i++)
            canvas.submitRelativeRect(i * gridSize + panX, 0, 1, height, i % 5 == 0 ? GRID_STRONG : GRID);
        for (int i = firstY; i * gridSize + panY < height; i++)
            canvas.submitRelativeRect(0, i * gridSize + panY, width, 1, i % 5 == 0 ? GRID_STRONG : GRID);

        for (FlowConnection connection : flow.getConnections()) {
            FlowNode from = flow.getNode(connection.from());
            FlowNode to = flow.getNode(connection.to());
            if (from != null && to != null && from.getType() != null && to.getType() != null) {
                NodeLayout fromLayout = layout(from);
                NodeLayout toLayout = layout(to);
                drawCurve(context, fromLayout.outputX(), fromLayout.outputY(connection.output()), toLayout.inputX(), toLayout.inputY(),
                    NodeLayout.outputColor(connection.output()));
            }
        }

        if (drag == Drag.CONNECTION && dragNode != null) {
            NodeLayout layout = layout(dragNode);
            drawCurve(context, layout.outputX(), layout.outputY(dragOutput), mouseX, mouseY, SELECTED_COLOR);
        }

        boolean running = manager.isRunning(flow.getName());
        for (FlowNode node : flow.getNodes()) {
            if (node.getType() != null)
                layout(node).render(canvas, node == selected, running, output -> flow.getTarget(node, output) != null);
        }
    }

    /**
     * S-curve like in n8n, approximated with short lines. Connections that lead backwards get bigger handles,
     * so they bend around the blocks instead of crossing them.
     */
    private void drawCurve(ScreenContext context, float x1, float y1, float x2, float y2, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float handle = Math.max(40 * zoom, dx < 0 ? Math.max(-dx / 2, Math.abs(dy) / 2) : dx / 2);
        float lineWidth = Math.max(1.2F, 2 * zoom);
        int segments = (int) Math.max(16, Math.min(80, (Math.abs(dx) + Math.abs(dy) + 2 * handle) / 6));

        float lastX = x1;
        float lastY = y1;
        for (int i = 1; i <= segments; i++) {
            float t = (float) i / segments;
            float u = 1 - t;
            float x = u * u * u * x1 + 3 * u * u * t * (x1 + handle) + 3 * u * t * t * (x2 - handle) + t * t * t * x2;
            float y = u * u * u * y1 + 3 * u * u * t * y1 + 3 * u * t * t * y2 + t * t * t * y2;
            drawSegment(context, lastX, lastY, x, y, lineWidth, color);
            // Round joints, otherwise thick segments leave small gaps at the bends
            context.canvas().submitCircle(x, y, lineWidth / 2, color);
            lastX = x;
            lastY = y;
        }
    }

    /** A thick straight line as rotated rectangle (line widths of the line renderer are not reliable in the GUI). */
    private static void drawSegment(ScreenContext context, float x1, float y1, float x2, float y2, float width, int color) {
        float length = (float) Math.hypot(x2 - x1, y2 - y1);
        if (length < 0.01F)
            return;

        context.pushStack();
        context.translate(x1, y1, 0);
        context.rotateRadiansZ((float) Math.atan2(y2 - y1, x2 - x1));
        context.canvas().submitRelativeRect(0, -width / 2, length, width, color);
        context.popStack();
    }

    private NodeLayout layout(FlowNode node) {
        return new NodeLayout(node, node.getX() * zoom + panX, node.getY() * zoom + panY, zoom);
    }

    private float toWorldX(float screenX) {
        return (screenX - panX) / zoom;
    }

    private float toWorldY(float screenY) {
        return (screenY - panY) / zoom;
    }

    private static float getScreenWidth() {
        return Laby.labyAPI().minecraft().minecraftWindow().getScaledWidth();
    }

    private static float getScreenHeight() {
        return Laby.labyAPI().minecraft().minecraftWindow().getScaledHeight();
    }

    // ---------------------------------------------------------------- Input

    private boolean isOnCanvas(float x, float y) {
        float right = getScreenWidth() - (hasPropertiesPanel() ? PROPERTIES_WIDTH : 0);
        return x > SIDEBAR_WIDTH && x < right && y > TOOLBAR_HEIGHT;
    }

    @Override
    public boolean mouseClicked(MutableMouse mouse, MouseButton button) {
        float x = mouse.getX();
        float y = mouse.getY();
        if (!isOnCanvas(x, y))
            return super.mouseClicked(mouse, button);

        document().unfocus();
        List<FlowNode> nodes = flow.getNodes();
        for (int i = flow.isBuiltin() ? -1 : nodes.size() - 1; i >= 0; i--) {
            FlowNode node = nodes.get(i);
            if (node.getType() == null)
                continue;

            NodeLayout layout = layout(node);
            String output = layout.getOutputAt(x, y);
            if (output != null) {
                if (button == MouseButton.RIGHT) {
                    flow.getConnections().removeIf(connection -> connection.from().equals(node.getId()) && connection.output().equals(output));
                } else {
                    drag = Drag.CONNECTION;
                    dragNode = node;
                    dragOutput = output;
                }
                return true;
            }

            if (layout.contains(x, y)) {
                if (button == MouseButton.RIGHT && !node.getType().id().equals(NodeTypes.START)) {
                    deleteNode(node);
                    return true;
                }

                // Bring to front
                nodes.remove(i);
                nodes.add(node);
                drag = Drag.NODE;
                dragNode = node;
                dragOffsetX = toWorldX(x) - node.getX();
                dragOffsetY = toWorldY(y) - node.getY();
                if (selected != node) {
                    selected = node;
                    reload();
                }
                return true;
            }
        }

        drag = Drag.PAN;
        dragOffsetX = x - panX;
        dragOffsetY = y - panY;
        if (selected != null) {
            selected = null;
            reload();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MutableMouse mouse, MouseButton button, double deltaX, double deltaY) {
        float x = mouse.getX();
        float y = mouse.getY();
        switch (drag) {
            case NODE -> dragNode.setPosition(snap(toWorldX(x) - dragOffsetX), snap(toWorldY(y) - dragOffsetY));
            case PAN -> {
                panX = x - dragOffsetX;
                panY = y - dragOffsetY;
            }
            case CONNECTION -> {
                mouseX = x;
                mouseY = y;
            }
            case NONE -> {
                return super.mouseDragged(mouse, button, deltaX, deltaY);
            }
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MutableMouse mouse, MouseButton button) {
        if (drag == Drag.CONNECTION) {
            for (FlowNode node : flow.getNodes()) {
                if (node != dragNode && node.getType() != null && node.getType().hasInput() && layout(node).contains(mouse.getX(), mouse.getY())) {
                    flow.connect(dragNode, dragOutput, node);
                    break;
                }
            }
        }

        boolean consumed = drag != Drag.NONE;
        drag = Drag.NONE;
        dragNode = null;
        return consumed || super.mouseReleased(mouse, button);
    }

    /** Zooms around the mouse position, so the point under the cursor stays where it is. */
    @Override
    public boolean mouseScrolled(MutableMouse mouse, double scrollDelta) {
        float x = mouse.getX();
        float y = mouse.getY();
        if (!isOnCanvas(x, y) || scrollDelta == 0)
            return super.mouseScrolled(mouse, scrollDelta);

        float oldZoom = zoom;
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * (scrollDelta > 0 ? ZOOM_STEP : 1 / ZOOM_STEP)));
        panX = x - (x - panX) * zoom / oldZoom;
        panY = y - (y - panY) * zoom / oldZoom;
        return true;
    }

    @Override
    public boolean keyPressed(Key key, InputType type) {
        boolean typing = textFields.stream().anyMatch(TextFieldWidget::isFocused);
        if (key == Key.DELETE && selected != null && !typing && !flow.isBuiltin() && !selected.getType().id().equals(NodeTypes.START)) {
            deleteNode(selected);
            return true;
        }
        return super.keyPressed(key, type);
    }
}
