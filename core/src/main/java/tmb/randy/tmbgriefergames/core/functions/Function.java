package tmb.randy.tmbgriefergames.core.functions;


import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.event.client.scoreboard.ScoreboardTeamEntryAddEvent;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.events.FishEvent;
import tmb.randy.tmbgriefergames.core.events.HopperStateChangedEvent;
import tmb.randy.tmbgriefergames.core.events.ResetLinesEvent;
import tmb.randy.tmbgriefergames.core.events.ToggleFunctionEvent;

abstract public class Function {

    protected String identifier;

    public Function(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {return identifier;}

    public void tickEvent(GameTickEvent event) {}
    public void renderWorldEvent(RenderWorldEvent event) {}
    public void keyEvent(KeyEvent event) {}
    public void chatReceiveEvent(ChatReceiveEvent event) {}
    public void chatMessageSendEvent(ChatMessageSendEvent event) {}
    public void mouseButtonEvent(MouseButtonEvent event) {}
    public void itemStackTooltipEvent(ItemStackTooltipEvent event) {}
    public void serverDisconnectEvent(ServerDisconnectEvent event) {}

    public void cbChangedEvent(CbChangedEvent event) {}
    public void resetLinesEvent(ResetLinesEvent event) {}
    public void hopperStateChangedEvent(HopperStateChangedEvent event) {}
    public void toggleFunctionEvent(ToggleFunctionEvent event) {}
    public void scoreboardTeamEntryAddEvent(ScoreboardTeamEntryAddEvent event) {}
    public void fishEvent(FishEvent event) {}
}