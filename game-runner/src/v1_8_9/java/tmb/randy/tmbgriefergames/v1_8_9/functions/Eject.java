package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;
import tmb.randy.tmbgriefergames.v1_8_9.click.ClickManager;

public class Eject extends ActiveFunction {
    private BlockPos chestPos;

    public Eject() {
        super(Functions.EJECT);
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if(Helper.getPlayer().openContainer instanceof ContainerChest chest && isEnabled()) {
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
                            Helper.getPlayer().closeScreen();
                            MovingObjectPosition rayTraceResult = Minecraft.getMinecraft().objectMouseOver;
                            if(rayTraceResult.typeOfHit == MovingObjectType.BLOCK && rayTraceResult.getBlockPos().equals(chestPos)) {
                                Minecraft.getMinecraft().playerController.onPlayerRightClick(Helper.getPlayer(), Helper.getWorld(), Helper.getPlayer().getHeldItem(), chestPos, rayTraceResult.sideHit, rayTraceResult.hitVec);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if(event.state() == State.PRESS && event.key() == Key.ESCAPE) {
            if(super.stop()) {
                ClickManager.getSharedInstance().clearAllQueues();
                chestPos = null;
            }
        }
    }

    private static BlockPos getChestPos() {
        MovingObjectPosition rayTraceResult = Minecraft.getMinecraft().objectMouseOver;

        if (rayTraceResult != null && rayTraceResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos blockPosition = rayTraceResult.getBlockPos();
            IBlockState blockState = Helper.getWorld().getBlockState(blockPosition);

            if (blockState.getBlock() instanceof BlockChest) {
                return blockPosition;
            }
        }

        return null;
    }
}