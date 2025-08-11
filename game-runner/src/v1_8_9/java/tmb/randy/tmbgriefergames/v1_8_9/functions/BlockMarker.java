package tmb.randy.tmbgriefergames.v1_8_9.functions;

import java.util.HashSet;
import java.util.Set;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

public class BlockMarker extends Function {

    public BlockMarker() {
        super(Functions.BLOCKMARKER);
    }

    @Override
    public void renderWorldEvent(RenderWorldEvent event) {
        if(Addon.getSharedInstance().configuration().getBlockMarker().get()) {
            ItemStack heldItem = Helper.getPlayer().getHeldItem();
            if (heldItem == null || !heldItem.hasTagCompound())
                return;

            NBTTagCompound tag = heldItem.getTagCompound();
            if (tag == null || (!tag.hasKey("orb_pickaxe") && !tag.hasKey("orb_shovel")))
                return;

            int size =  3;

            if(tag.hasKey("orb_tool_level")) {
                size = switch (tag.getString("orb_tool_level")) {
                    case "LARGE" -> 7;
                    case "MEDIUM" -> 5;
                    default -> 3;
                };
            }

            MovingObjectPosition rayTrace = Minecraft.getMinecraft().objectMouseOver;
            if (rayTrace == null || rayTrace.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
                return;

            BlockPos target = rayTrace.getBlockPos();
            Set<BlockPos> blocks = getAffectedBlocks(target, size);
            blocks.removeIf(pos -> Helper.getWorld().isAirBlock(pos) || heldItem.getStrVsBlock(Helper.getWorld().getBlockState(pos).getBlock()) <= 1.0F);

            renderBlockOutline(blocks);
        }
    }

    private Set<BlockPos> getAffectedBlocks(BlockPos center, int radius) {
        Set<BlockPos> blocks = new HashSet<>();

        Vec3 playerPos = Helper.getPlayer().getPositionEyes(1.0f);
        Vec3 blockCenter = new Vec3(center).addVector(0.5, 0.5, 0.5);
        Vec3 direction = blockCenter.subtract(playerPos).normalize();

        int range = radius / 2;

        if (Math.abs(direction.xCoord) > Math.abs(direction.yCoord) && Math.abs(direction.xCoord) > Math.abs(direction.zCoord)) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    blocks.add(center.add(0, y, z));
                }
            }
        } else if (Math.abs(direction.zCoord) > Math.abs(direction.yCoord)) {
            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    blocks.add(center.add(x, y, 0));
                }
            }
        } else {
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    blocks.add(center.add(x, 0, z));
                }
            }
        }
        return blocks;
    }

    private void renderBlockOutline(Set<BlockPos> blocks) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GL11.glLineWidth(2.0f);
        GlStateManager.disableDepth();
        GlStateManager.color(0.0f, 1.0f, 0.0f, 1.0f);

        GL11.glBegin(GL11.GL_LINES);
        for (BlockPos pos : blocks) {
            double x = pos.getX() - Minecraft.getMinecraft().getRenderManager().viewerPosX;
            double y = pos.getY() - Minecraft.getMinecraft().getRenderManager().viewerPosY;
            double z = pos.getZ() - Minecraft.getMinecraft().getRenderManager().viewerPosZ;
            drawBlockOutline(x, y, z);
        }
        GL11.glEnd();

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawBlockOutline(double x, double y, double z) {
        double s = 1.0;

        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x + s, y, z);

        GL11.glVertex3d(x + s, y, z);
        GL11.glVertex3d(x + s, y, z + s);

        GL11.glVertex3d(x + s, y, z + s);
        GL11.glVertex3d(x, y, z + s);

        GL11.glVertex3d(x, y, z + s);
        GL11.glVertex3d(x, y, z);

        GL11.glVertex3d(x, y + s, z);
        GL11.glVertex3d(x + s, y + s, z);

        GL11.glVertex3d(x + s, y + s, z);
        GL11.glVertex3d(x + s, y + s, z + s);

        GL11.glVertex3d(x + s, y + s, z + s);
        GL11.glVertex3d(x, y + s, z + s);

        GL11.glVertex3d(x, y + s, z + s);
        GL11.glVertex3d(x, y + s, z);

        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x, y + s, z);

        GL11.glVertex3d(x + s, y, z);
        GL11.glVertex3d(x + s, y + s, z);

        GL11.glVertex3d(x + s, y, z + s);
        GL11.glVertex3d(x + s, y + s, z + s);

        GL11.glVertex3d(x, y, z + s);
        GL11.glVertex3d(x, y + s, z + s);
    }
}