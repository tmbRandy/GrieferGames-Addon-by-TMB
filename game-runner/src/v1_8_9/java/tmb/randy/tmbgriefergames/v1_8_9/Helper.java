package tmb.randy.tmbgriefergames.v1_8_9;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import tmb.randy.tmbgriefergames.v1_8_9.enums.CompressionLevel;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Helper {
    public static EntityPlayerSP getPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    public static WorldClient getWorld() {
        return Minecraft.getMinecraft().theWorld;
    }

    public static BlockPos getBlockPosLookingAt() {
        MovingObjectPosition trace = getPlayer().rayTrace(5, 1.0F);
        if(trace != null && trace.typeOfHit == MovingObjectType.BLOCK)
            return trace.getBlockPos();

        return null;
    }

    public static Block getBlockLookingAt() {
        BlockPos pos = getBlockPosLookingAt();
        if(pos != null && getWorld() != null)
            return getWorld().getBlockState(pos).getBlock();

        return null;
    }

    public static void lookAtBlockPos(BlockPos pos, EnumFacing facing) {
        Vec3 playerPos = getPlayer().getPositionEyes(1.0F);
        Vec3 targetPos;

        if (facing == null)
            targetPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        else
            targetPos = new Vec3(pos.getX() + 0.5 + facing.getDirectionVec().getX() * 0.5, pos.getY() + 0.5 + facing.getDirectionVec().getY() * 0.5, pos.getZ() + 0.5 + facing.getDirectionVec().getZ() * 0.5);

        double diffX = targetPos.xCoord - playerPos.xCoord;
        double diffY = targetPos.yCoord - playerPos.yCoord;
        double diffZ = targetPos.zCoord - playerPos.zCoord;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * (180 / Math.PI)) - 90.0F;
        float pitch = (float) (-(Math.atan2(diffY, diffXZ) * (180 / Math.PI)));

        getPlayer().rotationYaw = yaw;
        getPlayer().rotationPitch = pitch;
    }

    public static void lookAtBlockPos(BlockPos pos) {
        lookAtBlockPos(pos, null);
    }

    public static void rightClick() {
        Minecraft.getMinecraft().playerController.sendUseItem(getPlayer(), getWorld(), getPlayer().getHeldItem());
    }

    public static boolean isInventoryFull() {
        InventoryPlayer inventory = getPlayer().inventory;

        for (int i = 0; i < inventory.mainInventory.length; i++) {
            ItemStack stack = inventory.mainInventory[i];
            if (stack == null)
                return false;
        }

        return true;
    }

    public static boolean isInventoryEmpty() {
        InventoryPlayer inventory = getPlayer().inventory;

        for (int i = 0; i < inventory.mainInventory.length; i++) {
            ItemStack stack = inventory.mainInventory[i];
            if (stack != null)
                return false;
        }

        return true;
    }

    public static void closeScreen() {
        Minecraft.getMinecraft().displayGuiScreen(null);
        getPlayer().closeScreen();
    }

    public static String formatDouble(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat format = new DecimalFormat("#,###", symbols);
        format.setGroupingUsed(true);
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);

        return format.format(value);
    }

    public static int getFreeSlots() {
        InventoryPlayer inventory = getPlayer().inventory;
        int freeSlots = 0;

        for (int i = 0; i < inventory.mainInventory.length; i++) {
            ItemStack stack = inventory.mainInventory[i];
            if (stack == null)
                freeSlots++;
        }

        return freeSlots;
    }

    public static int getItemSlotCount(Item item, int meta) {
        InventoryPlayer inventory = getPlayer().inventory;
        int slotCount = 0;

        for (int i = 0; i < inventory.mainInventory.length; i++) {
            ItemStack stack = inventory.mainInventory[i];
            if (stack != null && stack.getItem() == item && stack.getMetadata() == meta)
                slotCount++;
        }

        return slotCount;
    }

    public static int getSlotForItem(Item item, int meta, CompressionLevel maxCompressionLevel) {
        InventoryPlayer inventory = getPlayer().inventory;

        for (int i = 0; i < inventory.mainInventory.length; i++) {
            ItemStack stack = inventory.mainInventory[i];
            if (stack != null && stack.getItem() == item && (stack.getMetadata() == meta || meta == -1)) {
                CompressionLevel level = CompressionLevel.fromItemStack(stack);

                if(level.ordinal() <= maxCompressionLevel.ordinal())
                    return i;
            }
        }

        return -1;
    }

    public static int getSlotForItem(Item item, int meta) {
        InventoryPlayer inventory = getPlayer().inventory;

        for (int i = 0; i < inventory.mainInventory.length; i++) {
            ItemStack stack = inventory.mainInventory[i];
            if (stack != null && stack.getItem() == item && (stack.getMetadata() == meta || meta == -1))
                    return i;
        }

        return -1;
    }

    public static BlockPos getPlacementPosition() {
        MovingObjectPosition mop = getPlayer().rayTrace(5, 1.0F);

        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            int targetX = mop.getBlockPos().getX();
            int targetY = mop.getBlockPos().getY();
            int targetZ = mop.getBlockPos().getZ();
            EnumFacing sideHit = mop.sideHit;
            int offsetX = 0;
            int offsetY = 0;
            int offsetZ = 0;

            switch (sideHit) {
                case UP:
                    offsetY = 1;
                    break;
                case DOWN:
                    offsetY = -1;
                    break;
                case NORTH:
                    offsetZ = -1;
                    break;
                case SOUTH:
                    offsetZ = 1;
                    break;
                case WEST:
                    offsetX = -1;
                    break;
                case EAST:
                    offsetX = 1;
                    break;
            }

            return new BlockPos(targetX + offsetX, targetY + offsetY, targetZ + offsetZ);
        } else {
            return null;
        }
    }
}
