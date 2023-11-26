package tmb.randy.tmbgriefergames.v1_12_2.util;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.I18n;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import tmb.randy.tmbgriefergames.core.Addon;

public class NatureBordersRenderer {

    private final int offsetX = 1;
    private final int offsetZ = 0;

    public static double getDistanceSq(double x1, double z1, double x2, double z2)
    {
        double xs = x1 - x2;
        double zs = z1 - z2;
        return xs * xs + zs * zs;
    }

    private float lineRed = 1.0F;
    private float lineGreen = 0.0F;
    private float lineBlue = 0.0F;

    public void onKey(KeyEvent event) {
        if(Addon.areKeysPressed(Addon.getSharedInstance().configuration().getNatureSubConfig().getHotkey().get())) {
            Addon.getSharedInstance().configuration().getNatureSubConfig().getShowBorders().set(!Addon.getSharedInstance().configuration().getNatureSubConfig().getShowBorders().get());

            String activeString = Addon.getSharedInstance().configuration().getNatureSubConfig().getShowBorders().get() ? I18n.getTranslation("tmbgriefergames.natureBorders.plotBordersVisible") : I18n.getTranslation("tmbgriefergames.natureBorders.plotBordersInvisible");
            Addon.getSharedInstance().displayNotification(activeString);
        }
    }

    public BlockPos getTopLiquidOrSolidBlock2(WorldClient world, BlockPos pos)
    {
        Chunk chunk = world.getChunk(pos);
        boolean inBlock = true;
        BlockPos var3;
        BlockPos var4;

        for (var3 = new BlockPos(pos.getX(), pos.getY() + 3, pos.getZ()); var3.getY() >= 0; var3 = var4)
        {
            var4 = var3.offset(EnumFacing.DOWN);
            Material material;
            boolean bool;

            if (inBlock)
            {
                material = chunk.getBlockState(var3).getMaterial();
                bool = material == Material.WATER || material == Material.LAVA || material.blocksMovement() && material != Material.LEAVES;

                if (bool)
                {
                    continue;
                }
            }

            inBlock = false;
            material = chunk.getBlockState(var4).getMaterial();
            bool = material == Material.WATER || material == Material.LAVA || material.blocksMovement() && material != Material.LEAVES;

            if (bool)
            {
                break;
            }
        }

        return var3;
    }

    Vector3f HSV2RGB(float h, float s, float v)
    {
        float r = v;
        float g = v;
        float b = v;

        if (s > 0.0F)
        {
            h *= 6.0F;
            int i = (int)h;
            float f = h - i;

            switch (i)
            {
                case 0:
                default:
                    g = v * (1.0F - s * (1.0F - f));
                    b = v * (1.0F - s);
                    break;

                case 1:
                    r = v * (1.0F - s * f);
                    b = v * (1.0F - s);
                    break;

                case 2:
                    r = v * (1.0F - s);
                    b = v * (1.0F - s * (1.0F - f));
                    break;

                case 3:
                    r = v * (1.0F - s);
                    g = v * (1.0F - s * f);
                    break;

                case 4:
                    r = v * (1.0F - s * (1.0F - f));
                    g = v * (1.0F - s);
                    break;

                case 5:
                    g = v * (1.0F - s);
                    b = v * (1.0F - s * f);
            }
        }

        return new Vector3f(r, g, b);
    }

    public boolean isPosInRenderableArea(EntityPlayerSP player, int posX, int posZ)
    {
        return getDistanceSq(player.posX, player.posZ, posX, posZ) < 36864.0D;
    }

