package tmb.randy.tmbgriefergames.core.commands;

import net.labymod.api.client.chat.command.Command;
import tmb.randy.tmbgriefergames.core.Addon;

public class EjectCommand extends Command {

    public EjectCommand() {
        super("auswurf");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        if(!Addon.isGG())
            return false;

        Addon.getSharedInstance().getBridge().startAuswurf();

        return true;
    }
}
