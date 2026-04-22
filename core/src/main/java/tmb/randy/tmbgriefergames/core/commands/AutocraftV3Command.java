package tmb.randy.tmbgriefergames.core.commands;

import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.FunctionState;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.helper.Commander;

public class AutocraftV3Command extends DescribedCommand {

    public AutocraftV3Command() {
        super("craftV3");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        if(!Addon.isGG())
            return false;

        if(Commander.canSend())
            Addon.toggleActiveFunction(Functions.CRAFTV3.name(), FunctionState.TOGGLE);

        return true;
    }
}