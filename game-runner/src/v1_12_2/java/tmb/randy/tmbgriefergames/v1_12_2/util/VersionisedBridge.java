package tmb.randy.tmbgriefergames.v1_12_2.util;

import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.activity.types.IngameOverlayActivity;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent;
import net.labymod.api.event.client.render.overlay.IngameOverlayElementRenderEvent;
import net.labymod.api.event.client.render.overlay.IngameOverlayElementRenderEvent.OverlayElementType;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import net.labymod.api.event.client.world.WorldLoadEvent;
import net.labymod.api.models.Implements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.IBridge;
import tmb.randy.tmbgriefergames.v1_12_2.util.chat.ChatCleaner;
import tmb.randy.tmbgriefergames.v1_12_2.util.chat.CooldownNotifier;
import tmb.randy.tmbgriefergames.v1_12_2.util.chat.EmptyLinesRemover;
import tmb.randy.tmbgriefergames.v1_12_2.util.chat.NewsBlocker;
import tmb.randy.tmbgriefergames.v1_12_2.util.chat.PaymentValidator;
import tmb.randy.tmbgriefergames.v1_12_2.util.chat.StreamerMute;
import tmb.randy.tmbgriefergames.v1_12_2.util.chat.TypeCorrection;
import tmb.randy.tmbgriefergames.v1_12_2.util.click.ClickManager;
import javax.inject.Singleton;

@Singleton
@Implements(IBridge.class)
public class VersionisedBridge implements IBridge {
    private final ChatCleaner chatCleaner = new ChatCleaner();
    private final CooldownNotifier cooldownNotifier = new CooldownNotifier();
    private final EmptyLinesRemover emptyLinesRemover = new EmptyLinesRemover();
    private final NewsBlocker newsBlocker = new NewsBlocker();
    private final PaymentValidator paymentValidator = new PaymentValidator();
    private final StreamerMute streamerMute = new StreamerMute();
    private final TypeCorrection typeCorrection = new TypeCorrection();
    private final PlotSwitch plotSwitch = new PlotSwitch();
    private final ItemSaver itemSaver = new ItemSaver();
    private final TooltipExtension tooltipExtension = new TooltipExtension();
    private final PlayerTracer playerTracer = new PlayerTracer();
    private final CBTracker cbTracker = new CBTracker();
    private final ItemClearTimerListener itemClearTimerListener = new ItemClearTimerListener();
    private final AutoHopper autoHopper = new AutoHopper();
    private final FlyTimer flyTimer = new FlyTimer();
    private final ItemShifter itemShifter = new ItemShifter();
    private final NatureBordersRenderer natureBordersRenderer = new NatureBordersRenderer();
    private final AccountUnity accountUnity = new AccountUnity();
    private final AutoComp autoComp = new AutoComp();
    private final AutoCrafter autoCrafter = new AutoCrafter();
    private final AutoCrafterNew autoCrafterNew = new AutoCrafterNew();

    private GuiScreen lastGui;

    @Subscribe
    public void worldLoadEvent(WorldLoadEvent event) {
        // No checking for isGG as the function to auto skip the lobby/hub wouldn't work as the server adress is set too late.
        cbTracker.worldLoadEvent(event);
    }

    @Subscribe
    public void messageReceived(ChatReceiveEvent event) {
        if(!Addon.isGG())
            return;

        chatCleaner.messageReceived(event);
        emptyLinesRemover.messageReceived(event);
        newsBlocker.messageReceived(event);
        paymentValidator.messageReceived(event);
        streamerMute.messageReceived(event);
        plotSwitch.messageReceived(event);
        playerTracer.messageReceived(event);
        autoHopper.messageReceived(event);
        accountUnity.messageReceived(event);
    }

    @Subscribe
    public void messageSend(ChatMessageSendEvent event) {
        if(!Addon.isGG())
            return;

        cooldownNotifier.messageReceived(event);
        typeCorrection.messageSend(event);
        plotSwitch.messageSend(event);
    }

    @Subscribe (Priority.FIRST)
    public void mouseInput(MouseButtonEvent event) {
        if(!Addon.isGG())
            return;

        itemSaver.mouseInput(event);
        autoHopper.mouseInput(event);
        flyTimer.onMouseButtonEvent(event);
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
        if(!Laby.labyAPI().minecraft().isIngame() || Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null)
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

        ClickManager.getSharedInstance().tick(event);
        autoComp.onTickEvent(event);
        autoCrafter.onTickEvent(event);
        autoHopper.tick(event);
        itemShifter.tick(event);
        plotSwitch.tick(event);
        autoCrafterNew.onTickEvent(event);
    }

    @Subscribe
    public void keyDown(KeyEvent event) {
        if(!Addon.isGG())
            return;

        plotSwitch.keyDown(event);
        playerTracer.onKey(event);
        itemShifter.onKey(event);
        natureBordersRenderer.onKey(event);
        autoComp.onKeyEvent(event);
        autoCrafter.onKeyEvent(event);
        autoCrafterNew.onKeyEvent(event);
    }

    @Subscribe
    public void onRenderEvent(RenderWorldEvent event) {
        if(!Laby.labyAPI().minecraft().isIngame() || Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null || !Addon.isGG())
            return;

        natureBordersRenderer.onRender(event);
    }

    @Subscribe
    public void renderTooltip(ItemStackTooltipEvent event) {
        if(!Addon.isGG())
            return;

        tooltipExtension.renderTooltip(event);
        flyTimer.onTooltipEvent(event);
    }

    @Override
    public void startPlayerTracer(String name) {
        if(!Addon.isGG())
            return;

        playerTracer.startTracer(name);
    }

    public void onGuiOpenEvent(GuiScreen screen) {
        ClickManager.getSharedInstance().clearAllQueues();
        if(screen == null) {
            autoComp.stopComp();
            itemShifter.stopShifting();
        }
    }

    @Subscribe
    public void networkPayloadEvent(NetworkPayloadEvent event) {
        if(!Addon.isGG())
            return;

        itemClearTimerListener.networkPayloadEvent(event);
    }

    @Override
    public void cbChanged() {
        playerTracer.cbChanged();
        autoComp.stopComp();
        autoCrafterNew.stopCrafter();
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
    public String getItemRemoverValue() {
        return ItemClearTimerListener.getDisplayValue();
    }

    public static boolean isChatGuiOpen() {
        for (IngameOverlayActivity activity : Laby.labyAPI().ingameOverlay().getActivities()) {
            if(activity.isAcceptingInput()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void startNewAutocrafter() {
        autoCrafterNew.startCrafter();
    }

    @Override
    public boolean isCompActive() {
        return autoComp.isCompActive();
    }
}
