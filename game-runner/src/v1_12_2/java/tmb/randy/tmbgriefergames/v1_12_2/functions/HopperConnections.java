package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.functions.HopperConnectionsMaster;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

public class HopperConnections extends HopperConnectionsMaster<BlockPos, ItemStack> {

    public void onGuiOpenEvent() {
        if(Helper.getPlayer().openContainer instanceof ContainerChest chestContainer) {
            String invName = chestContainer.getLowerChestInventory().getName();
            BlockPos source = Helper.getBlockPosLookingAt();
            if(invName.equals(Const.Menu.TRICHTER_MEHRFACH_VERBINDUNGEN)) {
                for (int i = 0; i < 44; i++) {
                    ItemStack stack = chestContainer.getLowerChestInventory().getStackInSlot(i);
                    String firstLine = Helper.getLoreLine(stack, 0);
                    if(firstLine != null && firstLine.contains(Const.Lore.VERBUNDEN_MIT_LABEL))
                        addConnection(source, Helper.parseBlockPos(firstLine, Const.Lore.VERBUNDEN_MIT_PREFIX, "\"", ".0"), stack);
                }
            } else if(invName.equals(Const.Menu.TRICHTER_EINSTELLUNGEN)) {
                ItemStack stack = chestContainer.getLowerChestInventory().getStackInSlot(16);
                String firstLine = Helper.getLoreLine(stack, 0);
                if(firstLine != null && firstLine.contains(Const.Lore.WEITERLEITEN_AN))
                    addConnection(source, Helper.parseBlockPos(firstLine, Const.Lore.WEITERLEITEN_AN, "\"", ".0", "§7", "§e"), null);
            }
        }
    }

    @Override
    protected BlockPos getLookingAtPos() {
        return Helper.getBlockPosLookingAt();
    }

    @Override
    protected boolean isHopper(BlockPos pos) {
        return Helper.getWorld().getBlockState(pos).getBlock() == Blocks.HOPPER;
    }

    @Override
    protected ItemStack getHeldStack() {
        return Helper.getHeldItem();
    }

    @Override
    protected String getStackKey(ItemStack stack) {
        return stack.getItem().toString() + ":" + stack.getMetadata();
    }

    @Override
    public void renderWorldEvent(RenderWorldEvent event) {

        for (Connection<BlockPos, ItemStack> conntection : getConnections()) {

            if(conntection.pos1() == null)
                continue;

            double distance = conntection.pos1().distanceSq(Helper.getPlayer().posX,
                Helper.getPlayer().posY, Helper.getPlayer().posZ);

            if (distance > 900)
                continue;

            if (conntection.cb() == CBtracker.getCurrentCB()) {
                if (Helper.getWorld().getBlockState(conntection.pos1()).getBlock()
                    == Blocks.HOPPER) {
                    drawLineBetween(conntection);
                }
            }
        }

        if(getCurrentConnectingHopper() != null)
            drawSphere(getCurrentConnectingHopper());
    }

    private void drawLineBetween(Connection<BlockPos, ItemStack> connection) {
        if(!Addon.settings().getHopperSubConfig().getShowLines().get())
            return;

        double x1 = connection.pos1().getX() - Minecraft.getMinecraft().getRenderManager().viewerPosX + 0.5;
        double y1 = connection.pos1().getY() - Minecraft.getMinecraft().getRenderManager().viewerPosY + 0.5;
        double z1 = connection.pos1().getZ() - Minecraft.getMinecraft().getRenderManager().viewerPosZ + 0.5;

        double x2 = connection.pos2().getX() - Minecraft.getMinecraft().getRenderManager().viewerPosX + 0.5;
        double y2 = connection.pos2().getY() - Minecraft.getMinecraft().getRenderManager().viewerPosY + 0.5;
        double z2 = connection.pos2().getZ() - Minecraft.getMinecraft().getRenderManager().viewerPosZ + 0.5;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();

        GL11.glLineWidth(64.0F);
        GL11.glBegin(GL11.GL_LINES);

        if (connection.stack() == null) {
            GL11.glColor3f(0.333f, 1.0f, 1.0f);
            GL11.glVertex3d(x1, y1, z1);

            GL11.glColor3f(0.0f, 0.0f, 0.667f);
            GL11.glVertex3d(x2, y2, z2);
        } else {
            GL11.glColor3f(1.0f, 0.667f, 0.0f);
            GL11.glVertex3d(x1, y1, z1);

            GL11.glColor3f(0.667f, 0.0f, 0.0f);
            GL11.glVertex3d(x2, y2, z2);
        }

        GL11.glEnd();

        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();

        if (connection.stack() != null) {
            long time = Helper.getWorld().getTotalWorldTime();
            double animationDuration = 100.0;

            double progress = (time % (int) animationDuration) / animationDuration;

            double animX = x1 + (x2 - x1) * progress;
            double animY = y1 + (y2 - y1) * progress;
            double animZ = z1 + (z2 - z1) * progress;

            renderItemStackAt(connection.stack(), animX, animY, animZ);
        }
    }

    private void renderItemStackAt(ItemStack stack, double x, double y, double z) {
        if(!Addon.settings().getHopperSubConfig().getShowLines().get())
            return;

        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y, z);

        GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        GlStateManager.disableLighting();

        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        Minecraft.getMinecraft().getRenderItem().renderItem(stack, TransformType.FIXED);

        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    private void drawSphere(BlockPos center) {
        if(!Addon.settings().getHopperSubConfig().getShowRadius().get())
            return;

        double cx = center.getX() - Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double cy = center.getY() - Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double cz = center.getZ() - Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        GL11.glLineWidth(1.5F);
        GlStateManager.color(1.0f, 0.0f, 0.0f, 1.0f);

        GL11.glTranslated(cx, cy, cz);

        GL11.glBegin(GL11.GL_LINES);

        int segments = 60;
        double step = Math.PI * 2 / segments;

        for (double theta = 0; theta < Math.PI * 2; theta += step) {
            for (double phi = 0; phi < Math.PI; phi += step) {
                double x1 = 30.0 * Math.sin(phi) * Math.cos(theta);
                double z1 = 30.0 * Math.sin(phi) * Math.sin(theta);
                double y1 = 30.0 * Math.cos(phi);

                double x2 = 30.0 * Math.sin(phi + step) * Math.cos(theta);
                double z2 = 30.0 * Math.sin(phi + step) * Math.sin(theta);
                double y2 = 30.0 * Math.cos(phi + step);

                GL11.glVertex3d(x1, y1, z1);
                GL11.glVertex3d(x2, y2, z2);
            }
        }

        for (double theta = 0; theta < Math.PI * 2; theta += step) {
            for (double phi = 0; phi < Math.PI; phi += step) {
                double y1 = 30.0 * Math.sin(phi) * Math.cos(theta);
                double z1 = 30.0 * Math.sin(phi) * Math.sin(theta);
                double x1 = 30.0 * Math.cos(phi);

                double y2 = 30.0 * Math.sin(phi + step) * Math.cos(theta);
                double z2 = 30.0 * Math.sin(phi + step) * Math.sin(theta);
                double x2 = 30.0 * Math.cos(phi + step);

                GL11.glVertex3d(x1, y1, z1);
                GL11.glVertex3d(x2, y2, z2);
            }
        }

        GL11.glEnd();

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

}
