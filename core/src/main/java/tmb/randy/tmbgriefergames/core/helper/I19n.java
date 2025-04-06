package tmb.randy.tmbgriefergames.core.helper;

import net.labymod.api.util.I18n;
import org.jetbrains.annotations.NotNull;
import tmb.randy.tmbgriefergames.core.Addon;

public class I19n {
    public static @NotNull String translate(@NotNull String key, Object... args) {
        return I18n.translate(Addon.getNamespace() + "." + key, args);
    }
}