package tmb.randy.tmbgriefergames.core.widgets;

import net.labymod.api.Laby;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.helper.I19n;
import tmb.randy.tmbgriefergames.core.helper.ItemClearTimerListener;

public class ItemClearWidget extends TextHudWidget<TextHudWidgetConfig> {
    private String name;
    private TextLine line;

    public ItemClearWidget(HudWidgetCategory category) {
        super("itemclear");
        this.name = Laby.labyAPI().getName();
        setIcon(Icon.texture(ResourceLocation.create(Addon.getNamespace(), "textures/widgets/itemremover.png")));
        this.bindCategory(category);
    }

    @Override
    public void load(TextHudWidgetConfig config) {
        super.load(config);
        this.line = super.createLine(I19n.translate("itemRemover.itemRemover"), name);
    }

    @Override
    public void onTick(boolean isEditorContext) {
        if(Addon.isGG()) {
            name = ItemClearTimerListener.getDisplayValue();
            this.line.updateAndFlush(name);
        }
    }

    @Override
    public boolean isVisibleInGame() {
        return ItemClearTimerListener.getDisplayValue().length() > 1 && Addon.isGG();
    }
}
