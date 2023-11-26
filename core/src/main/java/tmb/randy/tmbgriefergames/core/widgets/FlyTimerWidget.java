package tmb.randy.tmbgriefergames.core.widgets;

import net.labymod.api.Laby;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;

public class FlyTimerWidget extends TextHudWidget<TextHudWidgetConfig> {

    private String name;
    private TextLine line;

    public FlyTimerWidget() {
        super("flytimer");
        this.name = Laby.labyAPI().getName();
        this.bindCategory(HudWidgetCategory.INGAME);
    }

    @Override
    public void load(TextHudWidgetConfig config) {
        super.load(config);
        this.line = super.createLine(I18n.getTranslation("tmbgriefergames.flyTimer.flyPotion"), name);
    }

    @Override
    public void onTick(boolean isEditorContext) {
        String newName = isEditorContext ? Laby.labyAPI().getName() : Addon.getSharedInstance().getBridge().getWidgetString();
        if(name != null && name.equals(newName)) return;
        name = newName;

        this.line.updateAndFlush(name);
    }

    @Override
    public boolean isVisibleInGame() {
        return Addon.getSharedInstance().getBridge().isFlyCountdownActive() && Addon.isGG();
    }
}
