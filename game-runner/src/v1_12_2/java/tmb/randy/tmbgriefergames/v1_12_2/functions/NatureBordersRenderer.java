package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.Color;
import net.minecraft.block.material.Material;
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
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.functions.NatureBordersRendererMaster;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

public class NatureBordersRenderer extends NatureBordersRendererMaster {

    public BlockPos getTopLiquidOrSolidBlock2(WorldClient world, BlockPos pos) {
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

    public boolean isPosInRenderableArea(EntityPlayerSP player, int posX, int posZ) {
        return getDistanceSq(player.posX, player.posZ, posX, posZ) < 36864.0D;
    }

    @Override
    public void renderWorldEvent(RenderWorldEvent event) {
        if (isEnabled() && CBtracker.isNatureWorldCB()) {
            double x = Helper.getPlayer().prevPosX + (Helper.getPlayer().posX - Helper.getPlayer().prevPosX) * event.getPartialTicks();
            double y = Helper.getPlayer().prevPosY + (Helper.getPlayer().posY - Helper.getPlayer().prevPosY) * event.getPartialTicks();
            double z = Helper.getPlayer().prevPosZ + (Helper.getPlayer().posZ - Helper.getPlayer().prevPosZ) * event.getPartialTicks();
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
            if(Addon.settings().getNatureSubConfig().getRainbow().get()) {
                ColorVector hsv = this.RGB2HSV(this.lineRed, this.lineGreen, this.lineBlue);
                ColorVector rgb = this.HSV2RGB((hsv.x + 0.01F) % 1.0F, hsv.y, hsv.z);
                this.lineRed = rgb.x;
                this.lineGreen = rgb.y;
                this.lineBlue = rgb.z;
                GL11.glColor4f(rgb.x, rgb.y, rgb.z, 0.35F);
            } else {
                Color color = Color.of(Addon.settings().getNatureSubConfig().getBorderColor().get());
                GL11.glColor4f(((float) color.getRed())/255, ((float) color.getGreen())/255, ((float) color.getBlue())/255, 0.35F);
            }
            render.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            int rad = 16 * Addon.settings().getNatureSubConfig().getBorderRadius().get();
            int hrf = rad / 2;

            for (int xx = 0; xx < rad; ++xx)
            {
                for (int zz = 0; zz < rad; ++zz)
                {
                    if (this.isPosInRenderableArea(Helper.getPlayer(), (int)Helper.getPlayer().posX - hrf + xx, (int)Helper.getPlayer().posZ - hrf + zz))
                    {
                        int posX = (int)Helper.getPlayer().posX - hrf + xx;
                        int posZ = (int)Helper.getPlayer().posZ - hrf + zz;
                        BlockPos pos = new BlockPos(posX, Helper.getPlayer().posY, posZ);

                        BlockPos posN = new BlockPos(posX, Helper.getPlayer().posY, posZ - 1);
                        BlockPos posE = new BlockPos(posX + 1, Helper.getPlayer().posY, posZ);
                        double yy = 1.0D - y;
                        double yy2 = 256.0D - y;

                        if (!Addon.settings().getNatureSubConfig().getBorderMaxHeight().get())
                        {
                            pos = this.getTopLiquidOrSolidBlock2(Helper.getWorld(), pos);
                            posN = this.getTopLiquidOrSolidBlock2(Helper.getWorld(), posN);
                            posE = this.getTopLiquidOrSolidBlock2(Helper.getWorld(), posE);
                        }

                        int additionalOffsetZ = (pos.getZ() < 0 ? -1 : 0);

                        if ((pos.getZ() + additionalOffsetZ) % 42 == 0)
                        {
                            if (!Addon.settings().getNatureSubConfig().getBorderMaxHeight().get())
                            {
                                yy = Math.max(pos.getY(), posN.getY()) - y;
                                yy2 = yy + Addon.settings().getNatureSubConfig().getBorderheight().get();
                            }

                            render.pos(pos.getX() - x, yy, pos.getZ() - z).endVertex();
                            render.pos(pos.getX() + 1.0D - x, yy, pos.getZ() - z).endVertex();
                            render.pos(pos.getX() + 1.0D - x, yy2, pos.getZ() - z).endVertex();
                            render.pos(pos.getX() - x, yy2, pos.getZ() - z).endVertex();
                        }

                        int additionalOffsetX = (pos.getX() < 0 ? -1 : 0);

                        if ((pos.getX() + 1 + additionalOffsetX) % 42 == 0)
                        {
                            if (!Addon.settings().getNatureSubConfig().getBorderMaxHeight().get())
                            {
                                yy = Math.max(pos.getY(), posE.getY()) - y;
                                yy2 = yy + Addon.settings().getNatureSubConfig().getBorderheight().get();
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

}

