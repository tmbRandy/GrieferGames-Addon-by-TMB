package tmb.randy.tmbgriefergames.core;

import java.util.function.BiConsumer;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.event.client.scoreboard.ScoreboardTeamEntryAddEvent;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import tmb.randy.tmbgriefergames.api.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.api.events.FishEvent;
import tmb.randy.tmbgriefergames.api.events.HopperStateChangedEvent;
import tmb.randy.tmbgriefergames.api.events.ResetLinesEvent;
import tmb.randy.tmbgriefergames.api.events.ToggleFunctionEvent;
import tmb.randy.tmbgriefergames.api.functions.Function;

/** Forwards all events to the registered functions while the player is on GrieferGames. */
public class Listener {

    private static <E> void dispatch(E event, BiConsumer<Function, E> handler) {
        if (!Addon.isGG())
            return;

        for (Function function : Addon.getFunctions())
            handler.accept(function, event);
    }

    @Subscribe
    public void gameTickEvent(GameTickEvent e) { dispatch(e, Function::tickEvent); }

    @Subscribe
    public void chatReceiveEvent(ChatReceiveEvent e) { dispatch(e, Function::chatReceiveEvent); }

    @Subscribe
    public void chatMessageSendEvent(ChatMessageSendEvent e) { dispatch(e, Function::chatMessageSendEvent); }

    @Subscribe
    public void renderWorldEvent(RenderWorldEvent e) { dispatch(e, Function::renderWorldEvent); }

    @Subscribe
    public void keyEvent(KeyEvent e) { dispatch(e, Function::keyEvent); }

    @Subscribe
    public void mouseButtonEvent(MouseButtonEvent e) { dispatch(e, Function::mouseButtonEvent); }

    @Subscribe
    public void itemStackTooltipEvent(ItemStackTooltipEvent e) { dispatch(e, Function::itemStackTooltipEvent); }

    @Subscribe
    public void serverDisconnectEvent(ServerDisconnectEvent e) { dispatch(e, Function::serverDisconnectEvent); }

    @Subscribe
    public void scoreboardTeamEntryAddEvent(ScoreboardTeamEntryAddEvent e) { dispatch(e, Function::scoreboardTeamEntryAddEvent); }

    // Custom events

    @Subscribe
    public void cbChangedEvent(CbChangedEvent e) { dispatch(e, Function::cbChangedEvent); }

    @Subscribe
    public void resetLinesEvent(ResetLinesEvent e) { dispatch(e, Function::resetLinesEvent); }

    @Subscribe
    public void hopperStateChangedEvent(HopperStateChangedEvent e) { dispatch(e, Function::hopperStateChangedEvent); }

    @Subscribe
    public void toggleFunctionEvent(ToggleFunctionEvent e) { dispatch(e, Function::toggleFunctionEvent); }

    @Subscribe
    public void fishEvent(FishEvent e) { dispatch(e, Function::fishEvent); }
}
