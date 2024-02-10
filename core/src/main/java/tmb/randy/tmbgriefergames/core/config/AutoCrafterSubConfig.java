package tmb.randy.tmbgriefergames.core.config;

import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import tmb.randy.tmbgriefergames.core.enums.AutoCrafterNewFinalAction;
import tmb.randy.tmbgriefergames.core.enums.QueueType;

public class AutoCrafterSubConfig extends Config {
    @DropdownSetting
    private final ConfigProperty<QueueType> autoCraftSpeed = new ConfigProperty<>(QueueType.FAST);

    @SwitchSetting
    private final ConfigProperty<Boolean> autoDrop = new ConfigProperty<>(false);

    @SwitchSetting
    private final ConfigProperty<Boolean> endlessMode = new ConfigProperty<>(false);

    @SwitchSetting
    private final ConfigProperty<Boolean> onlyFullStacks = new ConfigProperty<>(false);

    @DropdownSetting
    private final ConfigProperty<AutoCrafterNewFinalAction> finalAction = new ConfigProperty<>(AutoCrafterNewFinalAction.COMP);

    public ConfigProperty<QueueType> getAutoCraftSpeed() { return this.autoCraftSpeed; }
    public ConfigProperty<Boolean> getAutoDrop() {
        return this.autoDrop;
    }

    public ConfigProperty<Boolean> getEndlessMode() {
        return this.endlessMode;
    }

    public ConfigProperty<Boolean> getOnlyFullStacks() { return this.onlyFullStacks; }

    public ConfigProperty<AutoCrafterNewFinalAction> getFinalAction() { return finalAction; }
}