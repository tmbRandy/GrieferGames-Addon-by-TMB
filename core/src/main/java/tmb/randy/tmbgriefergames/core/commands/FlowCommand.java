package tmb.randy.tmbgriefergames.core.commands;

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
 * /flow [name]       starts or stops the flow
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
                Flow flow = manager.getFlow(String.join(" ", arguments));
                if (flow == null)
                    Addon.displayNotification(Addon.translate("flow.unknown", String.join(" ", arguments)));
                else
                    manager.toggle(flow);
            }
        }
        return true;
    }
}
