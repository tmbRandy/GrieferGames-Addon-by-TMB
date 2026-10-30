package tmb.randy.tmbgriefergames.v1_8_9.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.api.enums.HopperState;
import tmb.randy.tmbgriefergames.api.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.api.events.HopperStateChangedEvent;
import tmb.randy.tmbgriefergames.api.events.ResetLinesEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.config.HopperSubConfig;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.menu.HopperMenu;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

/** Remembers hopper connections and renders them as lines, plus the 30 block radius while connecting. */
public class HopperConnections extends Function {
    private static final double MAX_RENDER_DISTANCE = 30;
    private static final double CONNECT_RADIUS = 30;
    private static final int SPHERE_SEGMENTS = 60;
    private static final double ITEM_ANIMATION_TICKS = 100;

    private final Map<String, HopperConnection> connections = new HashMap<>();
    private BlockPos currentConnectingHopper;

    /** A connection from {@code from} to {@code to}, multi connections also store their filter item. */
    private record HopperConnection(BlockPos from, BlockPos to, @Nullable ItemStack stack, CBs cb) {
        String key() {
            return stack == null ? cb.getName() + "." + from : cb.getName() + "." + stack.getItem() + ":" + stack.getMetadata() + "." + from;
        }
    }

    public HopperConnections() {
        super(Functions.HOPPERCONNECTIONS.name());
    }

    @Override
    public void hopperStateChangedEvent(HopperStateChangedEvent event) {
        if (event.newState() == HopperState.NONE)
            currentConnectingHopper = null;
    }

    @Override
    public void cbChangedEvent(CbChangedEvent event) {
        currentConnectingHopper = null;
    }

    @Override
    public void resetLinesEvent(ResetLinesEvent event) {
        connections.clear();
    }

    /** Reads the existing connections out of the hopper menus. */
    public void onGuiOpenEvent() {
        String title = Helper.getChestTitle();

        if (HopperMenu.MultiConnections.TITLE.equals(title)) {
            for (int i = 0; i < HopperMenu.MultiConnections.CONNECTION_SLOTS; i++) {
                ItemStack stack = Helper.getContainerStack(i);
                String firstLine = getFirstLoreLine(stack);
                if (firstLine != null && firstLine.contains(Const.Lore.VERBUNDEN_MIT_LABEL))
                    addConnection(firstLine.replace(Const.Lore.VERBUNDEN_MIT_PREFIX, ""), stack);
            }
        } else if (HopperMenu.TITLE.equals(title)) {
            String firstLine = getFirstLoreLine(Helper.getContainerStack(HopperMenu.CONNECT));
            if (firstLine != null && firstLine.contains(Const.Lore.WEITERLEITEN_AN))
                addConnection(firstLine.replace(Const.Lore.WEITERLEITEN_AN, "").replace("§7", "").replace("§e", ""), null);
        }
    }

    private void addConnection(String coordinates, ItemStack stack) {
        int[] target = Helper.parseCoordinates(coordinates);
        BlockPos hopper = Helper.getBlockPosLookingAt();
        if (target != null && hopper != null)
            put(new HopperConnection(hopper, new BlockPos(target[0], target[1], target[2]), stack, CBtracker.getCurrentCB()));
    }

    private static String getFirstLoreLine(ItemStack stack) {
        List<String> lore = Helper.getLore(stack);
        return lore.isEmpty() ? null : lore.get(0);
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        String message = event.chatMessage().getPlainText();
        BlockPos lookingAt = Helper.getBlockPosLookingAt();

        switch (message) {
            case Const.Chat.TRICHTER_CONNECT_START, Const.Chat.TRICHTER_MULTI_CONNECT_START -> {
                if (Helper.isBlock(lookingAt, Const.Id.HOPPER))
                    currentConnectingHopper = lookingAt;
            }
            case Const.Chat.TRICHTER_CONNECTED -> {
                if (lookingAt != null)
                    put(new HopperConnection(currentConnectingHopper, lookingAt, null, CBtracker.getCurrentCB()));
                currentConnectingHopper = null;
            }
            case Const.Chat.TRICHTER_CONNECT_ENDED, Const.Chat.TRICHTER_START_TOO_FAR -> currentConnectingHopper = null;
            case Const.Chat.TRICHTER_MULTI_ADDED -> {
                if (lookingAt != null)
                    put(new HopperConnection(currentConnectingHopper, lookingAt, Helper.getHeldItem(), CBtracker.getCurrentCB()));
            }
            case Const.Chat.TRICHTER_MULTI_REMOVED -> {
                if (lookingAt != null)
                    connections.values().removeIf(connection -> connection.stack() != null && connection.from().equals(lookingAt));
            }
            case Const.Chat.TRICHTER_REMOVED -> {
                if (lookingAt != null)
                    connections.values().stream()
                        .filter(connection -> connection.stack() == null && connection.from().equals(lookingAt))
                        .findFirst()
                        .ifPresent(connection -> connections.remove(connection.key()));
            }
        }
    }

