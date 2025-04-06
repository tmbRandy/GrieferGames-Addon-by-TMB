package tmb.randy.tmbgriefergames.v1_8_9;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;

public class Helper {
    public static EntityPlayerSP getPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    public static WorldClient getWorld() {
        return Minecraft.getMinecraft().theWorld;
    }

    public static BlockPos getBlockLookingAt() {
        MovingObjectPosition trace = getPlayer().rayTrace(5, 1.0F);
        if(trace != null && trace.typeOfHit == MovingObjectType.BLOCK) {
            return trace.getBlockPos();
        }

        return null;
    }

    public static void lookAtBlockPos(BlockPos pos) {
        Vec3 playerPos = getPlayer().getPositionEyes(1.0F);
        Vec3 targetPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        double diffX = targetPos.xCoord - playerPos.xCoord;
        double diffY = targetPos.yCoord - playerPos.yCoord;
        double diffZ = targetPos.zCoord - playerPos.zCoord;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * (180 / Math.PI)) - 90.0F;
        float pitch = (float) (-(Math.atan2(diffY, diffXZ) * (180 / Math.PI)));

        getPlayer().rotationYaw = yaw;
        getPlayer().rotationPitch = pitch;
    }

    public static void rightClick() {
        Minecraft.getMinecraft().playerController.sendUseItem(getPlayer(), getWorld(), getPlayer().getHeldItem());
    }
}
