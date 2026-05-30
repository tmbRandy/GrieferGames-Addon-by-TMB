package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.core.functions.VABKMaster;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

public class VABK extends VABKMaster {

    @Override
    protected void setSelectedSlot(int slot) {
        Helper.getPlayer().inventory.currentItem = slot;
    }

    @Override
    protected void startUsingBow() {
        ItemStack heldItem = Helper.getPlayer().getHeldItem();

        if (heldItem != null && heldItem.getItem() instanceof ItemBow)
            Helper.rightClick();
    }

    @Override
    protected void shoot() {
        ItemStack heldItem = Helper.getPlayer().getHeldItem();

        if (heldItem != null && heldItem.getItem() instanceof ItemBow)
            Minecraft.getMinecraft().playerController.onStoppedUsingItem(Helper.getPlayer());

        setSelectedSlot(0);
    }
}