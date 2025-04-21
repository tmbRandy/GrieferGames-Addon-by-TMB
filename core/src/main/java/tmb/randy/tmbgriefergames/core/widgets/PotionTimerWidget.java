package tmb.randy.tmbgriefergames.core.widgets;

import java.util.Date;
import javax.inject.Singleton;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.Subscribe;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.helper.I19n;

@Singleton
public class PotionTimerWidget extends TextHudWidget<TextHudWidgetConfig> {
    private TextLine flyLine;
    private TextLine breakLine;
    private static Date flyTimer;
    private static Date breakTimer;

    public enum Timer {
        FLY, BREAK
    }

    public PotionTimerWidget(HudWidgetCategory category) {
        super("potiontimer");
        setIcon(Icon.texture(ResourceLocation.create(Addon.getNamespace(), "textures/widgets/potion.png")));
        this.bindCategory(category);
    }

    @Override
    public void load(TextHudWidgetConfig config) {
        super.load(config);
        flyLine = createLine(I19n.translate("functions.potionTimer.flyPotion"), "");
        breakLine = createLine(I19n.translate("functions.potionTimer.breakPotion"), "");
    }

    @Subscribe
    public void onTick(boolean isEditorContext) {
        if (isEditorContext) {
            String placeholder = "15:00";
            flyLine.updateAndFlush(placeholder);
            breakLine.updateAndFlush(placeholder);
            return;
        }

        if (!Addon.isGG()) {
            flyLine.setState(State.HIDDEN);
            breakLine.setState(State.HIDDEN);
            return;
        }

        if (flyTimer != null) {
            long remainingTime = getRemainingTime(Timer.FLY);
            if (remainingTime < 0) {
                flyTimer = null;
                Addon.getSharedInstance().displayNotification(I19n.translate("functions.potionTimer.flyPotionExpired"));
                flyLine.updateAndFlush("");
                flyLine.setState(State.HIDDEN);
            } else {
                flyLine.updateAndFlush(formatTime(remainingTime));
                flyLine.setState(State.VISIBLE);
            }
        } else {
            flyLine.updateAndFlush("");
            flyLine.setState(State.HIDDEN);
        }

        if (breakTimer != null) {
            long remainingTime = getRemainingTime(Timer.BREAK);
            if (remainingTime < 0) {
                breakTimer = null;
                Addon.getSharedInstance().displayNotification(I19n.translate("functions.potionTimer.breakPotionExpired"));
                breakLine.updateAndFlush("");
                breakLine.setState(State.HIDDEN);
            } else {
                breakLine.updateAndFlush(formatTime(remainingTime));
                breakLine.setState(State.VISIBLE);
            }
        } else {
            breakLine.updateAndFlush("");
            breakLine.setState(State.HIDDEN);
        }
    }

    @Override
    public boolean isVisibleInGame() {
        return (isCountdownActive(Timer.FLY) || isCountdownActive(Timer.BREAK)) && Addon.isGG();
    }

    public static void startTimer(Timer timer) {
        Date currentTime = new Date();
        long expirationTime = currentTime.getTime() + 15 * 60 * 1000;

        switch (timer) {
            case FLY -> {
                flyTimer = new Date(expirationTime);
                Addon.getSharedInstance().displayNotification(I19n.translate("functions.potionTimer.flyPotionUsed"));
            }
            case BREAK -> {
                breakTimer = new Date(expirationTime);
                Addon.getSharedInstance().displayNotification(I19n.translate("functions.potionTimer.breakPotionUsed"));
            }
        }
    }

    private long getRemainingTime(Timer timer) {
        Date timerDate = (timer == Timer.FLY) ? flyTimer : breakTimer;
        if (timerDate == null) {
            return -1;
        }
        return timerDate.getTime() - System.currentTimeMillis();
    }

    public boolean isCountdownActive(Timer timer) {
        Date timerDate = (timer == Timer.FLY) ? flyTimer : breakTimer;
        return timerDate != null && getRemainingTime(timer) > 0;
    }

    private String formatTime(long remainingMillis) {
        int remainingMinutes = (int) (remainingMillis / (60 * 1000));
        int remainingSeconds = (int) ((remainingMillis / 1000) % 60);
        return String.format("%d:%02d", remainingMinutes, remainingSeconds);
    }
}