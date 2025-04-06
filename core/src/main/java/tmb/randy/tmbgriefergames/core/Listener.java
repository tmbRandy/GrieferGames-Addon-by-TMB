package tmb.randy.tmbgriefergames.core;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.events.HopperStateChangedEvent;
import tmb.randy.tmbgriefergames.core.events.ResetLinesEvent;
import tmb.randy.tmbgriefergames.core.events.ToggleFunctionEvent;
import tmb.randy.tmbgriefergames.core.functions.Function;

public class Listener {

    @Subscribe
    public void gameTickEvent(GameTickEvent e) {
        if(!Addon.isGG()) return;

        for (Function function : Addon.getSharedInstance().getFunctions())
            function.tickEvent(e);
    }

    @Subscribe
    public void chatReceiveEvent(ChatReceiveEvent e) {
        if(!Addon.isGG()) return;

        for (Function function : Addon.getSharedInstance().getFunctions())
            function.chatReceiveEvent(e);
    }

    @Subscribe
    public void chatMessageSendEvent(ChatMessageSendEvent e) {
        if(!Addon.isGG()) return;

        for (Function function : Addon.getSharedInstance().getFunctions())
            function.chatMessageSendEvent(e);
    }

    @Subscribe
    public void renderWorldEvent(RenderWorldEvent e) {
        if(!Addon.isGG()) return;

        for (Function function : Addon.getSharedInstance().getFunctions())
            function.renderWorldEvent(e);
    }

    @Subscribe
    public void keyEvent(KeyEvent e) {
        if(!Addon.isGG()) return;

        for (Function function : Addon.getSharedInstance().getFunctions())
            function.keyEvent(e);
    }

    @Subscribe
    public void mouseButtonEvent(MouseButtonEvent e) {
        if(!Addon.isGG()) return;

        for (Function function : Addon.getSharedInstance().getFunctions())
            function.mouseButtonEvent(e);
    }

    @Subscribe
    public void itemStackTooltipEvent(ItemStackTooltipEvent e) {
        if(!Addon.isGG()) return;

        for (Function function : Addon.getSharedInstance().getFunctions())
            function.itemStackTooltipEvent(e);
    }

    @Subscribe
    public void serverDisconnectEvent(ServerDisconnectEvent e) {
        if(!Addon.isGG()) return;

        for (Function function : Addon.getSharedInstance().getFunctions())
            function.serverDisconnectEvent(e);
    }

    // Custom events

    @Subscribe
    public void cbChangedEvent(CbChangedEvent e) {
        if(!Addon.isGG()) return;

        for (Function function : Addon.getSharedInstance().getFunctions())
            function.cbChangedEvent(e);
    }

    @Subscribe
    public void resetLinesEvent(ResetLinesEvent e) {
        if(!Addon.isGG()) return;

        for (Function function : Addon.getSharedInstance().getFunctions())
            function.resetLinesEvent(e);
    }

    @Subscribe
    public void hopperStateChangedEvent(HopperStateChangedEvent e) {
        if(!Addon.isGG()) return;

        for (Function function : Addon.getSharedInstance().getFunctions())
            function.hopperStateChangedEvent(e);
    }

    @Subscribe
    public void toggleFunctionEvent(ToggleFunctionEvent e) {
        if(!Addon.isGG()) return;

        for (Function function : Addon.getSharedInstance().getFunctions())
            function.toggleFunctionEvent(e);
    }
}
