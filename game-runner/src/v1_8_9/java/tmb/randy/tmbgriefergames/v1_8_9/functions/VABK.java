package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.labymod.api.event.Phase;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

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
        if(event.state() == State.PRESS && Addon.getSharedInstance().allKeysPressed(Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKhotkey().get()) && Addon.getSharedInstance().isChatGuiClosed())
            toggle();
    }

    private void startUsingBow() {
        ItemStack heldItem = Helper.getPlayer().getHeldItem();

        if (heldItem != null && heldItem.getItem() instanceof ItemBow)
            Helper.rightClick();
    }

    private void shoot() {
        ItemStack heldItem = Helper.getPlayer().getHeldItem();

        if (heldItem != null && heldItem.getItem() instanceof ItemBow)
            Minecraft.getMinecraft().playerController.onStoppedUsingItem(Helper.getPlayer());

        Helper.getPlayer().inventory.currentItem = 0;
    }
}