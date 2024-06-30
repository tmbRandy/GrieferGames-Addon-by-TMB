package tmb.randy.tmbgriefergames.v1_12_2.util;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.util.I18n;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.v1_12_2.util.click.ClickManager;

public class Auswurf {
    private boolean active;
    private BlockPos chestPos;

    public void onTickEvent(GameTickEvent event) {
        if(active && Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().world != null) {
            if(Minecraft.getMinecraft().player.openContainer instanceof ContainerChest chest) {
                IInventory inv = chest.getLowerChestInventory();
                if(inv.getName().startsWith("§0Lager: ")) {
                    if(chestPos == null) {
                        chestPos = getChestPos();
                    } else {
                        if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                            for (int i = 0; i < 25; i++) {
                                Slot slot = chest.getSlot(i);
                                if(slot.getHasStack()) {
                                    ClickManager.getSharedInstance().dropClick(i);
                                }
                            }

                            if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                                Minecraft.getMinecraft().displayGuiScreen(null);
                                Minecraft.getMinecraft().player.closeScreen();
                                RayTraceResult rayTraceResult = Minecraft.getMinecraft().objectMouseOver;
                                if(rayTraceResult.typeOfHit == Type.BLOCK && rayTraceResult.getBlockPos().equals(chestPos)) {
                                    Minecraft.getMinecraft().playerController.processRightClickBlock(Minecraft.getMinecraft().player, Minecraft.getMinecraft().world, chestPos, rayTraceResult.sideHit, rayTraceResult.hitVec, EnumHand.MAIN_HAND);
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    public void onKeyEvent(KeyEvent event) {
        if(event.state() == State.PRESS) {
            if (active && event.key() == Key.ESCAPE) {
                ClickManager.getSharedInstance().clearAllQueues();
                Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.eject.disabled"));
                chestPos = null;
                active = false;
            }
        }
    }

    public void startAuswurf() {
        Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.eject.enabled"));
        active = true;
    }

    private static BlockPos getChestPos() {
        Minecraft mc = Minecraft.getMinecraft();
        RayTraceResult rayTraceResult = mc.objectMouseOver;

        if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos blockPosition = rayTraceResult.getBlockPos();
            IBlockState blockState = mc.world.getBlockState(blockPosition);

            if (blockState.getBlock() instanceof BlockChest) {
                return blockPosition;
            }
        }

        return null;
    }

    private static Entity getEntityInBlockPos(BlockPos pos) {
        Minecraft mc = Minecraft.getMinecraft();
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityLivingBase && entity.getDistanceSq(pos) < 2) {
                return entity;
            }
        }
        return null;
    }
}
