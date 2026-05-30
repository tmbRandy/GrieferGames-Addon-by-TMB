package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import tmb.randy.tmbgriefergames.core.functions.InfinityMinerMaster;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

public class InfinityMiner extends InfinityMinerMaster {

    @Override
    protected void breakBlock() {
        if(Helper.getPlayer().getHeldItemMainhand().getItemDamage() < Helper.getPlayer().getHeldItemMainhand().getMaxDamage()) {
            RayTraceResult trace = Helper.getPlayer().rayTrace(5, 1.0F);

            if(trace != null) {
                if (!didBreakLastTick) {
                    Minecraft.getMinecraft().playerController.clickBlock(trace.getBlockPos(), trace.sideHit);
                    Helper.getPlayer().swingArm(EnumHand.MAIN_HAND);
                }

                // Attempt to break the block
                if (Minecraft.getMinecraft().playerController.onPlayerDamageBlock(trace.getBlockPos(), trace.sideHit))
                    Helper.getPlayer().swingArm(EnumHand.MAIN_HAND);

                didBreakLastTick = true;
            }
        }
    }
}