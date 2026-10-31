package tmb.randy.tmbgriefergames.core.commands;

import java.util.Arrays;
import net.labymod.api.Laby;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.flow.Flow;
import tmb.randy.tmbgriefergames.core.flow.FlowManager;
import tmb.randy.tmbgriefergames.core.flow.editor.FlowEditorActivity;

/**
 * /flow              opens the editor
 * /flow edit [name]  opens the editor with the given flow
 * /flow list         lists all flows
 * /flow stop         stops all running flows
 * /flow [name] [args]  starts (with ${arg1}, ...) or stops the flow
 */
public class FlowCommand extends DescribedCommand {

    public FlowCommand() {
        super("flow", "ablauf");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        FlowManager manager = FlowManager.get();

        if (arguments.length == 0 || arguments[0].equalsIgnoreCase("edit")) {
            Flow flow = arguments.length > 1 ? manager.getFlow(String.join(" ", arguments).substring(5)) : null;
            Laby.labyAPI().minecraft().executeNextTick(() -> Laby.labyAPI().minecraft().minecraftWindow().displayScreen(new FlowEditorActivity(flow)));
            return true;
        }

        switch (arguments[0].toLowerCase()) {
            case "list" -> {
                if (manager.getFlows().isEmpty())
                    Addon.displayNotification(Addon.translate("flow.noFlows"));
                for (Flow flow : manager.getFlows())
                    Addon.displayNotification((manager.isRunning(flow.getName()) ? "§a▶ " : "§7■ ") + "§f" + flow.getName());
            }
            case "stop" -> manager.stopAll();
            default -> {
                // Flow names may contain spaces: the longest matching name wins, the rest are arguments
                for (int words = arguments.length; words > 0; words--) {
                    Flow flow = manager.getFlow(String.join(" ", Arrays.copyOfRange(arguments, 0, words)));
                    if (flow == null)
                        continue;

                    if (manager.isRunning(flow.getName()))
                        manager.stop(flow.getName());
                    else
                        manager.start(flow, Arrays.copyOfRange(arguments, words, arguments.length));
                    return true;
                }
                Addon.displayNotification(Addon.translate("flow.unknown", String.join(" ", arguments)));
            }
        }
        return true;
    }
}
