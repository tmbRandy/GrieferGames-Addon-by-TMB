package tmb.randy.tmbgriefergames.v1_12_2.functions;

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
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.EjectMaster;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;
import tmb.randy.tmbgriefergames.v1_12_2.click.ClickManager;

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
        RayTraceResult rayTraceResult = Minecraft.getMinecraft().objectMouseOver;
        return rayTraceResult.typeOfHit == Type.BLOCK && rayTraceResult.getBlockPos().equals(pos);
    }

    @Override
    protected void reopenChest(BlockPos pos) {
        RayTraceResult rayTraceResult = Minecraft.getMinecraft().objectMouseOver;
        Minecraft.getMinecraft().playerController.processRightClickBlock(Helper.getPlayer(), Helper.getWorld(), rayTraceResult.getBlockPos(), rayTraceResult.sideHit, rayTraceResult.hitVec, EnumHand.MAIN_HAND);
    }

    @Override
    protected void clearClickQueues() {
        ClickManager.getSharedInstance().clearAllQueues();
    }

    @Override
    protected BlockPos getChestPos() {
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