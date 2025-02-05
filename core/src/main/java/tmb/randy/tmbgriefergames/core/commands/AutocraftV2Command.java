package tmb.randy.tmbgriefergames.core.commands;

import tmb.randy.tmbgriefergames.core.Addon;

public class AutocraftV2Command extends TmbCommand {

    public AutocraftV2Command() {
        super("autocraft");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        if(!Addon.isGG())
            return false;

        Addon.getSharedInstance().getBridge().startNewAutocrafter();

        return true;
    }
}