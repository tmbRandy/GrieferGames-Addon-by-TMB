package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import tmb.randy.tmbgriefergames.core.functions.VABKMaster;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

public class VABK extends VABKMaster {

    @Override
    protected void setSelectedSlot(int slot) {
        Helper.getPlayer().inventory.currentItem = slot;
    }

    @Override
    protected void startUsingBow() {
        ItemStack heldItem = Helper.getPlayer().getHeldItemMainhand();

        if (heldItem != null && heldItem.getItem() instanceof ItemBow)
            Helper.rightClick();
    }

    @Override
    protected void shoot() {
        if (Helper.getPlayer().isHandActive() && Helper.getPlayer().getActiveItemStack().getItem() instanceof ItemBow) {
            int useDuration = Helper.getPlayer().getItemInUseCount();

            if (useDuration >= 20) {
                Helper.getPlayer().connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                setSelectedSlot(0);
            }
        }
    }
}