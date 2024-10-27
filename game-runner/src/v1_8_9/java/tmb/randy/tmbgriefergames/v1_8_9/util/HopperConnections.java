package tmb.randy.tmbgriefergames.v1_8_9.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.nbt.NBTTagType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.CBtracker;
import tmb.randy.tmbgriefergames.core.enums.CBs;
import tmb.randy.tmbgriefergames.core.enums.HopperState;
import tmb.randy.tmbgriefergames.core.events.HopperStateChangedEvent;

public class HopperConnections {

    private Map<String, HopperConnection> conntections = new HashMap<>();
    private BlockPos currentConnectingHopper;

    private record HopperConnection(BlockPos pos1, BlockPos pos2, @Nullable ItemStack stack, CBs cb) {
        public String toString() {
            if(stack != null) {
                return cb.getName() + "." + stack.getItem().toString() + ":" + stack.getMetadata() + "." + pos1;
            }

            return cb.getName() + "." + pos1;
        }
    }

    public void hopperStateChanged(HopperStateChangedEvent event) {
        if(event.getNewState() == HopperState.NONE) {
            currentConnectingHopper = null;
        }
    }

    public void onGuiOpenEvent() {

        if(Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerChest chestContainer) {
            String invName = chestContainer.getLowerChestInventory().getName();
            if(invName.equals("§6Trichter-Mehrfach-Verbindungen")) {
                for (int i = 0; i < 44; i++) {
                    ItemStack stack = chestContainer.getLowerChestInventory().getStackInSlot(i);
                    if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("display")) {
                        String firstLine = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", NBTTagType.STRING.getId()).get(0).toString();
                        if(firstLine.contains("Verbunden mit:")) {
                            String coordinateString = firstLine.replace("§7Verbunden mit: §e", "").replace("\"", "").replace(".0", "");
                            String[] coodStrings = coordinateString.split(";");
                            if(coodStrings.length == 3) {
                                int x = Integer.parseInt(coodStrings[0]);
                                int y = Integer.parseInt(coodStrings[1]);
                                int z = Integer.parseInt(coodStrings[2]);

                                MovingObjectPosition mop = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1.0F);
                                if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
                                    HopperConnection newConn = new HopperConnection(mop.getBlockPos(), new BlockPos(x, y, z), stack, CBtracker.getCurrentCB());
                                    conntections.put(newConn.toString(), newConn);
                                }

                            }
                        }
                    }
                }
            } else if(invName.equals("§6Trichter-Einstellungen")) {
                ItemStack stack = chestContainer.getLowerChestInventory().getStackInSlot(16);
                if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("display")) {
                    String firstLine = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", NBTTagType.STRING.getId()).get(0).toString();
                    if(firstLine.contains("Weiterleiten an ")) {
                        String coordinateString = firstLine.replace("Weiterleiten an ", "").replace("\"", "").replace(".0", "").replace("§7", "").replace("§e", "").trim();
                        String[] coodStrings = coordinateString.split(";");
                        if(coodStrings.length == 3) {
                            int x = Integer.parseInt(coodStrings[0]);
                            int y = Integer.parseInt(coodStrings[1]);
                            int z = Integer.parseInt(coodStrings[2]);

                            MovingObjectPosition mop = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1.0F);
                            if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {

                                HopperConnection newConn = new HopperConnection(mop.getBlockPos(), new BlockPos(x, y, z), null, CBtracker.getCurrentCB());
                                conntections.put(newConn.toString(), newConn);
                            }
                        }
                    }
                }
            }
        }
    }

    public void messageReceived(ChatReceiveEvent event) {
        String message = event.chatMessage().getPlainText();
        switch (message) {
            case "[Trichter] Das Verbinden wurde aktiviert. Klicke auf den gewünschten Endpunkt.",
                 "[Trichter] Das Multi-Verbinden wurde aktiviert. Klicke mit dem gewünschten Item auf den gewünschten Endpunkt." -> {
                MovingObjectPosition mop = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1.0F);
                if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
                    BlockPos blockPos = mop.getBlockPos();
                    IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);

                    if (state.getBlock() == Blocks.hopper) {
                        currentConnectingHopper = blockPos;
                    }
                }
            }
            case "[Trichter] Der Trichter wurde erfolgreich verbunden.",
                 "[Trichter] Der Verbindungsmodus wurde beendet.",
                 "[Trichter] Der Startpunkt ist zu weit entfernt. Bitte starte erneut." -> {
                if (message.equals("[Trichter] Der Trichter wurde erfolgreich verbunden.")) {
                    MovingObjectPosition mop = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1.0F);
                    if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {

                        HopperConnection newConn = new HopperConnection(currentConnectingHopper, mop.getBlockPos(), null, CBtracker.getCurrentCB());
                        conntections.put(newConn.toString(), newConn);
                    }
                }

                currentConnectingHopper = null;
            }
            case "[Trichter] Die Multi-Verbindung wurde hinzugefügt." -> {
                MovingObjectPosition mop = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1.0F);
                if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
                    ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem();
                    HopperConnection newConn = new HopperConnection(currentConnectingHopper, mop.getBlockPos(), stack, CBtracker.getCurrentCB());
                    conntections.put(newConn.toString(), newConn);
                }
            }
            case "[Trichter] Die Mehrfach-Verbindungen wurden aufgehoben." -> {
                ArrayList<String> removeKeys = new ArrayList<>();
                MovingObjectPosition mop = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1.0F);
                if(mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
                    for (String key : conntections.keySet()) {
                        HopperConnection connection = conntections.get(key);
                        if(connection.stack != null) {
                            if(connection.pos1.equals(mop.getBlockPos())) {
                                removeKeys.add(key);
                            }
                        }
                    }

                    for (String removeKey : removeKeys) {
                        conntections.remove(removeKey);
                    }
                }
            }
            case "[Trichter] Die Verbindung wurde aufgehoben." -> {
                String removeKey = null;
                MovingObjectPosition mop = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1.0F);
                if(mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
                    for (String key : conntections.keySet()) {
                        HopperConnection connection = conntections.get(key);
                        if(connection.stack == null) {
                            if(connection.pos1.equals(mop.getBlockPos())) {
                                removeKey = key;
                                break;
                            }
                        }
                    }

                    if(removeKey != null) {
                        conntections.remove(removeKey);
                    }
                }
            }
        }
    }

    public void cbChanged() {
        currentConnectingHopper = null;
    }

    public void renderWorld() {

        for (Entry<String, HopperConnection> entry : conntections.entrySet()) {
            HopperConnection conntection = entry.getValue();

            double distance = conntection.pos1.distanceSq(Minecraft.getMinecraft().thePlayer.posX,
                Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ);
            double actualDistance = Math.sqrt(distance);

            if (actualDistance > 30)
                continue;

            if (conntection.cb == CBtracker.getCurrentCB()) {
                if (Minecraft.getMinecraft().theWorld.getBlockState(conntection.pos1()).getBlock()
                    == Blocks.hopper) {
                    drawLineBetween(conntection);
                }
            }
        }

        if(currentConnectingHopper != null) {
            drawSphere(currentConnectingHopper);
        }
    }

    private void drawLineBetween(HopperConnection connection) {
        if(!Addon.getSharedInstance().configuration().getHopperSubConfig().getShowLines().get())
            return;

        Minecraft mc = Minecraft.getMinecraft();

        double x1 = connection.pos1.getX() - mc.getRenderManager().viewerPosX + 0.5;
        double y1 = connection.pos1.getY() - mc.getRenderManager().viewerPosY + 0.5;
        double z1 = connection.pos1.getZ() - mc.getRenderManager().viewerPosZ + 0.5;

        double x2 = connection.pos2.getX() - mc.getRenderManager().viewerPosX + 0.5;
        double y2 = connection.pos2.getY() - mc.getRenderManager().viewerPosY + 0.5;
        double z2 = connection.pos2.getZ() - mc.getRenderManager().viewerPosZ + 0.5;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();

        GL11.glLineWidth(64.0F);
        GL11.glBegin(GL11.GL_LINES);

        if (connection.stack == null) {
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

        if (connection.stack != null) {
            long time = mc.theWorld.getTotalWorldTime();
            double animationDuration = 100.0;

            double progress = (time % (int) animationDuration) / animationDuration;

            double animX = x1 + (x2 - x1) * progress;
            double animY = y1 + (y2 - y1) * progress;
            double animZ = z1 + (z2 - z1) * progress;

            renderItemStackAt(connection.stack, animX, animY, animZ);
        }
    }

    private void renderItemStackAt(ItemStack stack, double x, double y, double z) {
        if(!Addon.getSharedInstance().configuration().getHopperSubConfig().getShowLines().get())
            return;

        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y, z);

        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        GlStateManager.disableLighting();

        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        mc.getRenderItem().renderItem(stack, TransformType.FIXED);

        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    private void drawSphere(BlockPos center) {
        if(!Addon.getSharedInstance().configuration().getHopperSubConfig().getShowRadius().get())
            return;

        Minecraft mc = Minecraft.getMinecraft();

        double cx = center.getX() - mc.getRenderManager().viewerPosX;
        double cy = center.getY() - mc.getRenderManager().viewerPosY;
        double cz = center.getZ() - mc.getRenderManager().viewerPosZ;

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


    public void resetConnections() {
        conntections = new HashMap<>();
    }
}
