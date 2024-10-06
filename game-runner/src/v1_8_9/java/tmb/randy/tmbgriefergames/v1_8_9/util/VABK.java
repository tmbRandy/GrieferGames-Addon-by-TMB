package tmb.randy.tmbgriefergames.v1_8_9.util;

import net.labymod.api.event.Phase;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.core.Addon;

public class VABK {
    private int cooldown = 0;
    private boolean active;

    public void onTickEvent(GameTickEvent event) {
        if(Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null)
            return;

        if (event.phase() == Phase.PRE && active) {
            cooldown++;

            if (cooldown >= (Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKswitchCooldown().get() + Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKloadTime().get())) {
                cooldown = 0;
                shoot();
            } else if (cooldown == Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKswitchCooldown().get() - 2) {
                Minecraft.getMinecraft().thePlayer.inventory.currentItem = 2;
            } else if (cooldown == Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKswitchCooldown().get()) {
                startUsingBow();
            }
        }
    }

    public void onKeyEvent(KeyEvent event) {
        if(event.state() == State.PRESS && VersionisedBridge.getSharedInstance().allKeysPressed(Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKhotkey().get()) && !VersionisedBridge.getSharedInstance().isChatGuiOpen()) {
            toggleActive();
        }
    }

    public void toggleActive() {
        active = !active;
        Addon.getSharedInstance().displayNotification(
            I18n.getTranslation(active ? "tmbgriefergames.autoSword.enabled" : "tmbgriefergames.autoSword.disabled"));
    }

    public void stop() {
        if(active) {
            toggleActive();
        }
    }

    private void startUsingBow() {
        if(Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null)
            return;

        ItemStack heldItem = Minecraft.getMinecraft().thePlayer.getHeldItem();

        if (heldItem != null && heldItem.getItem() instanceof ItemBow)
            Minecraft.getMinecraft().playerController.sendUseItem(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().theWorld, heldItem);
    }

    private void shoot() {
        if(Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null)
            return;

        ItemStack heldItem = Minecraft.getMinecraft().thePlayer.getHeldItem();

        if (heldItem != null && heldItem.getItem() instanceof ItemBow)
            Minecraft.getMinecraft().playerController.onStoppedUsingItem(Minecraft.getMinecraft().thePlayer);

        Minecraft.getMinecraft().thePlayer.inventory.currentItem = 0;
    }
}

