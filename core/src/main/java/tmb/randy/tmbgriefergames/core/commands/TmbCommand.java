package tmb.randy.tmbgriefergames.core.commands;

import net.labymod.api.client.chat.command.Command;
import net.labymod.api.util.I18n;
import org.jetbrains.annotations.NotNull;
import tmb.randy.tmbgriefergames.core.Addon;

public abstract class TmbCommand extends Command {

    protected TmbCommand(@NotNull String prefix, @NotNull String... aliases) {
        super(prefix, aliases);
    }

    public String getDescription() {
        return I18n.translate(Addon.getSharedInstance().addonInfo().getNamespace() + ".commands." + this.prefix);
    }
}
