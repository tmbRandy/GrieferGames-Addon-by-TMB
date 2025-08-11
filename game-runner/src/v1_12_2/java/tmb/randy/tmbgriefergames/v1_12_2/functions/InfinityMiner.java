package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Keyboard;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

public class InfinityMiner extends ActiveFunction {
    private boolean didBreakLastTick;
    private ItemStack currentItem;

    public InfinityMiner() {
        super(Functions.INFINITYMINER);
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if (isEnabled())
            breakBlock();
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (event.state() == State.PRESS && Addon.getSharedInstance().allKeysPressed(Addon.getSharedInstance().configuration().getInfinityMiner().get())) {
            if (!isEnabled() && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                return;

            toggle();
        }
    }

    public void breakBlock() {
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