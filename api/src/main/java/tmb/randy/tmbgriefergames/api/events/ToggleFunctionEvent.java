package tmb.randy.tmbgriefergames.api.events;

import net.labymod.api.event.Event;
import tmb.randy.tmbgriefergames.api.enums.FunctionState;

public record ToggleFunctionEvent(String function, FunctionState state, String[] arguments) implements Event {}
