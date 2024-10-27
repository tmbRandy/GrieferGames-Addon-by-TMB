package tmb.randy.tmbgriefergames.v1_8_9.util;

import javax.inject.Singleton;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.activity.types.IngameOverlayActivity;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.overlay.IngameOverlayElementRenderEvent;
import net.labymod.api.event.client.render.overlay.IngameOverlayElementRenderEvent.OverlayElementType;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import net.labymod.api.models.Implements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.lwjgl.input.Keyboard;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.IBridge;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.events.HopperStateChangedEvent;
import tmb.randy.tmbgriefergames.v1_8_9.util.AutoCrafter.AutoCrafterV1;
import tmb.randy.tmbgriefergames.v1_8_9.util.AutoCrafter.AutoCrafterV2;
import tmb.randy.tmbgriefergames.v1_8_9.util.AutoCrafter.AutoCrafterV3;
import tmb.randy.tmbgriefergames.v1_8_9.util.click.ClickManager;

@Singleton
@Implements(IBridge.class)
public class VersionisedBridge implements IBridge {
    private static VersionisedBridge sharedInstance;
    private final TooltipExtension tooltipExtension = new TooltipExtension();
    private final AutoHopper autoHopper = new AutoHopper();
    private final FlyTimer flyTimer = new FlyTimer();
    private final ItemShifter itemShifter = new ItemShifter();
    private final NatureBordersRenderer natureBordersRenderer = new NatureBordersRenderer();
    private final AutoCrafterV1 autoCrafterV1 = new AutoCrafterV1();
    private final AutoCrafterV2 autoCrafterV2 = new AutoCrafterV2();
    private final AutoCrafterV3 autoCrafterV3 = new AutoCrafterV3();
    private final AutoDecomp autoDecomp = new AutoDecomp();
    private final AutoComp autoComp = new AutoComp();
    private final Eject auswurf = new Eject();
    private final HABK habk = new HABK();
    private final VABK vabk = new VABK();
    private final HopperConnections hopperConnections = new HopperConnections();
    private final AutoLoot autoLoot = new AutoLoot();

    private GuiScreen lastGui;

    public VersionisedBridge() {
        sharedInstance = this;
    }

    @Subscribe
    public void cbChanged(CbChangedEvent event) {
        if(!Addon.isGG())
            return;

        autoComp.stopComp();
        autoCrafterV2.stopCrafter();
        autoCrafterV3.stop();
        hopperConnections.cbChanged();
    }

    @Subscribe
    public void messageReceived(ChatReceiveEvent event) {
        if(!Addon.isGG())
            return;

        autoHopper.messageReceived(event);
        autoCrafterV3.chatMessageReceived(event);
        hopperConnections.messageReceived(event);
        autoLoot.chatMessageReceived(event);
    }

    @Subscribe (Priority.FIRST)
    public void mouseInput(MouseButtonEvent event) {
        if(!Addon.isGG())
            return;

        autoHopper.mouseInput(event);
        flyTimer.onMouseButtonEvent(event);
        habk.onMouseButtonEvent(event);
    }

    @Subscribe
    public void onScoreboardRender(IngameOverlayElementRenderEvent event) {
        if(!Addon.isGG())
            return;

        if (Addon.getSharedInstance().getGameInfoWidget().isEnabled() && event.elementType() == OverlayElementType.SCOREBOARD && Addon.getSharedInstance().getGameInfoWidget().isVisibleInGame() && event.phase() == Phase.PRE) {
            event.setCancelled(true);
        }
    }

    @Subscribe
    public void tick(GameTickEvent event) {
        if(!Laby.labyAPI().minecraft().isIngame() || Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null || !Addon.isGG())
            return;

        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

        if (lastGui != currentScreen) {
            if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest
                || Minecraft.getMinecraft().currentScreen instanceof GuiCrafting
                || Minecraft.getMinecraft().currentScreen instanceof GuiInventory)) {
                ClickManager.getSharedInstance().clearAllQueues();
                autoComp.stopComp();
                ItemShifter.getSharedInsance().stopShifting();
            }

            onGuiOpenEvent(currentScreen);
            lastGui = currentScreen;
        }

        ClickManager.getSharedInstance().tick();
        autoComp.onTickEvent();
        autoCrafterV1.onTickEvent();
        autoHopper.tick();
        itemShifter.tick();
        autoCrafterV2.onTickEvent(event);
        autoDecomp.onTickEvent(event);
        auswurf.onTickEvent();
        autoCrafterV3.onTick();
        vabk.onTickEvent(event);
        flyTimer.tick();
    }

    @Subscribe
    public void hopperStateChanged(HopperStateChangedEvent event) {
        if(!Addon.isGG())
            return;

        hopperConnections.hopperStateChanged(event);
    }

    @Subscribe
    public void keyDown(KeyEvent event) {
        if(!Addon.isGG())
            return;

        itemShifter.onKey(event);
        natureBordersRenderer.onKey();
        autoComp.onKeyEvent(event);
        autoCrafterV1.onKeyEvent(event);
        autoCrafterV2.onKeyEvent(event);
        autoDecomp.onKeyEvent();
        auswurf.onKeyEvent(event);
        autoCrafterV3.onKey(event);
        vabk.onKeyEvent(event);
    }

    @Subscribe
    public void onRenderEvent(RenderWorldEvent event) {
        if(!Laby.labyAPI().minecraft().isIngame() || Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null || !Addon.isGG())
            return;

        natureBordersRenderer.onRender(event);
        hopperConnections.renderWorld();
    }

    @Subscribe
    public void renderTooltip(ItemStackTooltipEvent event) {
        if(!Addon.isGG())
            return;

        tooltipExtension.renderTooltip(event);
        flyTimer.onTooltipEvent(event);
    }


    public void onGuiOpenEvent(GuiScreen screen) {
        ClickManager.getSharedInstance().clearAllQueues();
        if(screen == null) {
            autoComp.stopComp();
            itemShifter.stopShifting();
        } else
            hopperConnections.onGuiOpenEvent();
    }

    @Override
    public boolean isFlyCountdownActive() {
        return flyTimer.isTotalCountdownActive();
    }

    @Override
    public String getWidgetString() {
        return flyTimer.getWidgetString();
    }

    @Override
    public void startNewAutocrafter() {
        autoCrafterV2.startCrafter();
    }

    @Override
    public boolean isCompActive() {
        return autoComp.isCompActive();
    }

    @Override
    public void changeSlot(int slot) {Minecraft.getMinecraft().thePlayer.inventory.currentItem = slot;}

    @Override
    public void startAutocrafterV3() {
        autoCrafterV3.toggle();
    }

    @Override
    public boolean isChatGuiClosed() {
        for (IngameOverlayActivity activity : Laby.labyAPI().ingameOverlay().getActivities()) {
            if(activity.isAcceptingInput()) {
                return false;
            }
        }

        return true;
    }

    public static boolean isGUIOpen() {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

        return currentScreen instanceof GuiChest || currentScreen instanceof GuiInventory || currentScreen instanceof GuiCrafting;
    }

    @Override
    public boolean allKeysPressed(Key[] keys) {
        if(keys.length == 0)
            return false;

        if(isGUIOpen())
            return false;

        for (Key key : keys) {
            if(!Keyboard.isKeyDown(key.getId()))
                return false;
        }
        return true;
    }

    @Override
    public void startAuswurf() {
        auswurf.startAuswurf();
    }

    @Override
    public void openChat() {

    }

    public static VersionisedBridge getSharedInstance() {return sharedInstance;}

    @Override
    public void resetLines() {
        hopperConnections.resetConnections();
    }
}
