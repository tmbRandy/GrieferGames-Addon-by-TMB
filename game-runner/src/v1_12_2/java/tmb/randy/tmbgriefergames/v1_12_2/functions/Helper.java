package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;

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

    public static void lookAtBlockPos(BlockPos pos) {
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
    }

    public static void rightClick() {
        Minecraft.getMinecraft().playerController.processRightClick(getPlayer(), getWorld(), EnumHand.MAIN_HAND);
    }
}
