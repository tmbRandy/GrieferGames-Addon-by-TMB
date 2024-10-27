package tmb.randy.tmbgriefergames.core.util;

import net.labymod.api.Laby;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.HopperState;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.events.HopperStateChangedEvent;

public class HopperTracker {
    private static HopperState currentHopperState = HopperState.NONE;

    @Subscribe
    public void cbSwitch(CbChangedEvent event) {
        setCurrentHopperState(HopperState.NONE);
    }

    @Subscribe
    public void messageReceived(ChatReceiveEvent event) {
        if(!Addon.isGG())
            return;

        String message = event.chatMessage().getPlainText();

        setCurrentHopperState(switch (message) {
            case "[Trichter] Das Multi-Verbinden wurde aktiviert. Klicke mit dem gewünschten Item auf den gewünschten Endpunkt." -> HopperState.MULTICONNECT;
            case "[Trichter] Das Verbinden wurde aktiviert. Klicke auf den gewünschten Endpunkt." -> HopperState.CONNECT;
            case "[Trichter] Der Trichter wurde erfolgreich verbunden.",
                 "[Trichter] Der Verbindungsmodus wurde beendet.",
                 "[Trichter] Der Startpunkt ist zu weit entfernt. Bitte starte erneut." -> HopperState.NONE;
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
