package tmb.randy.tmbgriefergames.core.config;

import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget.TextFieldSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.property.ConfigProperty;

public class AccountUnitySubConfig extends Config {

    @SwitchSetting
    private final ConfigProperty<Boolean> tpAccept = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> voteBooster = new ConfigProperty<>(true);

    @TextFieldSetting
    private final ConfigProperty<String> mainAccount = new ConfigProperty<>("");

    public ConfigProperty<Boolean> getTpAccept() {return tpAccept;}
    public ConfigProperty<Boolean> getVoteBooster() {return voteBooster;}
    public ConfigProperty<String> getMainAccount() {return mainAccount;}
}