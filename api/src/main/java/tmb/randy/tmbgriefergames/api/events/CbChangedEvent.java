package tmb.randy.tmbgriefergames.api.events;

import net.labymod.api.event.Event;
import tmb.randy.tmbgriefergames.api.enums.CBs;

public record CbChangedEvent(CBs CB) implements Event {}
