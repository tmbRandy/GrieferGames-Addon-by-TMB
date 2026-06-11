package tmb.randy.tmbgriefergames.v1_12_2;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import tmb.randy.tmbgriefergames.v1_12_2.enums.CompressionLevel;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Helper {
    public static EntityPlayerSP getPlayer() {
        return Minecraft.getMinecraft().player;
    }

    public static WorldClient getWorld() {
        return Minecraft.getMinecraft().world;
    }

    public static BlockPos getBlockPosLookingAt() {
        RayTraceResult trace = getPlayer().rayTrace(5, 1.0F);
        if(trace != null && trace.typeOfHit == Type.BLOCK)
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
        Vec3d playerPos = getPlayer().getPositionEyes(1.0F);
        Vec3d targetPos;

        if (facing == null) {
            targetPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        } else {
            targetPos = new Vec3d(
                pos.getX() + 0.5 + facing.getDirectionVec().getX() * 0.5,
                pos.getY() + 0.5 + facing.getDirectionVec().getY() * 0.5,
                pos.getZ() + 0.5 + facing.getDirectionVec().getZ() * 0.5
            );
        }

        double diffX = targetPos.x - playerPos.x;
        double diffY = targetPos.y - playerPos.y;
        double diffZ = targetPos.z - playerPos.z;
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
        Minecraft.getMinecraft().playerController.processRightClick(getPlayer(), getWorld(), EnumHand.MAIN_HAND);
    }

    public static boolean isInventoryFull() {
        InventoryPlayer inventory = getPlayer().inventory;

        for (int i = 0; i < inventory.mainInventory.size(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty())
                return false;
        }

        return true;
    }

    public static boolean isInventoryEmpty() {
        InventoryPlayer inventory = getPlayer().inventory;

        for (int i = 0; i < inventory.mainInventory.size(); i++) {
            ItemStack stack = inventory.mainInventory.get(i);
            if (!stack.isEmpty())
                return false;
        }

        return true;
    }

    public static int getFreeSlots() {
        InventoryPlayer inventory = getPlayer().inventory;
        int freeSlots = 0;

        for (int i = 0; i < inventory.mainInventory.size(); i++) {
            ItemStack stack = inventory.mainInventory.get(i);
            if (stack.isEmpty())
                freeSlots++;
        }

        return freeSlots;
    }

    public static int getItemSlotCount(Item item, int meta) {
        InventoryPlayer inventory = getPlayer().inventory;
        int slotCount = 0;

        for (int i = 0; i < inventory.mainInventory.size(); i++) {
            ItemStack stack = inventory.mainInventory.get(i);
            if (!stack.isEmpty() && stack.getItem() == item && stack.getMetadata() == meta)
                slotCount++;
        }

        return slotCount;
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

    public static BlockPos getPlacementPosition() {
        RayTraceResult rayTraceResult = getPlayer().rayTrace(5, 1.0F);

        if (rayTraceResult != null && rayTraceResult.typeOfHit == Type.BLOCK) {
            BlockPos targetBlockPos = rayTraceResult.getBlockPos();
            EnumFacing sideHit = rayTraceResult.sideHit;
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

            return new BlockPos(targetBlockPos.getX() + offsetX, targetBlockPos.getY() + offsetY, targetBlockPos.getZ() + offsetZ);
        } else {
            return null;
        }
    }

    public static int getSlotForItem(Item item, int meta, CompressionLevel maxCompressionLevel) {
        InventoryPlayer inventory = getPlayer().inventory;

        for (int i = 0; i < inventory.mainInventory.size(); i++) {
            ItemStack stack = inventory.mainInventory.get(i);
            if (!stack.isEmpty() && stack.getItem() == item && (stack.getMetadata() == meta || meta == -1)) {
                CompressionLevel level = CompressionLevel.fromItemStack(stack);

                if(level.ordinal() <= maxCompressionLevel.ordinal())
                    return i;
            }
        }

        return -1;
    }

    public static int getSlotForItem(Item item, int meta) {
        InventoryPlayer inventory = getPlayer().inventory;

        for (int i = 0; i < inventory.mainInventory.size(); i++) {
            ItemStack stack = inventory.mainInventory.get(i);
            if (!stack.isEmpty() && stack.getItem() == item && (stack.getMetadata() == meta || meta == -1))
                return i;
        }

        return -1;
    }

    public static int findLowestCompressionSlot(ItemStack target) {
        int slotWithLowest = -1;
        CompressionLevel lowest = null;

        for (int i = 0; i < getPlayer().inventory.mainInventory.size(); i++) {
            ItemStack stack = getPlayer().inventory.mainInventory.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() == target.getItem() && stack.getMetadata() == target.getMetadata()) {
                CompressionLevel level = CompressionLevel.fromItemStack(stack);

                if (lowest == null || level.getLevel() < lowest.getLevel()) {
                    lowest = level;
                    slotWithLowest = i;

                    if (lowest == CompressionLevel.NONE) {
                        return slotWithLowest;
                    }
                }
            }
        }

        return slotWithLowest;
    }
}
