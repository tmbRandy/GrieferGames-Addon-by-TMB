package tmb.randy.tmbgriefergames.core.activities.plotwheel;

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
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.CBtracker;
import tmb.randy.tmbgriefergames.core.FileManager;
import tmb.randy.tmbgriefergames.core.activities.plotwheel.CBwheel.CBsegment;
import tmb.randy.tmbgriefergames.core.activities.plotwheel.PlotWheel.PlotSegment;
import tmb.randy.tmbgriefergames.core.enums.CBs;
import java.util.UUID;

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

        /////////////////////////

        VerticalListWidget<HorizontalListWidget> addContainer = new VerticalListWidget<>().addId("add-container");

        HorizontalListWidget addPlotTitle = new HorizontalListWidget().addId("title-list");
        HorizontalListEntry addPlotEntry = new HorizontalListEntry(ComponentWidget.i18n("tmbgriefergames.plotWheel.addPlot"));
        addPlotTitle.addChild(addPlotEntry);
        addContainer.addChild(addPlotTitle);
        addContainer.addChild(addPlotTitle);

        //////////////

        HorizontalListWidget plotNameList = new HorizontalListWidget();
        HorizontalListEntry plotNameListTag = new HorizontalListEntry(ComponentWidget.i18n("tmbgriefergames.plotWheel.name").addId("label"));
        addNameTextField = new TextFieldWidget().placeholder(Component.translatable("tmbgriefergames.plotWheel.optional")).addId("name-textfield");
        HorizontalListEntry plotNameListTextfield = new HorizontalListEntry(addNameTextField);
        plotNameList.addChild(plotNameListTag);
        plotNameList.addChild(plotNameListTextfield);
        addContainer.addChild(plotNameList);

        //////////////

        HorizontalListWidget CBlist = new HorizontalListWidget();
        cbDropdownWidget = new DropdownWidget<>().addId("cb-dropdown");
        HorizontalListEntry cbListTag = new HorizontalListEntry(ComponentWidget.i18n("tmbgriefergames.plotWheel.cb").addId("label"));

        cbDropdownWidget.add(I18n.translate("tmbgriefergames.plotWheel.all"));
        for (CBs cb : CBs.values()) {
            if(CBtracker.isPlotworldCB(cb) && cb != CBs.EVENT) {
                cbDropdownWidget.add(cb.getName());
            }
        }

        if(CBtracker.isPlotworldCB())
            cbDropdownWidget.setSelected(CBtracker.getCurrentCB().getName());


        HorizontalListEntry CBlistEntry = new HorizontalListEntry(cbDropdownWidget);
        CBlist.addChild(cbListTag);
        CBlist.addChild(CBlistEntry);
        addContainer.addChild(CBlist);

        //////////////

        HorizontalListWidget commandList = new HorizontalListWidget();
        HorizontalListEntry commandListTag = new HorizontalListEntry(ComponentWidget.i18n("tmbgriefergames.plotWheel.command").addId("label"));
        addCommandTextField = new TextFieldWidget().placeholder(Component.text("/p h Farm")).addId("command-textfield");
        HorizontalListEntry commandListTextfield = new HorizontalListEntry(addCommandTextField);
        commandList.addChild(commandListTag);
        commandList.addChild(commandListTextfield);
        addContainer.addChild(commandList);

        //////////////

        HorizontalListWidget accountList = new HorizontalListWidget();
        HorizontalListEntry accountListTag = new HorizontalListEntry(ComponentWidget.i18n("tmbgriefergames.plotWheel.thisAccountOnly"));
        accountOnlyWidget = new CheckBoxWidget();
        HorizontalListEntry accountListCheckbox = new HorizontalListEntry(accountOnlyWidget);
        accountList.addChild(accountListTag);
        accountList.addChild(accountListCheckbox);
        addContainer.addChild(accountList);

        //////////////

        HorizontalListWidget addPlotButtonList = new HorizontalListWidget();
        ButtonWidget addButton = ButtonWidget.i18n("tmbgriefergames.plotWheel.add");
        addButton.setPressable(() -> {
            if(!addCommandTextField.getText().trim().isEmpty()) {
                String plotName = addNameTextField.getText().trim().isEmpty() ? null : addNameTextField.getText().trim();
                CBs selectedCB = (cbDropdownWidget.getSelected() == null ? I18n.translate("tmbgriefergames.plotWheel.all") : cbDropdownWidget.getSelected()).equals(I18n.translate("tmbgriefergames.plotWheel.all")) ? CBs.NONE : CBs.valueOf(cbDropdownWidget.getSelected());
                UUID uuid = accountOnlyWidget.state() == CheckBoxWidget.State.CHECKED ? Laby.labyAPI().getUniqueId() : null;
                String command = addCommandTextField.getText().trim();

                PlotWheelPlot plot = new PlotWheelPlot(selectedCB, plotName, command, uuid);
                FileManager.addPlot(plot);

                resetAddWidgets();
                plotWheel.loadPlots(this.selectedCB);
            }
        });

        HorizontalListEntry addPlotButtonEntry = new HorizontalListEntry(addButton);
        addPlotButtonList.addChild(addPlotButtonEntry);
        addContainer.addChild(addPlotButtonList);

        //////////////

        document.addChild(addContainer);
    }

    @Override
    public void onOpenScreen() {
        super.onOpenScreen();
        setSelectedCB(CBtracker.getCurrentCB());
    }

    @Override
    public void setSelectedCB(CBs cb) {
        if(isOpen() && cbDisplay != null) {
            selectedCB = cb;
            cbDisplay.setText(cb.getName());
            cbDisplay.textColor().set(CBtracker.getCurrentCB() == cb ? Color.GREEN.get() : Color.ORANGE.get());
            plotWheel.loadPlots(cb);
        }
    }

    @Subscribe
    public void tick(GameTickEvent event) {
        if(isOpen() && Addon.isGG()) {
            for (AbstractWidget<?> child : cbWheel.getChildren()) {
                if(child instanceof CBsegment segment) {
                    if(segment.isSegmentSelected()) {
                        if(segment.getCb() != selectedCB) {
                            setSelectedCB(segment.getCb());
                        }
                        break;
                    }
                }
            }

            for (AbstractWidget<?> child : plotWheel.getChildren()) {
                if(child instanceof PlotSegment segment) {
                    if(segment.isSegmentSelected()) {
                        plotDisplay.setText(segment.getPlot().command());
                        return;
                    }
                }
            }

            if(plotDisplay != null)
                plotDisplay.setText("");
        }
    }

    private void resetAddWidgets() {
        addCommandTextField.setText("");
        addNameTextField.setText("");
        accountOnlyWidget.setState(CheckBoxWidget.State.UNCHECKED);
        cbDropdownWidget.setSelected(CBtracker.getCurrentCB().getName());
    }
}