package tmb.randy.tmbgriefergames.core.config;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.widgets.input.MultiKeybindWidget.MultiKeyBindSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownEntryTranslationPrefix;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;
import tmb.randy.tmbgriefergames.core.enums.AutoCrafterNewFinalAction;
import tmb.randy.tmbgriefergames.core.enums.QueueType;

public class AutoCrafterSubConfig extends Config {

    @SettingSection("v1")

    @DropdownEntryTranslationPrefix("tmbgriefergames.settings.autoCrafterConfig.autoCraftSpeed.entries")
    @DropdownSetting
    private final ConfigProperty<QueueType> autoCraftSpeed = new ConfigProperty<>(QueueType.FAST);

    @SwitchSetting
    private final ConfigProperty<Boolean> autoDrop = new ConfigProperty<>(false);

    @SwitchSetting
    private final ConfigProperty<Boolean> endlessMode = new ConfigProperty<>(false);

    @SwitchSetting
    private final ConfigProperty<Boolean> onlyFullStacks = new ConfigProperty<>(false);

    @SettingSection("v2")

    @DropdownEntryTranslationPrefix("tmbgriefergames.settings.autoCrafterConfig.finalActionV2.entries")
    @DropdownSetting
    private final ConfigProperty<AutoCrafterNewFinalAction> finalActionV2 = new ConfigProperty<>(AutoCrafterNewFinalAction.COMP);

    @SettingSection("v3")

    @DropdownEntryTranslationPrefix("tmbgriefergames.settings.autoCrafterConfig.finalActionV3.entries")
    @DropdownSetting
    private final ConfigProperty<AutoCrafterNewFinalAction> finalActionV3 = new ConfigProperty<>(AutoCrafterNewFinalAction.COMP);

    @SliderSetting(min = 0, max = 80)
    private final ConfigProperty<Integer> delay = new ConfigProperty<>(0);

    @SettingSection("autocomp")

    @MultiKeyBindSetting
    private final ConfigProperty<Key[]> autoCompHotkey = new ConfigProperty<>(new Key[]{Key.ARROW_LEFT, Key.ARROW_UP, Key.ARROW_RIGHT});

    @MultiKeyBindSetting
    private final ConfigProperty<Key[]> autoDecompHotkey = new ConfigProperty<>(new Key[]{Key.ARROW_LEFT, Key.ARROW_DOWN, Key.ARROW_RIGHT});

    public ConfigProperty<QueueType> getAutoCraftSpeed() { return this.autoCraftSpeed; }
    public ConfigProperty<Boolean> getAutoDrop() {
        return this.autoDrop;
    }
    public ConfigProperty<Boolean> getEndlessMode() {
        return this.endlessMode;
    }
    public ConfigProperty<Boolean> getOnlyFullStacks() { return this.onlyFullStacks; }
    public ConfigProperty<AutoCrafterNewFinalAction> getFinalActionV2() { return finalActionV2; }
    public ConfigProperty<Key[]> getAutoCompHotkey() {return autoCompHotkey;}
    public ConfigProperty<Key[]> getAutoDecompHotkey() {return autoDecompHotkey;}
    public ConfigProperty<AutoCrafterNewFinalAction> getFinalActionV3() {return finalActionV3;}
    public ConfigProperty<Integer> getDelay() {return delay;}
}