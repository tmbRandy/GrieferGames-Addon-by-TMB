package tmb.randy.tmbgriefergames.core.config;

import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.SpriteSlot;
import net.labymod.api.configuration.loader.property.ConfigProperty;

public class TooltipSubConfig extends Config {
    @SwitchSetting
    @SpriteSlot(size = 21, x = 5, y = 1)
    private final ConfigProperty<Boolean> showCompTooltip = new ConfigProperty<>(true);

    @SwitchSetting
    @SpriteSlot(size = 21, y = 2)
    private final ConfigProperty<Boolean> showAdventurerTooltip = new ConfigProperty<>(true);



    public ConfigProperty<Boolean> getShowCompTooltip() {
        return this.showCompTooltip;
    }
    public ConfigProperty<Boolean> getShowAdventurerTooltip() {
        return this.showAdventurerTooltip;
    }
}
