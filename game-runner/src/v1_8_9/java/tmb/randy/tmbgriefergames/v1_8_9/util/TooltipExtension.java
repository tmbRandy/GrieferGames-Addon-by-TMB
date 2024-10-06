package tmb.randy.tmbgriefergames.v1_8_9.util;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.component.data.DataComponentKey;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import tmb.randy.tmbgriefergames.core.Addon;

public class TooltipExtension {


    public void renderTooltip(ItemStackTooltipEvent event) {
        if(!Addon.isGG()) {
            return;
        }

        ItemStack stack = event.itemStack();

        if(stack.hasDataComponentContainer()) {

            if(Addon.getSharedInstance().configuration().getTooltipConfig().getShowCompTooltip().get() && (stack.getDataComponentContainer().has(
                DataComponentKey.simple("currentAmount")) || stack.getDataComponentContainer().has(DataComponentKey.simple("stackSize")))) {

                int currentAmount;

                if(stack.getDataComponentContainer().has(DataComponentKey.simple("currentAmount"))) {
                    currentAmount = ((NBTTagInt)stack.getDataComponentContainer().get(DataComponentKey.simple("currentAmount"))).getInt() * stack.getSize();
                } else {
                    currentAmount = ((NBTTagInt)stack.getDataComponentContainer().get(DataComponentKey.simple("stackSize"))).getInt() * stack.getSize();
                }

                int maxStackSize = stack.getMaximumStackSize();

                int DKs = currentAmount / (maxStackSize * 9 * 6);
                int rest = currentAmount - (DKs * maxStackSize * 9 * 6);

                int stacks = rest / maxStackSize;
                int items = rest - (stacks * maxStackSize);


                event.getTooltipLines().add(Component.translatable("tmbgriefergames.tooltip.compressedTooltip", Component.text(DKs), Component.text(stacks), Component.text(items)));
            } else if(Addon.getSharedInstance().configuration().getTooltipConfig().getShowAdventurerTooltip().get() && stack.getDataComponentContainer().has(DataComponentKey.simple("adventure"))) {

                int amount = ((NBTTagCompound)stack.getDataComponentContainer().get(DataComponentKey.simple("adventure"))).getInteger("adventure.amount");
                int total = ((NBTTagCompound)stack.getDataComponentContainer().get(DataComponentKey.simple("adventure"))).getInteger("adventure.req_amount");

                float percent = 100f / (float)total * (float)amount;

                int needed = total - amount;

                int DKs = needed / (64 * 9 * 6);
                int rest = needed - (DKs * 64 * 9 * 6);

                int stacks = rest / 64;
                int items = rest - (stacks * 64);

                float percentRounded = Math.round(percent * 10) / 10.0f;
                event.getTooltipLines().add(Component.translatable("tmbgriefergames.tooltip.adventurerTooltip", Component.text(percentRounded), Component.text(DKs), Component.text(stacks), Component.text(items)));
            }
        }
    }
}
