package tmb.randy.tmbgriefergames.core.commands;

import net.labymod.api.Laby;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.FunctionState;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.events.ToggleFunctionEvent;

public class EjectCommand extends DescribedCommand {

    public EjectCommand() {
        super("auswurf");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        if(!Addon.isGG())
            return false;

        Laby.fireEvent(new ToggleFunctionEvent(Functions.EJECT, FunctionState.TOGGLE, arguments));

        return true;
    }
}
