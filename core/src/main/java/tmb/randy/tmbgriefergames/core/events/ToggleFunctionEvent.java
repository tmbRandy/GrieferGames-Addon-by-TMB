package tmb.randy.tmbgriefergames.core.events;

import net.labymod.api.event.Event;
import tmb.randy.tmbgriefergames.core.enums.FunctionState;

public record ToggleFunctionEvent(String function, FunctionState state, String[] arguments) implements Event {}