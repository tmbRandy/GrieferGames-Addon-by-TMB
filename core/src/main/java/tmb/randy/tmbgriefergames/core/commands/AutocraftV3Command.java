package tmb.randy.tmbgriefergames.core.commands;

import net.labymod.api.client.chat.command.Command;
import tmb.randy.tmbgriefergames.core.Addon;

public class AutocraftV3Command extends Command {

    public AutocraftV3Command() {
        super("craftV3");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        if(!Addon.isGG())
            return false;

        Addon.getSharedInstance().getBridge().startAutocrafterV3();
        return true;
    }
}