package tmb.randy.tmbgriefergames.core.commands;

import net.labymod.api.client.chat.command.Command;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;

public class PlayerTracerCommand extends Command {
    public PlayerTracerCommand() {
        super("fahndung");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        if(!Addon.isGG())
            return false;

        switch (arguments.length) {
            case 0 -> {
                Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.playerTracer.startedHopping"));
                Addon.getSharedInstance().getPlayerTracer().startTracer(null);
            }
            case 1 -> {
                Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.playerTracer.lookingForPlayer", arguments[0]));
                Addon.getSharedInstance().getPlayerTracer().startTracer(arguments[0]);
            }
            default -> Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.playerTracer.tooManyArguments"));
        }

        return true;
    }
}
