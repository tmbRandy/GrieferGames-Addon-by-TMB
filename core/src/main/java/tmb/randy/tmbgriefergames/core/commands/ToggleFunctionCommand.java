package tmb.randy.tmbgriefergames.core.commands;

import org.jetbrains.annotations.NotNull;
import tmb.randy.tmbgriefergames.api.enums.FunctionState;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.helper.Commander;

/** A command that starts or stops an active function, e.g. /auswurf. */
public class ToggleFunctionCommand extends DescribedCommand {
    private final Functions function;

    public ToggleFunctionCommand(Functions function, @NotNull String prefix, @NotNull String... aliases) {
        super(prefix, aliases);
        this.function = function;
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        if (!Addon.isGG())
            return false;

        if (Commander.canSend())
            Addon.toggleActiveFunction(function.name(), FunctionState.TOGGLE);

        return true;
    }
}
