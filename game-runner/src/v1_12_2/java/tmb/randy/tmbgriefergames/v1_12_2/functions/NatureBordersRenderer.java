package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.Color;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import tmb.randy.tmbgriefergames.api.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.config.NatureSubConfig;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

/** Renders the borders of the 42x42 plots in the nature worlds. */
public class NatureBordersRenderer extends ActiveFunction {
    private static final int PLOT_SIZE = 42;
    private static final double MAX_RENDER_DISTANCE_SQ = 192 * 192;
    private static final float ALPHA = 0.35F;
    private static final float RAINBOW_SPEED = 0.01F;

    private float rainbowHue = 0.0F;

    public NatureBordersRenderer() {
        super(Functions.NATUREBORDERSRENDERER.name());
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (Addon.allKeysPressedAndGuiClosed(config().getHotkey().get()) && CBtracker.isNatureWorldCB())
            toggle();
    }

    @Override
    public void cbChangedEvent(CbChangedEvent event) {
        // Stays enabled while switching CBs
    }

    @Override
    public void renderWorldEvent(RenderWorldEvent event) {
        if (!isEnabled() || !CBtracker.isNatureWorldCB())
            return;

        float partialTicks = event.getPartialTicks();
        double viewX = Helper.getPlayer().prevPosX + (Helper.getPlayer().posX - Helper.getPlayer().prevPosX) * partialTicks;
        double viewY = Helper.getPlayer().prevPosY + (Helper.getPlayer().posY - Helper.getPlayer().prevPosY) * partialTicks;
        double viewZ = Helper.getPlayer().prevPosZ + (Helper.getPlayer().posZ - Helper.getPlayer().prevPosZ) * partialTicks;

        setupRendering();
        Helper.startQuads();

        int diameter = 16 * config().getBorderRadius().get();
        int playerX = (int) Helper.getPlayer().posX;
        int playerZ = (int) Helper.getPlayer().posZ;

        for (int dx = 0; dx < diameter; dx++) {
            for (int dz = 0; dz < diameter; dz++) {
                int posX = playerX - diameter / 2 + dx;
                int posZ = playerZ - diameter / 2 + dz;
                if (isInRenderDistance(posX, posZ))
                    addBorderQuads(posX, posZ, viewX, viewY, viewZ);
            }
        }

        Helper.draw();
        resetRendering();
    }

    private void addBorderQuads(int posX, int posZ, double viewX, double viewY, double viewZ) {
        boolean maxHeight = config().getBorderMaxHeight().get();
        double playerY = Helper.getPlayer().posY;

        BlockPos pos = new BlockPos(posX, playerY, posZ);
        BlockPos north = new BlockPos(posX, playerY, posZ - 1);
        BlockPos east = new BlockPos(posX + 1, playerY, posZ);

        if (!maxHeight) {
            pos = Helper.getTopSolidOrLiquidBlock(pos);
            north = Helper.getTopSolidOrLiquidBlock(north);
            east = Helper.getTopSolidOrLiquidBlock(east);
        }

        double x = pos.getX() - viewX;
        double z = pos.getZ() - viewZ;

        // Border along the x axis (north side of the block)
        if ((pos.getZ() + (pos.getZ() < 0 ? -1 : 0)) % PLOT_SIZE == 0) {
            double[] height = getHeightRange(Math.max(pos.getY(), north.getY()), viewY, maxHeight);
            Helper.vertex(x, height[0], z);
            Helper.vertex(x + 1, height[0], z);
            Helper.vertex(x + 1, height[1], z);
            Helper.vertex(x, height[1], z);
        }

        // Border along the z axis (east side of the block)
        if ((pos.getX() + 1 + (pos.getX() < 0 ? -1 : 0)) % PLOT_SIZE == 0) {
            double[] height = getHeightRange(Math.max(pos.getY(), east.getY()), viewY, maxHeight);
            Helper.vertex(x + 1, height[0], z);
            Helper.vertex(x + 1, height[0], z + 1);
            Helper.vertex(x + 1, height[1], z + 1);
            Helper.vertex(x + 1, height[1], z);
        }
    }

    /** Bottom and top of a border wall relative to the view, either the whole world height or the configured height. */
    private double[] getHeightRange(int groundY, double viewY, boolean maxHeight) {
        if (maxHeight)
            return new double[] {1.0D - viewY, 256.0D - viewY};

        double bottom = groundY - viewY;
        return new double[] {bottom, bottom + config().getBorderheight().get()};
    }

    private boolean isInRenderDistance(int posX, int posZ) {
        double dx = Helper.getPlayer().posX - posX;
        double dz = Helper.getPlayer().posZ - posZ;
        return dx * dx + dz * dz < MAX_RENDER_DISTANCE_SQ;
    }

    private void setupRendering() {
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

        if (config().getRainbow().get()) {
            rainbowHue = (rainbowHue + RAINBOW_SPEED) % 1.0F;
            setColor(java.awt.Color.HSBtoRGB(rainbowHue, 1.0F, 1.0F));
        } else {
            setColor(config().getBorderColor().get());
        }
    }

    private static void setColor(int rgb) {
        Color color = Color.of(rgb);
        GL11.glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, ALPHA);
    }

    private void resetRendering() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        RenderHelper.enableStandardItemLighting();
    }

    private static NatureSubConfig config() {
        return Addon.settings().getNatureSubConfig();
    }
}
