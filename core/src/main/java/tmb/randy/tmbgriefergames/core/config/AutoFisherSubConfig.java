package tmb.randy.tmbgriefergames.core.config;

import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.ShowSettingInParent;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;

public class AutoFisherSubConfig extends Config {

    @ShowSettingInParent
    @SwitchSetting
    private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);
    @SwitchSetting
    private final ConfigProperty<Boolean> allowEC = new ConfigProperty<>(false);

    @SettingSection("rubbish")

    @SwitchSetting
    private final ConfigProperty<Boolean> autoDropRods = new ConfigProperty<>(false);
    @SwitchSetting
    private final ConfigProperty<Boolean> autoDropBows = new ConfigProperty<>(false);
    @SwitchSetting
    private final ConfigProperty<Boolean> autoDropSaddle = new ConfigProperty<>(false);
    @SwitchSetting
    private final ConfigProperty<Boolean> autoDropFish = new ConfigProperty<>(false);
    @SwitchSetting
    private final ConfigProperty<Boolean> autoDropBooks = new ConfigProperty<>(false);
    @SwitchSetting
    private final ConfigProperty<Boolean> autoDropLilypads = new ConfigProperty<>(false);

    public ConfigProperty<Boolean> getEnabled() { return this.enabled; }

    public ConfigProperty<Boolean> getAllowEC() {return allowEC;}
    public ConfigProperty<Boolean> getAutoDropRods() {
        return this.autoDropRods;
    }
    public ConfigProperty<Boolean> getAutoDropBows() {
        return this.autoDropBows;
    }
    public ConfigProperty<Boolean> getAutoDropSaddle() {
        return this.autoDropSaddle;
    }
    public ConfigProperty<Boolean> getAutoDropFish() {
        return this.autoDropFish;
    }
    public ConfigProperty<Boolean> getAutoDropBooks() {
        return this.autoDropBooks;
    }
    public ConfigProperty<Boolean> getAutoDropLilypads() {
        return this.autoDropLilypads;
    }
}
