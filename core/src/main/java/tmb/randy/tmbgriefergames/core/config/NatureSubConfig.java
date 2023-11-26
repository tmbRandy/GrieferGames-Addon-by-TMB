package tmb.randy.tmbgriefergames.core.config;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.widgets.input.KeybindWidget.KeyBindSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.MultiKeybindWidget.MultiKeyBindSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.ParentSwitch;
import net.labymod.api.configuration.loader.property.ConfigProperty;

public class NatureSubConfig extends Config {

    @ParentSwitch
    private final ConfigProperty<Boolean> showBorders = new ConfigProperty<>(false);

    @SliderSetting(min = 1, max = 10, steps = 1)
    private final ConfigProperty<Integer> borderRadius = new ConfigProperty<>(5);

    @SliderSetting(min = 0.1f, max = 10f, steps = 0.1f)
    private final ConfigProperty<Float> borderheight = new ConfigProperty<>(1f);
    @MultiKeyBindSetting
    private final ConfigProperty<Key[]> hotkey = new ConfigProperty<>(new Key[]{Key.L_SHIFT, Key.N});
    @SwitchSetting
    private final ConfigProperty<Boolean> borderMaxHeight = new ConfigProperty<>(false);



    public ConfigProperty<Boolean> getShowBorders() { return this.showBorders; }
    public ConfigProperty<Integer> getBorderRadius() {
        return this.borderRadius;
    }
    public ConfigProperty<Float> getBorderheight() {
        return this.borderheight;
    }
    public ConfigProperty<Key[]> getHotkey() { return this.hotkey; }
    public ConfigProperty<Boolean> getBorderMaxHeight() {
        return this.borderMaxHeight;
    }

}