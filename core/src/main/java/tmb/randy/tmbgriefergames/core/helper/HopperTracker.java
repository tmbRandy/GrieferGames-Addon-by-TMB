package tmb.randy.tmbgriefergames.core.helper;

import net.labymod.api.Laby;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.api.enums.HopperState;
import tmb.randy.tmbgriefergames.api.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.api.events.HopperStateChangedEvent;

public class HopperTracker {
    private static HopperState currentHopperState = HopperState.NONE;

    @Subscribe
    public void cbSwitch(CbChangedEvent event) {
        if(Addon.isGG()) setCurrentHopperState(HopperState.NONE);
    }

    @Subscribe
    public void messageReceived(ChatReceiveEvent event) {
        if(!Addon.isGG())
            return;

        String message = event.chatMessage().getPlainText();

        setCurrentHopperState(switch (message) {
            case Const.Chat.TRICHTER_MULTI_CONNECT_START -> HopperState.MULTICONNECT;
            case Const.Chat.TRICHTER_CONNECT_START -> HopperState.CONNECT;
            case Const.Chat.TRICHTER_CONNECTED,
                 Const.Chat.TRICHTER_CONNECT_ENDED,
                 Const.Chat.TRICHTER_START_TOO_FAR -> HopperState.NONE;
            default -> currentHopperState;
        });
    }

    public static HopperState getCurrentHopperState() {return currentHopperState;}

    public static void setCurrentHopperState(HopperState newHopperState) {
        if(currentHopperState != newHopperState) {
            HopperState oldVal = currentHopperState;
            currentHopperState = newHopperState;
            Laby.fireEvent(new HopperStateChangedEvent(oldVal, newHopperState));
        }
    }
}
