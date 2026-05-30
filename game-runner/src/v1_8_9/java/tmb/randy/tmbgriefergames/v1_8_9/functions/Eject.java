package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.EjectMaster;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;
import tmb.randy.tmbgriefergames.v1_8_9.click.ClickManager;

public class Eject extends EjectMaster<BlockPos> {

    @Override
    protected boolean isStorageMenuOpen() {
        return Helper.getPlayer().openContainer instanceof ContainerChest chest && isStorageMenuName(chest.getLowerChestInventory().getName());
    }

    @Override
    protected boolean isClickQueueEmpty() {
        return ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM);
    }

    @Override
    protected void dropStorageItems() {
        if(Helper.getPlayer().openContainer instanceof ContainerChest chest)
            for (int i = 0; i < 25; i++) {
                Slot slot = chest.getSlot(i);
                if(slot.getHasStack())
                    ClickManager.getSharedInstance().dropClick(i);
            }
    }

    @Override
    protected void closeScreen() {
        Minecraft.getMinecraft().displayGuiScreen(null);
        Helper.getPlayer().closeScreen();
    }

    @Override
    protected boolean isStillLookingAt(BlockPos pos) {
        MovingObjectPosition rayTraceResult = Minecraft.getMinecraft().objectMouseOver;
        return rayTraceResult.typeOfHit == MovingObjectType.BLOCK && rayTraceResult.getBlockPos().equals(pos);
    }

    @Override
    protected void reopenChest(BlockPos pos) {
        MovingObjectPosition rayTraceResult = Minecraft.getMinecraft().objectMouseOver;
        Minecraft.getMinecraft().playerController.onPlayerRightClick(Helper.getPlayer(), Helper.getWorld(), Helper.getPlayer().getHeldItem(), pos, rayTraceResult.sideHit, rayTraceResult.hitVec);
    }

    @Override
    protected void clearClickQueues() {
        ClickManager.getSharedInstance().clearAllQueues();
    }

    @Override
    protected BlockPos getChestPos() {
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