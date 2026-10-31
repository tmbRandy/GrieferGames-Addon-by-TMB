package tmb.randy.tmbgriefergames.core.commands;

import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.flow.Flow;
import tmb.randy.tmbgriefergames.core.flow.FlowManager;
import tmb.randy.tmbgriefergames.core.helper.Commander;

/** /fahndung [player]: starts or stops the built-in "Fahndung" flow. */
public class PlayerTracerCommand extends DescribedCommand {
    public static final String FLOW = "Fahndung";

    public PlayerTracerCommand() {
        super("fahndung");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        Flow flow = FlowManager.get().getFlow(FLOW);
        if (!Addon.isGG() || flow == null)
            return false;

        if (FlowManager.get().isRunning(FLOW))
            FlowManager.get().stop(FLOW);
        else if (Commander.canSend())
            FlowManager.get().start(flow, arguments);
        return true;
    }
}