    public void onRender(RenderWorldEvent event)
    {
        if (Addon.getSharedInstance().configuration().getNatureSubConfig().getShowBorders().get() && (CBTracker.currentCB.equals("Nature") || CBTracker.currentCB.equals("Extreme")))
        {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            WorldClient world = Minecraft.getMinecraft().world;
            double x = player.prevPosX + (player.posX - player.prevPosX) * event.getPartialTicks();
            double y = player.prevPosY + (player.posY - player.prevPosY) * event.getPartialTicks();
            double z = player.prevPosZ + (player.posZ - player.prevPosZ) * event.getPartialTicks();
            RenderHelper.disableStandardItemLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glLineWidth(1.0F);
            GL11.glDepthMask(false);
            GL11.glPushMatrix();
            Tessellator tesselator = Tessellator.getInstance();
            BufferBuilder render = tesselator.getBuffer();
            Vector3f hsv = this.RGB2HSV(this.lineRed, this.lineGreen, this.lineBlue);
            Vector3f rgb = this.HSV2RGB((hsv.x + 0.01F) % 1.0F, hsv.y, hsv.z);
            this.lineRed = rgb.x;
            this.lineGreen = rgb.y;
            this.lineBlue = rgb.z;
            GL11.glColor4f(rgb.x, rgb.y, rgb.z, 0.35F);
            render.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            int rad = 16 * Addon.getSharedInstance().configuration().getNatureSubConfig().getBorderRadius().get();
            int hrf = rad / 2;

            for (int xx = 0; xx < rad; ++xx)
            {
                for (int zz = 0; zz < rad; ++zz)
                {
                    if (this.isPosInRenderableArea(player, (int)player.posX - hrf + xx, (int)player.posZ - hrf + zz))
                    {
                        int posX = (int)player.posX - hrf + xx;
                        int posZ = (int)player.posZ - hrf + zz;
                        BlockPos pos = new BlockPos(posX, player.posY, posZ);

                        BlockPos posN = new BlockPos(posX, player.posY, posZ - 1);
                        BlockPos posE = new BlockPos(posX + 1, player.posY, posZ);
                        double yy = 1.0D - y;
                        double yy2 = 256.0D - y;

                        if (!Addon.getSharedInstance().configuration().getNatureSubConfig().getBorderMaxHeight().get())
                        {
                            pos = this.getTopLiquidOrSolidBlock2(world, pos);
                            posN = this.getTopLiquidOrSolidBlock2(world, posN);
                            posE = this.getTopLiquidOrSolidBlock2(world, posE);
                        }

                        int additionalOffsetZ = (pos.getZ() < 0 ? -1 : 0);

                        if ((pos.getZ() + offsetZ + additionalOffsetZ) % 42 == 0)
                        {
                            if (!Addon.getSharedInstance().configuration().getNatureSubConfig().getBorderMaxHeight().get())
                            {
                                yy = Math.max(pos.getY(), posN.getY()) - y;
                                yy2 = yy + Addon.getSharedInstance().configuration().getNatureSubConfig().getBorderheight().get();
                            }

                            render.pos(pos.getX() - x, yy, pos.getZ() - z).endVertex();
                            render.pos(pos.getX() + 1.0D - x, yy, pos.getZ() - z).endVertex();
                            render.pos(pos.getX() + 1.0D - x, yy2, pos.getZ() - z).endVertex();
                            render.pos(pos.getX() - x, yy2, pos.getZ() - z).endVertex();
                        }

                        int additionalOffsetX = (pos.getX() < 0 ? -1 : 0);

                        if ((pos.getX() + offsetX + additionalOffsetX) % 42 == 0)
                        {
                            if (!Addon.getSharedInstance().configuration().getNatureSubConfig().getBorderMaxHeight().get())
                            {
                                yy = Math.max(pos.getY(), posE.getY()) - y;
                                yy2 = yy + Addon.getSharedInstance().configuration().getNatureSubConfig().getBorderheight().get();
                            }

                            render.pos(pos.getX() + 1.0D - x, yy, pos.getZ() - z).endVertex();
                            render.pos(pos.getX() + 1.0D - x, yy, pos.getZ() + 1.0D - z).endVertex();
                            render.pos(pos.getX() + 1.0D - x, yy2, pos.getZ() + 1.0D - z).endVertex();
                            render.pos(pos.getX() + 1.0D - x, yy2, pos.getZ() - z).endVertex();
                        }
                    }
                }
            }

            tesselator.draw();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            RenderHelper.enableStandardItemLighting();
        }
    }

    Vector3f RGB2HSV(float r, float g, float b)
    {
        float max = Math.max(r, g);
        max = Math.max(max, b);
        float min = Math.min(r, g);
        min = Math.min(min, b);
        float h = max - min;

        if (h > 0.0F)
        {
            if (max == r)
            {
                h = (g - b) / h;

                if (h < 0.0F)
                {
                    h += 6.0F;
                }
            }
            else if (max == g)
            {
                h = 2.0F + (b - r) / h;
            }
            else
            {
                h = 4.0F + (r - g) / h;
            }
        }

        h /= 6.0F;
        float s = max - min;

        if (max != 0.0F)
        {
            s /= max;
        }

        return new Vector3f(h, s, max);
    }

}

