package tmb.randy.tmbgriefergames.core.commands;

import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.FunctionState;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.helper.Commander;

public class PlayerTracerCommand extends DescribedCommand {
    public PlayerTracerCommand() {
        super("fahndung");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        if(!Addon.isGG())
            return false;

        if(Commander.canSend())
            Addon.toggleActiveFunction(Functions.PLAYERTRACER.name(), FunctionState.TOGGLE, arguments);

        return true;
    }
}
