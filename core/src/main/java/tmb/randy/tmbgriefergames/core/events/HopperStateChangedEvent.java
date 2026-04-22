package tmb.randy.tmbgriefergames.core.events;

import net.labymod.api.event.Event;
import tmb.randy.tmbgriefergames.core.enums.HopperState;

public record HopperStateChangedEvent(HopperState oldState, HopperState newState) implements Event {}