package tmb.randy.tmbgriefergames.core.config;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.widgets.input.MultiKeybindWidget.MultiKeyBindSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;

public class SwordsSubConfig extends Config {

    @SettingSection("habk")

    @SwitchSetting
    private final ConfigProperty<Boolean> HABKenabled = new ConfigProperty<>(false);
    @SliderSetting(min = 100, max = 5000, steps = 100)
    private final ConfigProperty<Integer> HABKcooldown = new ConfigProperty<>(1000);

    @SettingSection("vabk")

    @SliderSetting(min = 3, max = 40, steps = 1)
    private final ConfigProperty<Integer> VABKloadTime = new ConfigProperty<>(16);
    @SliderSetting(min = 3, max = 40, steps = 1)
    private final ConfigProperty<Integer> VABKswitchCooldown = new ConfigProperty<>(18);
    @MultiKeyBindSetting
    private final ConfigProperty<Key[]> VABKhotkey = new ConfigProperty<>(new Key[]{Key.L_SHIFT, Key.V});

    public ConfigProperty<Boolean> getHABKenabled() { return this.HABKenabled; }
    public ConfigProperty<Integer> getHABKcooldown() { return this.HABKcooldown; }

    public ConfigProperty<Integer> getVABKloadTime() { return VABKloadTime; }
    public ConfigProperty<Integer> getVABKswitchCooldown() { return VABKswitchCooldown; }
    public ConfigProperty<Key[]> getVABKhotkey() { return VABKhotkey; }
}
