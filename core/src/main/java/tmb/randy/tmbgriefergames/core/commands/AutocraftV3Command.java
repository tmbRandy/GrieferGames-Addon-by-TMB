package tmb.randy.tmbgriefergames.core.commands;

import net.labymod.api.Laby;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.FunctionState;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.events.ToggleFunctionEvent;

public class AutocraftV3Command extends DescribedCommand {

    public AutocraftV3Command() {
        super("craftV3");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        if(!Addon.isGG())
            return false;

        Laby.fireEvent(new ToggleFunctionEvent(Functions.CRAFTV3, FunctionState.TOGGLE, arguments));
        return true;
    }
}