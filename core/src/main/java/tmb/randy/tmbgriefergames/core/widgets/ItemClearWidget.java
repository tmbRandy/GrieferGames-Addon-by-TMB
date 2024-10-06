package tmb.randy.tmbgriefergames.core.widgets;

import net.labymod.api.Laby;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.util.ItemClearTimerListener;

public class ItemClearWidget extends TextHudWidget<TextHudWidgetConfig> {
    private String name;
    private TextLine line;

    public ItemClearWidget(HudWidgetCategory category) {
        super("itemclear");
        this.name = Laby.labyAPI().getName();
        setIcon(Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/itemremover.png")));
        this.bindCategory(category);
    }

    @Override
    public void load(TextHudWidgetConfig config) {
        super.load(config);
        this.line = super.createLine(I18n.getTranslation("tmbgriefergames.itemRemover.itemRemover"), name);
    }

    @Override
    public void onTick(boolean isEditorContext) {
        name = ItemClearTimerListener.getDisplayValue();
        this.line.updateAndFlush(name);
    }

    @Override
    public boolean isVisibleInGame() {
        return ItemClearTimerListener.getDisplayValue().length() > 1 && Addon.isGG();
    }
}
