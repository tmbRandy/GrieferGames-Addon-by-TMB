package tmb.randy.tmbgriefergames.core.config;

import static tmb.randy.tmbgriefergames.core.config.Configuration.SPRITE_SIZE;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.widgets.input.KeybindWidget.KeyBindSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.MultiKeybindWidget.MultiKeyBindSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.SpriteSlot;
import net.labymod.api.configuration.loader.property.ConfigProperty;

public class PlotSwitchSubConfig extends Config {
    @SpriteSlot(size = SPRITE_SIZE, x = 3, y = 3)
    @KeyBindSetting
    private final ConfigProperty<Key> plotWheelHotkey = new ConfigProperty<>(Key.GRAVE);

    @MultiKeyBindSetting
    private final ConfigProperty<Key[]> nextPlot = new ConfigProperty<>(new Key[]{Key.L_SHIFT, Key.ARROW_RIGHT});
    @MultiKeyBindSetting
    private final ConfigProperty<Key[]> previousPlot = new ConfigProperty<>(new Key[]{Key.L_SHIFT, Key.ARROW_LEFT});

    public ConfigProperty<Key> getPlotWheelHotkey() {return plotWheelHotkey;}
    public ConfigProperty<Key[]> getNextPlot() {return nextPlot;}
    public ConfigProperty<Key[]> getPreviousPlot() {return previousPlot;}
}
