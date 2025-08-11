package tmb.randy.tmbgriefergames.v1_12_2;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import tmb.randy.tmbgriefergames.v1_12_2.enums.CompressionLevel;

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

    public static boolean lookAtBlockPos(BlockPos pos) {
        Vec3d playerPos = getPlayer().getPositionEyes(1.0F);
        Vec3d targetPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        double diffX = targetPos.x - playerPos.x;
        double diffY = targetPos.y - playerPos.y;
        double diffZ = targetPos.z - playerPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * (180 / Math.PI)) - 90.0F;
        float pitch = (float) (-(Math.atan2(diffY, diffXZ) * (180 / Math.PI)));

        getPlayer().rotationYaw = yaw;
        getPlayer().rotationPitch = pitch;

        return pos.equals(getBlockPosLookingAt());
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

    public static int getSlotForItem(Item item, int meta, CompressionLevel maxCompressionLevel) {
        InventoryPlayer inventory = getPlayer().inventory;

        for (int i = 0; i < inventory.mainInventory.size(); i++) {
            ItemStack stack = inventory.mainInventory.get(i);
            if (!stack.isEmpty() && stack.getItem() == item && (stack.getMetadata() == meta || meta == -1)) {
                CompressionLevel level = CompressionLevel.fromStack(stack);

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
}
