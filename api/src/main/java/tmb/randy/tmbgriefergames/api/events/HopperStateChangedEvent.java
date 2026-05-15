package tmb.randy.tmbgriefergames.api.events;

import net.labymod.api.event.Event;
import tmb.randy.tmbgriefergames.api.enums.HopperState;

public record HopperStateChangedEvent(HopperState oldState, HopperState newState) implements Event {}
