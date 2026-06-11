package tmb.randy.tmbgriefergames.core.activities.plotwheel;

import java.util.List;
import java.util.stream.Collectors;
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
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.helper.FileManager;

@AutoWidget
@Link("plotwheel.lss")
public class PlotWheel extends WheelWidget {
    private List<PlotWheelPlot> plots = List.of();
    private static CBs selectedCB;

    public void initialize(Parent parent) {
        super.initialize(parent);
        this.refresh();
    }

    public void refresh() {
        this.removeChildIf((widget) -> widget instanceof Segment);

        int placeholders = Math.max(0, 25 - plots.size());
        for (int i = 0; i < placeholders; i++) {
            this.addSegment(new Segment());
        }

        for (int i = plots.size() - 1; i >= 0; i--) {
            PlotWheelPlot plot = plots.get(i);
            PlotSegment segment = new PlotSegment(plot, this);
            segment.addId("plot-segment");
            this.addSegment(segment);
        }
    }

    public static class PlotSegment extends Segment {
        private final PlotWheelPlot plot;

        public PlotSegment(PlotWheelPlot plot, PlotWheel plotWheel) {
            this.plot = plot;
            ComponentWidget display = ComponentWidget.text(plot.name() == null ? plot.command() : plot.name());

            String commandLower = plot.command().toLowerCase();
            if (commandLower.startsWith(Const.Cmd.PLOT_HOME)) {
                display.addId("plot-segment-display-plot");
            } else if (commandLower.startsWith(Const.Cmd.HOME)) {
                display.addId("plot-segment-display-home");
            } else if (commandLower.startsWith(Const.Cmd.WARP)) {
                display.addId("plot-segment-display-warp");
            } else {
                display.addId("plot-segment-display-else");
            }

            this.addChild(display);

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.addEntry(ContextMenuEntry.builder().text(Component.translatable("tmbgriefergames.plotWheel.delete")).clickHandler(contextMenuEntry -> {
                FileManager.deletePlot(plot);
                plotWheel.loadPlots(plot.cb());
                return true;
            }).build());
            this.setContextMenu(contextMenu);

            this.setPressable(() -> {
                if (Laby.labyAPI().minecraft().isMouseDown(MouseButton.LEFT)) {
                    if (CBtracker.isPlotworldCB()) {
                        if (selectedCB == CBtracker.getCurrentCB()) {
                            Laby.references().chatExecutor().chat(plot.command());
                            Laby.labyAPI().minecraft().minecraftWindow().displayScreen((ScreenInstance) null);
                        } else {
                            Addon.queuedPlot = plot;
                            Laby.references().chatExecutor().chat(Const.Cmd.SWITCH + selectedCB);
                        }
                    }
                } else if (Laby.labyAPI().minecraft().isMouseDown(MouseButton.RIGHT)) {
                    this.openContextMenu();
                }
            });
        }

        public PlotWheelPlot getPlot() {
            return plot;
        }
    }

    public void loadPlots(CBs cb) {
        selectedCB = cb;
        plots = FileManager.loadPlots().stream()
            .filter(p -> p.cb() == cb || p.cb() == CBs.NONE)
            .filter(p -> p.account() == null || p.account().equals(Laby.labyAPI().getUniqueId()))
            .collect(Collectors.toList());
        this.reInitialize();
    }
}
