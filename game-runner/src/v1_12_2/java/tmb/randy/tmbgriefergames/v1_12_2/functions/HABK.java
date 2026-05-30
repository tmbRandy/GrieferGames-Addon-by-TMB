package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import tmb.randy.tmbgriefergames.core.functions.HABKMaster;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

public class HABK extends HABKMaster {

    @Override
    protected int getSelectedSlot() {
        return Helper.getPlayer().inventory.currentItem;
    }

    @Override
    protected void setSelectedSlot(int slot) {
        Helper.getPlayer().inventory.currentItem = slot;
    }

    @Override
    protected Object getStackInSlot(int slot) {
        return Helper.getPlayer().inventory.getStackInSlot(slot);
    }

    @Override
    protected boolean isBow(Object stack) {
        return stack instanceof ItemStack itemStack && itemStack.getItem() instanceof ItemBow;
    }

    @Override
    protected boolean isSword(Object stack) {
        return stack instanceof ItemStack itemStack && itemStack.getItem() instanceof ItemSword;
    }

    @Override
    protected String getEnchantments(Object stack) {
        ItemStack itemStack = (ItemStack) stack;
        if(itemStack != null && itemStack.getTagCompound() != null) {
            if(itemStack.getTagCompound().hasKey("ench")) {
                return itemStack.getTagCompound().getTag("ench").toString();
            }
        }
        return null;
    }
}