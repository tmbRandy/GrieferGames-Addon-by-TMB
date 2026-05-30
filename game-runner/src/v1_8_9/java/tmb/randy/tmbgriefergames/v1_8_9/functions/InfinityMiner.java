package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import tmb.randy.tmbgriefergames.core.functions.InfinityMinerMaster;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

public class InfinityMiner extends InfinityMinerMaster {

    @Override
    protected void breakBlock() {
        if(Helper.getPlayer().getHeldItem() != null && Helper.getPlayer().getHeldItem().getItemDamage() < Helper.getPlayer().getHeldItem().getMaxDamage()) {
            MovingObjectPosition trace = Helper.getPlayer().rayTrace(5, 1.0F);

            if(trace != null && trace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (!didBreakLastTick) {
                    Minecraft.getMinecraft().playerController.clickBlock(trace.getBlockPos(), trace.sideHit);
                    Helper.getPlayer().swingItem();
                }

                // Attempt to break the block
                if (Minecraft.getMinecraft().playerController.onPlayerDamageBlock(trace.getBlockPos(), trace.sideHit)) {
                    Helper.getPlayer().swingItem();
                }

                didBreakLastTick = true;
            }
        }
    }
}