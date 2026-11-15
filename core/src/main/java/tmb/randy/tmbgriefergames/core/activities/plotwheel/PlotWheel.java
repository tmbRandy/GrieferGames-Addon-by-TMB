package tmb.randy.tmbgriefergames.core.activities.plotwheel;

import java.util.List;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.ScreenInstance;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.context.ContextMenu;
import net.labymod.api.client.gui.screen.widget.context.ContextMenuEntry;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.WheelWidget;
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.helper.FileManager;

/** Inner wheel of the plot wheel: the saved plots of the selected CB. */
@AutoWidget
@Link("plotwheel.lss")
public class PlotWheel extends WheelWidget {
    /** The wheel is always filled up to this many segments, so it keeps its shape. */
    private static final int MIN_SEGMENTS = 25;

    private List<PlotWheelPlot> plots = List.of();
    private CBs selectedCB = CBs.NONE;

    @Override
    public void initialize(Parent parent) {
        super.initialize(parent);
        refresh();
    }

    public void refresh() {
        removeChildIf(widget -> widget instanceof Segment);

        for (int i = plots.size(); i < MIN_SEGMENTS; i++)
            addSegment(new Segment());

        for (int i = plots.size() - 1; i >= 0; i--)
            addSegment(new PlotSegment(plots.get(i)));
    }

    public void loadPlots(CBs cb) {
        selectedCB = cb;
        plots = FileManager.loadPlots().stream()
            .filter(plot -> plot.cb() == cb || plot.cb() == CBs.NONE)
            .filter(plot -> plot.account() == null || plot.account().equals(Laby.labyAPI().getUniqueId()))
            .toList();
        reInitialize();
    }

    /** Runs the plot command, switching to the selected CB first if necessary. */
    private void teleport(PlotWheelPlot plot) {
        if (!CBtracker.isPlotworldCB())
            return;

        if (selectedCB == CBtracker.getCurrentCB()) {
            Laby.references().chatExecutor().chat(plot.command());
            Laby.labyAPI().minecraft().minecraftWindow().displayScreen((ScreenInstance) null);
        } else {
            Addon.queuedPlot = plot;
            Laby.references().chatExecutor().chat(Const.Cmd.SWITCH + selectedCB);
        }
    }

    private static String getDisplayId(String command) {
        String lowerCase = command.toLowerCase();
        if (lowerCase.startsWith(Const.Cmd.PLOT_HOME))
            return "plot-segment-display-plot";
        if (lowerCase.startsWith(Const.Cmd.HOME))
            return "plot-segment-display-home";
        if (lowerCase.startsWith(Const.Cmd.WARP))
            return "plot-segment-display-warp";
        return "plot-segment-display-else";
    }

    public class PlotSegment extends Segment {
        private final PlotWheelPlot plot;

        public PlotSegment(PlotWheelPlot plot) {
            this.plot = plot;
            addId("plot-segment");
            addChild(ComponentWidget.text(plot.name() == null ? plot.command() : plot.name()).addId(getDisplayId(plot.command())));

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.addEntry(ContextMenuEntry.builder()
                .text(Component.translatable(Addon.getNamespace() + ".plotWheel.delete"))
                .clickHandler(entry -> {
                    FileManager.deletePlot(plot);
                    loadPlots(selectedCB);
                    return true;
                })
                .build());
            setContextMenu(contextMenu);

            setPressable(() -> {
                if (Laby.labyAPI().minecraft().isMouseDown(MouseButton.LEFT))
                    teleport(plot);
                else if (Laby.labyAPI().minecraft().isMouseDown(MouseButton.RIGHT))
                    openContextMenu();
            });
        }

        public PlotWheelPlot getPlot() {
            return plot;
        }
    }
}
