package tmb.randy.tmbgriefergames.core.events;

import net.labymod.api.event.Event;
import tmb.randy.tmbgriefergames.core.enums.HopperState;

public class HopperStateChangedEvent implements Event {
    private final HopperState oldState;
    private final HopperState newState;

    public HopperStateChangedEvent(HopperState oldState, HopperState newState) {
        this.oldState = oldState;
        this.newState = newState;
    }

    public HopperState getOldState() {return oldState;}
    public HopperState getNewState() {return newState;}
}
