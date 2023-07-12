package tmb.randy.griefergames.core.config;

import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.widgets.input.MultiKeybindWidget.MultiKeyBindSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.annotation.SpriteSlot;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;

@ConfigName("settings")
@SpriteTexture("settings")
public class Configuration extends AddonConfig {

    public Configuration() {
        this.chatConfig = new ChatSubConfig();
        this.tooltipConfig = new TooltipSubConfig();
    }

    @SwitchSetting
    @SpriteSlot(size = 21)
    private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

    @SwitchSetting
    @SpriteSlot(size = 21, x = 2, y = 1)
    private final ConfigProperty<Boolean> itemProtection = new ConfigProperty<>(true);

    @SpriteSlot(size = 21, x = 1)
    private final ChatSubConfig chatConfig;

    @SpriteSlot(size = 21, x = 3, y = 1)
    private final TooltipSubConfig tooltipConfig;

    @SettingSection("plotSwitch")

    @MultiKeyBindSetting
    private final ConfigProperty<Key[]> nextPlot = new ConfigProperty<>(new Key[]{Key.L_SHIFT, Key.ARROW_RIGHT});
    @MultiKeyBindSetting
    private final ConfigProperty<Key[]> previousPlot = new ConfigProperty<>(new Key[]{Key.L_SHIFT, Key.ARROW_LEFT});

  @Override
    public ConfigProperty<Boolean> enabled() {
      return this.enabled;
  }

    public ConfigProperty<Boolean> getItemProtection() {
        return this.itemProtection;
    }
    public ChatSubConfig getChatConfig() {return this.chatConfig; }
    public TooltipSubConfig getTooltipConfig() {return this.tooltipConfig; }
    public ConfigProperty<Key[]> getNextPlot() {return nextPlot;}
    public ConfigProperty<Key[]> getPreviousPlot() {return previousPlot;}

}
