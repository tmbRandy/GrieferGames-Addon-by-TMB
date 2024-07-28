package tmb.randy.tmbgriefergames.v1_12_2.util;

import net.labymod.api.event.Phase;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import tmb.randy.tmbgriefergames.core.Addon;

public class VABK {
    private int cooldown = 0;
    private boolean active;

    public void onTickEvent(GameTickEvent event) {
        if(Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null)
            return;

        if (event.phase() == Phase.PRE && active) {
            cooldown++;

            if (cooldown >= (Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKswitchCooldown().get() + Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKloadTime().get())) {
                cooldown = 0;
                shoot();
            } else if (cooldown == Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKswitchCooldown().get() - 2) {
                Minecraft.getMinecraft().player.inventory.currentItem = 2;
            } else if (cooldown == Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKswitchCooldown().get()) {
                startUsingBow();
            }
        }
    }

    public void onKeyEvent(KeyEvent event) {
        if(event.state() == State.PRESS && VersionisedBridge.allKeysPressed(Addon.getSharedInstance().configuration().getSwordsSubConfig().getVABKhotkey().get()) && !VersionisedBridge.isChatGuiOpen()) {
            toggleActive();
        }
    }

    public void toggleActive() {
        active = !active;
        Addon.getSharedInstance().displayNotification(I18n.getTranslation(active ? "tmbgriefergames.autoSword.enabled" : "tmbgriefergames.autoSword.disabled"));
    }

    public void stop() {
        if(active) {
            toggleActive();
        }
    }

    private void startUsingBow() {
        if(Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null)
            return;

        ItemStack heldItem = Minecraft.getMinecraft().player.getHeldItemMainhand();

        if (heldItem != null && heldItem.getItem() instanceof ItemBow)
            Minecraft.getMinecraft().playerController.processRightClick(Minecraft.getMinecraft().player, Minecraft.getMinecraft().world, EnumHand.MAIN_HAND);
    }

    private void shoot() {
        if (Minecraft.getMinecraft().player == null)
            return;

        if (Minecraft.getMinecraft().player.isHandActive() && Minecraft.getMinecraft().player.getActiveItemStack().getItem() instanceof ItemBow) {
            int useDuration = Minecraft.getMinecraft().player.getItemInUseCount();

            if (useDuration >= 20) {
                Minecraft.getMinecraft().player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                Minecraft.getMinecraft().player.inventory.currentItem = 0;
            }
        }
    }
}

