package tmb.randy.tmbgriefergames.core.activities.plotwheel;

import java.util.UUID;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.activity.types.SimpleActivity;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.CheckBoxWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.entry.HorizontalListEntry;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.util.Color;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.activities.plotwheel.CBwheel.CBsegment;
import tmb.randy.tmbgriefergames.core.activities.plotwheel.PlotWheel.PlotSegment;
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.helper.FileManager;

@AutoActivity
@Link("plotwheelactivity.lss")
public class PlotWheelActivity extends SimpleActivity implements ISelectableCB {
    PlotWheel plotWheel = new PlotWheel();
    CBwheel cbWheel = new CBwheel();
    CBs selectedCB = CBs.NONE;
    ComponentWidget cbDisplay = ComponentWidget.text("CB");
    ComponentWidget plotDisplay = ComponentWidget.text("Plot");
    TextFieldWidget addNameTextField;
    TextFieldWidget addCommandTextField;
    CheckBoxWidget accountOnlyWidget;
    DropdownWidget<String> cbDropdownWidget;

    @Override
    public void initialize(Parent parent) {
        super.initialize(parent);
        cbWheel.initialize(parent);

        cbDisplay.addId("cb-display");
        plotDisplay.addId("plot-display");

        cbWheel.addId("cb-wheel");
        this.document().addChild(cbWheel);

        plotWheel.initialize(parent);
        plotWheel.addId("plot-wheel");
        this.document().addChild(plotWheel);

        VerticalListWidget<Widget> centerList = new VerticalListWidget<>();
        centerList.addId("center-container");
        centerList.addChild(cbDisplay);
        centerList.addChild(plotDisplay);
        this.document.addChild(centerList);

        VerticalListWidget<HorizontalListWidget> addContainer = new VerticalListWidget<>().addId("add-container");

        HorizontalListWidget addPlotTitle = new HorizontalListWidget().addId("title-list");
        addPlotTitle.addChild(new HorizontalListEntry(ComponentWidget.i18n("tmbgriefergames.plotWheel.addPlot")));
        addContainer.addChild(addPlotTitle);

        addNameTextField = new TextFieldWidget().placeholder(Component.translatable("tmbgriefergames.plotWheel.optional")).addId("name-textfield");
        addContainer.addChild(buildLabelRow("tmbgriefergames.plotWheel.name", addNameTextField));

        cbDropdownWidget = new DropdownWidget<String>().addId("cb-dropdown");
        cbDropdownWidget.add(Addon.translate("plotWheel.all"));
        for (CBs cb : CBs.values()) {
            if (CBtracker.isPlotworldCB(cb) && cb != CBs.EVENT)
                cbDropdownWidget.add(cb.getName());
        }
        if (CBtracker.isPlotworldCB())
            cbDropdownWidget.setSelected(CBtracker.getCurrentCB().getName());
        addContainer.addChild(buildLabelRow("tmbgriefergames.plotWheel.cb", cbDropdownWidget));

        addCommandTextField = new TextFieldWidget().placeholder(Component.text("/p h Farm")).addId("command-textfield");
        addContainer.addChild(buildLabelRow("tmbgriefergames.plotWheel.command", addCommandTextField));

        accountOnlyWidget = new CheckBoxWidget();
        addContainer.addChild(buildLabelRow(Addon.getNamespace() + ".plotWheel.thisAccountOnly", accountOnlyWidget));

        ButtonWidget addButton = ButtonWidget.i18n(Addon.getNamespace() + ".plotWheel.add");
        addButton.setPressable(() -> {
            String command = addCommandTextField.getText().trim();
            if (command.isEmpty()) return;

            String plotNameText = addNameTextField.getText().trim();
            String selected = cbDropdownWidget.getSelected();
            CBs plotCB = selected == null || selected.equals(Addon.translate("plotWheel.all"))
                ? CBs.NONE
                : CBs.valueOf(selected);
            UUID uuid = accountOnlyWidget.state() == CheckBoxWidget.State.CHECKED ? Laby.labyAPI().getUniqueId() : null;

            FileManager.addPlot(new PlotWheelPlot(plotCB, plotNameText.isEmpty() ? null : plotNameText, command, uuid));
            resetAddWidgets();
            plotWheel.loadPlots(this.selectedCB);
        });

        HorizontalListWidget addPlotButtonList = new HorizontalListWidget();
        addPlotButtonList.addChild(new HorizontalListEntry(addButton));
        addContainer.addChild(addPlotButtonList);

        document.addChild(addContainer);
    }

    private HorizontalListWidget buildLabelRow(String translationKey, Widget widget) {
        HorizontalListWidget row = new HorizontalListWidget();
        row.addChild(new HorizontalListEntry(ComponentWidget.i18n(translationKey).addId("label")));
        row.addChild(new HorizontalListEntry(widget));
        return row;
    }

    @Override
    public void onOpenScreen() {
        super.onOpenScreen();
        setSelectedCB(CBtracker.getCurrentCB());
    }

    @Override
    public void setSelectedCB(CBs cb) {
        if (isOpen()) {
            selectedCB = cb;
            cbDisplay.setText(cb.getName());
            cbDisplay.textColor().set(CBtracker.getCurrentCB() == cb ? Color.GREEN.get() : Color.ORANGE.get());
            plotWheel.loadPlots(cb);
        }
    }

    @Subscribe
    public void tick(GameTickEvent event) {
        if (!isOpen() || !Addon.isGG()) return;

        for (AbstractWidget<?> child : cbWheel.getChildren()) {
            if (child instanceof CBsegment segment && segment.isSegmentSelected()) {
                if (segment.getCb() != selectedCB)
                    setSelectedCB(segment.getCb());
                break;
            }
        }

        for (AbstractWidget<?> child : plotWheel.getChildren()) {
            if (child instanceof PlotSegment segment && segment.isSegmentSelected()) {
                plotDisplay.setText(segment.getPlot().command());
                return;
            }
        }

        plotDisplay.setText("");
    }

    private void resetAddWidgets() {
        addCommandTextField.setText("");
        addNameTextField.setText("");
        accountOnlyWidget.setState(CheckBoxWidget.State.UNCHECKED);
        cbDropdownWidget.setSelected(CBtracker.getCurrentCB().getName());
    }
}
