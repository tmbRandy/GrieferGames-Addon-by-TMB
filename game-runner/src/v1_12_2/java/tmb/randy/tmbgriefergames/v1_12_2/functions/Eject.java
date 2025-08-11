package tmb.randy.tmbgriefergames.v1_12_2.functions;

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
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;
import tmb.randy.tmbgriefergames.v1_12_2.click.ClickManager;

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
                            RayTraceResult rayTraceResult = Minecraft.getMinecraft().objectMouseOver;
                            if(rayTraceResult.typeOfHit == Type.BLOCK && rayTraceResult.getBlockPos().equals(chestPos)) {
                                Minecraft.getMinecraft().playerController.processRightClickBlock(Helper.getPlayer(), Helper.getWorld(), rayTraceResult.getBlockPos(), rayTraceResult.sideHit, rayTraceResult.hitVec, EnumHand.MAIN_HAND);
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
        RayTraceResult rayTraceResult = Minecraft.getMinecraft().objectMouseOver;

        if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos blockPosition = rayTraceResult.getBlockPos();
            IBlockState blockState = Helper.getWorld().getBlockState(blockPosition);

            if (blockState.getBlock() instanceof BlockChest) {
                return blockPosition;
            }
        }

        return null;
    }
}