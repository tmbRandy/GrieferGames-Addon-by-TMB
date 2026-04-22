package tmb.randy.tmbgriefergames.core.events;

import net.labymod.api.event.Event;
import tmb.randy.tmbgriefergames.core.enums.CBs;

public record CbChangedEvent(CBs CB) implements Event {}
