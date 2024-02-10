package tmb.randy.tmbgriefergames.core.commands;

import net.labymod.api.client.chat.command.Command;
import tmb.randy.tmbgriefergames.core.Addon;

public class AutocraftCommand extends Command {

    public AutocraftCommand() {
        super("autocraft");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {

        Addon.getSharedInstance().getBridge().startNewAutocrafter();

        return true;
    }
}