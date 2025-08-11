package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.labymod.api.event.Phase;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

public class VABK extends ActiveFunction {
    private int cooldown = 0;

    public VABK() {
        super(Functions.VABK);
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if (event.phase() == Phase.PRE && isEnabled()) {
            cooldown++;

            if (cooldown >= (Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKswitchCooldown().get() + Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKloadTime().get())) {
                cooldown = 0;
                shoot();
            } else if (cooldown == Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKswitchCooldown().get() - 2) {
                Helper.getPlayer().inventory.currentItem = 2;
            } else if (cooldown == Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKswitchCooldown().get()) {
                startUsingBow();
            }
        }
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if(event.state() == State.PRESS && Addon.getSharedInstance().allKeysPressed(Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKhotkey().get()) && Addon.getSharedInstance().isChatGuiClosed()) {
            toggle();
        }
    }

    private void startUsingBow() {
        ItemStack heldItem = Helper.getPlayer().getHeldItemMainhand();

        if (heldItem != null && heldItem.getItem() instanceof ItemBow)
            Helper.rightClick();
    }

    private void shoot() {
        if (Helper.getPlayer().isHandActive() && Helper.getPlayer().getActiveItemStack().getItem() instanceof ItemBow) {
            int useDuration = Helper.getPlayer().getItemInUseCount();

            if (useDuration >= 20) {
                Helper.getPlayer().connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                Helper.getPlayer().inventory.currentItem = 0;
            }
        }
    }
}