package tmb.randy.tmbgriefergames.core.widgets;

import net.labymod.api.Laby;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.component.data.DataComponentKey;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.nbt.tags.NBTTagCompound;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.widgets.AdventureWidget.AdventureWidgetConfig;
import java.util.Objects;

public class AdventureWidget extends TextHudWidget<AdventureWidgetConfig> {

    private String name;
    private TextLine line;

    public AdventureWidget(HudWidgetCategory category) {
        super("adventure", AdventureWidgetConfig.class);
        this.name = Laby.labyAPI().getName();
        setIcon(Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/alert.png")));
        this.bindCategory(category);
    }

    @Override
    public void load(AdventureWidgetConfig config) {
        super.load(config);
        line = super.createLine(I18n.getTranslation("tmbgriefergames.hudWidget.adventure.name"), name);
    }

    @Override
    public void onTick(boolean isEditorContext) {
        String newName = isEditorContext ? getEditorDummy(config.getOneLine().get()) : getAdventurerForItemStack(null, this.config.getOneLine().get());
        if(name != null && name.equals(newName)) return;
        name = newName;

        this.line.updateAndFlush(name);
    }

    @Override
    public boolean isVisibleInGame() {
        return Addon.isGG() && !getAdventurerForItemStack(null, this.config.getOneLine().get()).isEmpty();
    }

    public static class AdventureWidgetConfig extends TextHudWidgetConfig {

        @SwitchSetting
        private final ConfigProperty<Boolean> oneLine = new ConfigProperty<>(false);

        public ConfigProperty<Boolean> getOneLine() {return oneLine;}

    }

    public static String getAdventurerForItemStack(ItemStack stack, boolean oneLine) {
        if(stack == null && Laby.labyAPI().minecraft().getClientPlayer() != null)
            stack = Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).getMainHandItemStack();

        if(stack != null) {
            if(stack.hasDataComponentContainer()) {
                if(stack.getDataComponentContainer().has(DataComponentKey.simple("adventure"))) {
                    if(stack.getDataComponentContainer().get(DataComponentKey.simple("adventure")) instanceof NBTTagCompound adventureContainer) {
                        if(adventureContainer.contains("adventure.amount")) {
                            int amount = adventureContainer.getInt("adventure.amount");
                            if(adventureContainer.contains("adventure.req_amount")) {
                                int total = adventureContainer.getInt("adventure.req_amount");

                                float percent = 100f / (float)total * (float)amount;

                                int needed = total - amount;

                                int DKs = needed / (64 * 9 * 6);
                                int rest = needed - (DKs * 64 * 9 * 6);

                                int stacks = rest / 64;
                                int items = rest - (stacks * 64);

                                float percentRounded = Math.round(percent * 10) / 10.0f;

                                if(oneLine)
                                    return I18n.translate("tmbgriefergames.tooltip.adventurerTooltipOneLine", percentRounded, DKs, stacks, items);
                                else
                                    return I18n.translate("tmbgriefergames.tooltip.adventurerTooltipMultiLine", percentRounded, DKs, stacks, items);
                            }

                        }
                    }
                }
            }
        }

        return "";
    }

    private String getEditorDummy(boolean oneLine) {
        if(oneLine)
            return I18n.translate("tmbgriefergames.tooltip.adventurerTooltipOneLine", 25.3, 1, 13, 9);
        else
            return I18n.translate("tmbgriefergames.tooltip.adventurerTooltipMultiLine", 25.3, 1, 13, 9);
    }
}
