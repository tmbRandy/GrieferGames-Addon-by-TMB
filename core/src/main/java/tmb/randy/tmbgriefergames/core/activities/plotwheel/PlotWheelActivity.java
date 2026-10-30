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
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.activities.plotwheel.CBwheel.CBsegment;
import tmb.randy.tmbgriefergames.core.activities.plotwheel.PlotWheel.PlotSegment;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.helper.FileManager;

/** Plot wheel screen: CB wheel outside, plot wheel inside and a form to add new plots. */
@AutoActivity
@Link("plotwheelactivity.lss")
public class PlotWheelActivity extends SimpleActivity {
    private final PlotWheel plotWheel = new PlotWheel();
    private final CBwheel cbWheel = new CBwheel();
    private final ComponentWidget cbDisplay = ComponentWidget.text("CB");
    private final ComponentWidget plotDisplay = ComponentWidget.text("Plot");
    private CBs selectedCB = CBs.NONE;

    private TextFieldWidget nameField;
    private TextFieldWidget commandField;
    private CheckBoxWidget accountOnlyCheckBox;
    private DropdownWidget<String> cbDropdown;

    private static String key(String key) {
        return Addon.getNamespace() + ".plotWheel." + key;
    }

    @Override
    public void initialize(Parent parent) {
        super.initialize(parent);

        cbWheel.initialize(parent);
        cbWheel.addId("cb-wheel");
        document().addChild(cbWheel);

        plotWheel.initialize(parent);
        plotWheel.addId("plot-wheel");
        document().addChild(plotWheel);

        VerticalListWidget<Widget> centerList = new VerticalListWidget<>().addId("center-container");
        centerList.addChild(cbDisplay.addId("cb-display"));
        centerList.addChild(plotDisplay.addId("plot-display"));
        document().addChild(centerList);

        document().addChild(createAddPlotForm());
    }

    private VerticalListWidget<HorizontalListWidget> createAddPlotForm() {
        VerticalListWidget<HorizontalListWidget> form = new VerticalListWidget<>().addId("add-container");

        HorizontalListWidget title = new HorizontalListWidget().addId("title-list");
        title.addChild(new HorizontalListEntry(ComponentWidget.i18n(key("addPlot"))));
        form.addChild(title);

        nameField = new TextFieldWidget().placeholder(Component.translatable(key("optional"))).addId("name-textfield");
        form.addChild(createLabeledRow("name", nameField));

        cbDropdown = new DropdownWidget<String>().addId("cb-dropdown");
        cbDropdown.add(Addon.translate("plotWheel.all"));
        for (CBs cb : CBtracker.getSwitchablePlotworldCBs())
            cbDropdown.add(cb.getName());
        if (CBtracker.isPlotworldCB())
            cbDropdown.setSelected(CBtracker.getCurrentCB().getName());
        form.addChild(createLabeledRow("cb", cbDropdown));

        commandField = new TextFieldWidget().placeholder(Component.text("/p h Farm")).addId("command-textfield");
        form.addChild(createLabeledRow("command", commandField));

        accountOnlyCheckBox = new CheckBoxWidget();
        form.addChild(createLabeledRow("thisAccountOnly", accountOnlyCheckBox));

        ButtonWidget addButton = ButtonWidget.i18n(key("add"));
        addButton.setPressable(this::addPlot);
        HorizontalListWidget buttonRow = new HorizontalListWidget();
        buttonRow.addChild(new HorizontalListEntry(addButton));
        form.addChild(buttonRow);

        return form;
    }

    private HorizontalListWidget createLabeledRow(String labelKey, Widget widget) {
        HorizontalListWidget row = new HorizontalListWidget();
        row.addChild(new HorizontalListEntry(ComponentWidget.i18n(key(labelKey)).addId("label")));
        row.addChild(new HorizontalListEntry(widget));
        return row;
    }

    private void addPlot() {
        String command = commandField.getText().trim();
        if (command.isEmpty())
            return;

        String name = nameField.getText().trim();
        String selected = cbDropdown.getSelected();
        CBs cb = selected == null || selected.equals(Addon.translate("plotWheel.all")) ? CBs.NONE : CBs.valueOf(selected);
        UUID account = accountOnlyCheckBox.state() == CheckBoxWidget.State.CHECKED ? Laby.labyAPI().getUniqueId() : null;

        FileManager.addPlot(new PlotWheelPlot(cb, name.isEmpty() ? null : name, command, account));
        resetForm();
        plotWheel.loadPlots(selectedCB);
    }

    private void resetForm() {
        commandField.setText("");
        nameField.setText("");
        accountOnlyCheckBox.setState(CheckBoxWidget.State.UNCHECKED);
        cbDropdown.setSelected(CBtracker.getCurrentCB().getName());
    }

    @Override
    public void onOpenScreen() {
        super.onOpenScreen();
        selectCB(CBtracker.getCurrentCB());
    }

    private void selectCB(CBs cb) {
        if (!isOpen())
            return;

        selectedCB = cb;
        cbDisplay.setText(cb.getName());
        cbDisplay.textColor().set(CBtracker.getCurrentCB() == cb ? Color.GREEN.get() : Color.ORANGE.get());
        plotWheel.loadPlots(cb);
    }

    /** Follows the hovered segments: the hovered CB loads its plots, the hovered plot shows its command. */
    @Subscribe
    public void tick(GameTickEvent event) {
        if (!isOpen() || !Addon.isGG())
            return;

        for (AbstractWidget<?> child : cbWheel.getChildren()) {
            if (child instanceof CBsegment segment && segment.isSegmentSelected()) {
                if (segment.getCb() != selectedCB)
                    selectCB(segment.getCb());
                break;
            }
        }

        String hoveredCommand = "";
        for (AbstractWidget<?> child : plotWheel.getChildren()) {
            if (child instanceof PlotSegment segment && segment.isSegmentSelected()) {
                hoveredCommand = segment.getPlot().command();
                break;
            }
        }
        plotDisplay.setText(hoveredCommand);
    }
}
