package tmb.randy.tmbgriefergames.core.util;

import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.component.data.DataComponentKey;
import net.labymod.api.component.data.NbtDataComponentContainer;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import net.labymod.api.nbt.NBTTag;
import net.labymod.api.nbt.NBTTagType;
import net.labymod.api.nbt.tags.NBTTagCompound;
import net.labymod.api.nbt.tags.NBTTagList;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.widgets.AdventureWidget;

public class TooltipExtension {

    @Subscribe
    public void renderTooltip(ItemStackTooltipEvent event) {
        if(!Addon.isGG()) return;

        ItemStack stack = event.itemStack();

        if(stack.hasDataComponentContainer()) {

            if(Addon.getSharedInstance().configuration().getTooltipConfig().getShowCompTooltip().get() && (stack.getDataComponentContainer().has(DataComponentKey.simple("currentAmount")) || stack.getDataComponentContainer().has(DataComponentKey.simple("stackSize")))) {
                if(stack.getDataComponentContainer() instanceof NbtDataComponentContainer compound) {

                    int endlessChestAmount = getEndlessChestAmount(compound.getWrapped());
                    int multiplier = endlessChestAmount > -1 ? endlessChestAmount : stack.getSize();

                    int currentAmount = compound.getWrapped().getInt(compound.getWrapped().contains("currentAmount") ? "currentAmount" : "stackSize") * multiplier;

                    int maxStackSize = stack.getMaximumStackSize();

                    int DKs = currentAmount / (maxStackSize * 9 * 6);
                    int rest = currentAmount - (DKs * maxStackSize * 9 * 6);

                    int stacks = rest / maxStackSize;
                    int items = rest - (stacks * maxStackSize);


                    event.getTooltipLines().add(Component.translatable("tmbgriefergames.tooltip.compressedTooltip", Component.text(DKs), Component.text(stacks), Component.text(items)));
                }
            } else if(Addon.getSharedInstance().configuration().getTooltipConfig().getShowAdventurerTooltip().get() && stack.getDataComponentContainer().has(DataComponentKey.simple("adventure"))) {
                if(Laby.labyAPI().minecraft().getClientPlayer() != null && Laby.labyAPI().minecraft().isIngame()) {
                    String adventureTooltip = AdventureWidget.getAdventurerForItemStack(stack, true);
                    if(!adventureTooltip.isEmpty()) {
                        event.getTooltipLines().add(TextComponent.builder().text(adventureTooltip).build());
                    }
                }
            } else if(stack.getDataComponentContainer().get(DataComponentKey.simple("EntityTag")) instanceof NBTTagCompound entityTagContainer) {
                if(entityTagContainer.getString("id") instanceof String idString) {
                    String translated = I18n.translate("tmbgriefergames.mobs." + idString.replace("minecraft:", ""));
                    if(translated.isEmpty())
                        translated = capitalizeFirstLetter(idString.replace("minecraft:", ""));

                    event.getTooltipLines().add(TextComponent.builder().text("§f" + I18n.translate("tmbgriefergames.mobs.spawn") + ": §a" + translated + "§a§l✔").build());
                }
            }
        }
    }

    private int getEndlessChestAmount(NBTTagCompound compound) {
        if(compound.contains("display")) {
            NBTTagCompound display = compound.getCompound("display");
            if(display.contains("Lore")) {
                NBTTagList<Object, NBTTag<Object>> lore = display.getList("Lore", NBTTagType.STRING);

                for (NBTTag<Object> tag : lore.tags()) {
                    if(tag.value() instanceof String str) {
                        if(str.startsWith("§e") && str.endsWith(" Verfügbar")) {
                            str = str.replace("§e", "").replace(" Verfügbar", "").replace(".", "");
                            return Integer.parseInt(str);
                        }
                    }
                }
            }
        }

        return -1;
    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}