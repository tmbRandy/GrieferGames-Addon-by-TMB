package tmb.randy.tmbgriefergames.core.config;

import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.widgets.input.KeybindWidget.KeyBindSetting;
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

    public static final int SPRITE_SIZE = 21;

    public Configuration() {
        this.chatConfig = new ChatSubConfig();
        this.tooltipConfig = new TooltipSubConfig();
        this.hopperSubConfig = new HopperSubConfig();
        this.natureSubConfig = new NatureSubConfig();
        this.autoCrafterConfig = new AutoCrafterSubConfig();
        this.swordsSubConfig = new SwordsSubConfig();
        this.accountUnitySubConfig = new AccountUnitySubConfig();
    }

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE)
    private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE, x = 2, y = 1)
    private final ConfigProperty<Boolean> itemProtection = new ConfigProperty<>(true);

    @SpriteSlot(size = SPRITE_SIZE, x = 5, y = 2)
    private final AutoCrafterSubConfig autoCrafterConfig;

    @SpriteSlot(size = SPRITE_SIZE, x = 1)
    private final ChatSubConfig chatConfig;

    @SpriteSlot(size = SPRITE_SIZE, x = 3, y = 1)
    private final TooltipSubConfig tooltipConfig;

    @SpriteSlot(size = SPRITE_SIZE, x = 1, y = 3)
    private final SwordsSubConfig swordsSubConfig;

    @SpriteSlot(size = SPRITE_SIZE, x = 3, y = 2)
    private final NatureSubConfig natureSubConfig;

    @SpriteSlot(size = SPRITE_SIZE, x = 2, y = 2)
    private final HopperSubConfig hopperSubConfig;

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE, x = 4, y = 2)
    private final ConfigProperty<Boolean> skipHub = new ConfigProperty<>(true);

    @SpriteSlot(size = SPRITE_SIZE, x = 2, y = 3)
    @SwitchSetting
    private final ConfigProperty<Boolean> autoLoot = new ConfigProperty<>(true);

    private final AccountUnitySubConfig accountUnitySubConfig;

    @SettingSection("plotSwitch")

    @SpriteSlot(size = SPRITE_SIZE, x = 3, y = 3)
    @KeyBindSetting
    private final ConfigProperty<Key> plotWheelHotkey = new ConfigProperty<>(Key.GRAVE);

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
    public ConfigProperty<Boolean> getSkipHub() {return skipHub;}
    public HopperSubConfig getHopperSubConfig() {return hopperSubConfig;}
    public NatureSubConfig getNatureSubConfig() {return natureSubConfig;}
    public AutoCrafterSubConfig getAutoCrafterConfig() {return autoCrafterConfig;}
    public SwordsSubConfig getSwordsSubConfig() {return swordsSubConfig;}
    public ConfigProperty<Boolean> getAutoLoot() {return autoLoot;}
    public AccountUnitySubConfig getAccountUnitySubConfig() {return accountUnitySubConfig;}
    public ConfigProperty<Key> getPlotWheelHotkey() {return plotWheelHotkey;}
    public ConfigProperty<Key[]> getNextPlot() {return nextPlot;}
    public ConfigProperty<Key[]> getPreviousPlot() {return previousPlot;}
}