    private void put(HopperConnection connection) {
        connections.put(connection.key(), connection);
    }

    @Override
    public void renderWorldEvent(RenderWorldEvent event) {
        for (HopperConnection connection : connections.values()) {
            if (connection.from() == null)
                return;

            double distance = Math.sqrt(connection.from().distanceSq(Helper.getPlayer().posX, Helper.getPlayer().posY, Helper.getPlayer().posZ));
            if (distance <= MAX_RENDER_DISTANCE && connection.cb() == CBtracker.getCurrentCB() && Helper.isBlock(connection.from(), Const.Id.HOPPER))
                drawConnection(connection);
        }

        if (currentConnectingHopper != null && config().getShowRadius().get())
            drawSphere(currentConnectingHopper);
    }

    private void drawConnection(HopperConnection connection) {
        if (!config().getShowLines().get())
            return;

        RenderManager view = Helper.mc().getRenderManager();
        double x1 = connection.from().getX() - view.viewerPosX + 0.5;
        double y1 = connection.from().getY() - view.viewerPosY + 0.5;
        double z1 = connection.from().getZ() - view.viewerPosZ + 0.5;
        double x2 = connection.to().getX() - view.viewerPosX + 0.5;
        double y2 = connection.to().getY() - view.viewerPosY + 0.5;
        double z2 = connection.to().getZ() - view.viewerPosZ + 0.5;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();

        GL11.glLineWidth(64.0F);
        GL11.glBegin(GL11.GL_LINES);
        // Single connections fade from light to dark blue, multi connections from orange to dark red
        if (connection.stack() == null) {
            GL11.glColor3f(0.333f, 1.0f, 1.0f);
            GL11.glVertex3d(x1, y1, z1);
            GL11.glColor3f(0.0f, 0.0f, 0.667f);
        } else {
            GL11.glColor3f(1.0f, 0.667f, 0.0f);
            GL11.glVertex3d(x1, y1, z1);
            GL11.glColor3f(0.667f, 0.0f, 0.0f);
        }
        GL11.glVertex3d(x2, y2, z2);
        GL11.glEnd();

        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();

        if (connection.stack() != null) {
            double progress = (Helper.getWorld().getTotalWorldTime() % (int) ITEM_ANIMATION_TICKS) / ITEM_ANIMATION_TICKS;
            renderItemStackAt(connection.stack(), x1 + (x2 - x1) * progress, y1 + (y2 - y1) * progress, z1 + (z2 - z1) * progress);
        }
    }

    private void renderItemStackAt(ItemStack stack, double x, double y, double z) {
        RenderManager view = Helper.mc().getRenderManager();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-view.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(view.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.disableLighting();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        Helper.mc().getRenderItem().renderItem(stack, TransformType.FIXED);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void drawSphere(BlockPos center) {
        RenderManager view = Helper.mc().getRenderManager();

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GL11.glLineWidth(1.5F);
        GlStateManager.color(1.0f, 0.0f, 0.0f, 1.0f);
        GL11.glTranslated(center.getX() - view.viewerPosX, center.getY() - view.viewerPosY, center.getZ() - view.viewerPosZ);

        GL11.glBegin(GL11.GL_LINES);
        double step = Math.PI * 2 / SPHERE_SEGMENTS;
        for (double theta = 0; theta < Math.PI * 2; theta += step) {
            for (double phi = 0; phi < Math.PI; phi += step) {
                // Two sets of meridians, rotated by 90 degrees against each other
                drawSegment(theta, phi, step, false);
                drawSegment(theta, phi, step, true);
            }
        }
        GL11.glEnd();

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static void drawSegment(double theta, double phi, double step, boolean rotated) {
        for (double angle : new double[] {phi, phi + step}) {
            double a = CONNECT_RADIUS * Math.sin(angle) * Math.cos(theta);
            double b = CONNECT_RADIUS * Math.cos(angle);
            double z = CONNECT_RADIUS * Math.sin(angle) * Math.sin(theta);
            if (rotated)
                GL11.glVertex3d(b, a, z);
            else
                GL11.glVertex3d(a, b, z);
        }
    }

    private static HopperSubConfig config() {
        return Addon.settings().getHopperSubConfig();
    }
}
