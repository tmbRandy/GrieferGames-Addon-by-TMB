package tmb.randy.tmbgriefergames.core.commands;

import net.labymod.api.client.chat.command.Command;
import org.jetbrains.annotations.NotNull;
import tmb.randy.tmbgriefergames.core.Addon;

public abstract class DescribedCommand extends Command {

    protected DescribedCommand(@NotNull String prefix, @NotNull String... aliases) {
        super(prefix, aliases);
    }

    public boolean matches(String name) {
        if (prefix.equalsIgnoreCase(name))
            return true;
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public String getDescription() {
        return Addon.translate("commands." + this.prefix);
    }
}
