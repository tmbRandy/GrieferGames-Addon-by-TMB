package tmb.randy.tmbgriefergames.core.config;

import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget.ButtonSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownEntryTranslationPrefix;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.ShowSettingInParent;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;
import net.labymod.api.util.MethodOrder;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.HopperFinalAction;
import tmb.randy.tmbgriefergames.core.enums.HopperItemStackSizeEnum;

public class HopperSubConfig extends Config {

    @SettingSection("auto")

    @ShowSettingInParent
    @SwitchSetting
    private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(false);
    @SwitchSetting
    private final ConfigProperty<Boolean> filterItem = new ConfigProperty<>(false);
    @SliderSetting(min = -1, max = 15, steps = 1)
    private final ConfigProperty<Integer> radius = new ConfigProperty<>(-1);
    @DropdownEntryTranslationPrefix("tmbgriefergames.settings.hopperSubConfig.stackSize.entries")
    @DropdownSetting
    private final ConfigProperty<HopperItemStackSizeEnum> stackSize = new ConfigProperty<>(HopperItemStackSizeEnum.NONE);
    @DropdownEntryTranslationPrefix("tmbgriefergames.settings.hopperSubConfig.finalAction.entries")
    @DropdownSetting
    private final ConfigProperty<HopperFinalAction> finalAction = new ConfigProperty<>(HopperFinalAction.NONE);
    @SwitchSetting
    private final ConfigProperty<Boolean> autoSneak = new ConfigProperty<>(false);

    @SettingSection("visual")

    @SwitchSetting
    private final ConfigProperty<Boolean> showLines = new ConfigProperty<>(true);
    @SwitchSetting
    private final ConfigProperty<Boolean> showRadius = new ConfigProperty<>(true);
    @MethodOrder(after = "showRadius")
    @ButtonSetting
    public void resetLines() {Addon.getSharedInstance().getBridge().resetLines();}

    public ConfigProperty<Boolean> getEnabled() {
        return this.enabled;
    }
    public ConfigProperty<Boolean> getFilterItem() {
        return this.filterItem;
    }
    public ConfigProperty<Integer> getRadius() {
        return this.radius;
    }
    public ConfigProperty<HopperItemStackSizeEnum> getStackSize() { return this.stackSize;}
    public ConfigProperty<HopperFinalAction> getFinalAction() {return this.finalAction;}
    public ConfigProperty<Boolean> getAutoSneak() {
        return this.autoSneak;
    }
    public ConfigProperty<Boolean> getShowLines() {return showLines;}
    public ConfigProperty<Boolean> getShowRadius() {return showRadius;}
}