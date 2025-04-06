package tmb.randy.tmbgriefergames.core.commands;

import net.labymod.api.client.chat.command.Command;
import org.jetbrains.annotations.NotNull;
import tmb.randy.tmbgriefergames.core.helper.I19n;

public abstract class DescribedCommand extends Command {

    protected DescribedCommand(@NotNull String prefix, @NotNull String... aliases) {
        super(prefix, aliases);
    }

    public String getDescription() {
        return I19n.translate("commands." + this.prefix);
    }
}
