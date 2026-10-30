package tmb.randy.tmbgriefergames.v1_8_9.functions;

import java.util.HashSet;
import java.util.Set;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing.Axis;
import org.lwjgl.opengl.GL11;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

/** Outlines the blocks an orb pickaxe or orb shovel will break. */
public class BlockMarker extends Function {

    public BlockMarker() {
        super(Functions.BLOCKMARKER.name());
    }

    @Override
    public void renderWorldEvent(RenderWorldEvent event) {
        if (!Addon.settings().getBlockMarker().get())
            return;

        ItemStack heldItem = Helper.getHeldItem();
        int size = getOrbToolSize(heldItem);
        BlockPos target = Helper.getBlockPosLookingAt();
        if (size == 0 || target == null)
            return;

        Set<BlockPos> blocks = getAffectedBlocks(target, size / 2);
        blocks.removeIf(pos -> Helper.getWorld().isAirBlock(pos) || Helper.getDestroySpeed(heldItem, pos) <= 1.0F);
        renderBlockOutlines(blocks);
    }

    /** Edge length of the area an orb tool breaks, 0 if the stack is no orb tool. */
    private static int getOrbToolSize(ItemStack stack) {
        if (Helper.isEmpty(stack) || !stack.hasTagCompound())
            return 0;

        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey("orb_pickaxe") && !tag.hasKey("orb_shovel"))
            return 0;

        return switch (tag.getString("orb_tool_level")) {
            case "LARGE" -> 7;
            case "MEDIUM" -> 5;
            default -> 3;
        };
    }

    /** All blocks in the plane perpendicular to the view axis around the center. */
    private static Set<BlockPos> getAffectedBlocks(BlockPos center, int range) {
        Axis axis = Helper.getViewAxis(center);
        Set<BlockPos> blocks = new HashSet<>();

        for (int a = -range; a <= range; a++) {
            for (int b = -range; b <= range; b++) {
                blocks.add(switch (axis) {
                    case X -> center.add(0, a, b);
                    case Z -> center.add(a, b, 0);
                    case Y -> center.add(a, 0, b);
                });
            }
        }
        return blocks;
    }

    private static void renderBlockOutlines(Set<BlockPos> blocks) {
        RenderManager view = Helper.mc().getRenderManager();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GL11.glLineWidth(2.0f);
        GlStateManager.disableDepth();
        GlStateManager.color(0.0f, 1.0f, 0.0f, 1.0f);

        GL11.glBegin(GL11.GL_LINES);
        for (BlockPos pos : blocks)
            drawBlockOutline(pos.getX() - view.viewerPosX, pos.getY() - view.viewerPosY, pos.getZ() - view.viewerPosZ);
        GL11.glEnd();

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /** The 12 edges of a block cube. */
    private static void drawBlockOutline(double x, double y, double z) {
        for (int i = 0; i < 4; i++) {
            double x1 = x + (i == 1 || i == 2 ? 1 : 0), z1 = z + (i >= 2 ? 1 : 0);
            double x2 = x + (i == 0 || i == 1 ? 1 : 0), z2 = z + (i == 1 || i == 2 ? 1 : 0);

            line(x1, y, z1, x2, y, z2);
            line(x1, y + 1, z1, x2, y + 1, z2);
            line(x1, y, z1, x1, y + 1, z1);
        }
    }

    private static void line(double x1, double y1, double z1, double x2, double y2, double z2) {
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y2, z2);
    }
}
