package tmb.randy.tmbgriefergames.core.functions;


import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.events.HopperStateChangedEvent;
import tmb.randy.tmbgriefergames.core.events.ResetLinesEvent;
import tmb.randy.tmbgriefergames.core.events.ToggleFunctionEvent;

abstract public class Function {

    protected Functions type;

    public Function(Functions type) {
        this.type = type;
    }

    public Functions getType() {
        return type;
    }

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
}