package tmb.randy.tmbgriefergames.core.events;

import net.labymod.api.event.Event;
import tmb.randy.tmbgriefergames.core.enums.FunctionState;
import tmb.randy.tmbgriefergames.core.enums.Functions;

public record ToggleFunctionEvent(Functions function, FunctionState state, String[] arguments) implements Event {}