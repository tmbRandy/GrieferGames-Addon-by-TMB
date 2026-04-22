package tmb.randy.tmbgriefergames.core.config;

import static tmb.randy.tmbgriefergames.core.config.Configuration.SPRITE_SIZE;

import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.SpriteSlot;
import net.labymod.api.configuration.loader.property.ConfigProperty;

public class ChatSubConfig extends Config {

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE, x = 3)
    private final ConfigProperty<Boolean> typeCorrection = new ConfigProperty<>(true);

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE, x = 5)
    private final ConfigProperty<Boolean> antiFakeMoney = new ConfigProperty<>(true);

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE, x = 2)
    private final ConfigProperty<Boolean> messageSplit = new ConfigProperty<>(true);

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE, x = 4, y = 1)
    private final ConfigProperty<Boolean> hideEmptyLines = new ConfigProperty<>(true);

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE, y = 1)
    private final ConfigProperty<Boolean> cooldownNotifier = new ConfigProperty<>(true);

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE, x = 4)
    private final ConfigProperty<Boolean> hideNews = new ConfigProperty<>(true);

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE, x = 1, y = 1)
    private final ConfigProperty<Boolean> cleanChat = new ConfigProperty<>(true);

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE, x = 1, y = 2)
    private final ConfigProperty<Boolean> muteStreamer = new ConfigProperty<>(false);

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE, x = 4, y = 3)
    private final ConfigProperty<Boolean> muteCaseOpening = new ConfigProperty<>(false);

    @SwitchSetting
    @SpriteSlot(size = SPRITE_SIZE, x = 5, y = 3)
    private final ConfigProperty<Boolean> muteLuckyBlocks = new ConfigProperty<>(false);

    @SpriteSlot(size = SPRITE_SIZE, y = 3)
    @SwitchSetting
    private final ConfigProperty<Boolean> msgTabMode = new ConfigProperty<>(true);



    public ConfigProperty<Boolean> getTypeCorrection() {
        return this.typeCorrection;
    }
    public ConfigProperty<Boolean> getMessageSplit() {
        return this.messageSplit;
    }
    public ConfigProperty<Boolean> getHideNews() {
        return this.hideNews;
    }
    public ConfigProperty<Boolean> getAntiFakeMoney() {return this.antiFakeMoney;}
    public ConfigProperty<Boolean> getCooldownNotifier() {return this.cooldownNotifier;}
    public ConfigProperty<Boolean> getCleanChat() {return this.cleanChat;}
    public ConfigProperty<Boolean> getHideEmptyLines() {return this.hideEmptyLines;}
    public ConfigProperty<Boolean> getMuteStreamer() {return this.muteStreamer;}
    public ConfigProperty<Boolean> getMuteCaseOpening() {return muteCaseOpening;}
    public ConfigProperty<Boolean> getMuteLuckyBlocks() {return muteLuckyBlocks;}
    public ConfigProperty<Boolean> getMsgTabMode() {return msgTabMode;}
}