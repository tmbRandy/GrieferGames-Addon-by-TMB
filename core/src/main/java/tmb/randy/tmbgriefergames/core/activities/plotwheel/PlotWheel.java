package tmb.randy.tmbgriefergames.core.activities.plotwheel;

import java.util.ArrayList;
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
import tmb.randy.tmbgriefergames.core.enums.CBs;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.helper.FileManager;

@AutoWidget
@Link("plotwheel.lss")
public class PlotWheel extends WheelWidget {
    private ArrayList<PlotWheelPlot> plots = new ArrayList<>();
    private static CBs selectedCB;

    public void initialize(Parent parent) {
        super.initialize(parent);
        this.refresh();
    }

    public void refresh() {
        this.removeChildIf((widget) -> widget instanceof Segment);

        //Add placeholder segments to guarantee al least 25 segments.
        if(plots.size() < 25) {
            for (int i = plots.size(); i < 25; i++) {
                this.addSegment(new Segment());
            }
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

            if (plot.command().toLowerCase().startsWith("/p h ")) {
                display.addId("plot-segment-display-plot");
            } else if(plot.command().toLowerCase().startsWith("/home ")) {
                display.addId("plot-segment-display-home");
            } else if(plot.command().toLowerCase().startsWith("/warp ")) {
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
                if(Laby.labyAPI().minecraft().isMouseDown(MouseButton.LEFT)) {
                    if(CBtracker.isPlotworldCB()) {
                        if(selectedCB == CBtracker.getCurrentCB()) {
                            Laby.references().chatExecutor().chat(plot.command());
                            Laby.labyAPI().minecraft().minecraftWindow().displayScreen((ScreenInstance) null);
                        } else {
                            Addon.queuedPlot = plot;
                            Laby.references().chatExecutor().chat("/switch " + selectedCB);
                        }
                    }
                } else if(Laby.labyAPI().minecraft().isMouseDown(MouseButton.RIGHT)) {
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
        ArrayList<PlotWheelPlot> allPlots = FileManager.loadPlots();
        ArrayList<PlotWheelPlot> filteredPlots = new ArrayList<>();

        for (PlotWheelPlot plot : allPlots) {
            if(plot.cb() == cb || plot.cb() == CBs.NONE)
                if(plot.account() == null || plot.account().equals(Laby.labyAPI().getUniqueId()))
                    filteredPlots.add(plot);
        }

        plots = filteredPlots;
        this.reInitialize();
    }
}