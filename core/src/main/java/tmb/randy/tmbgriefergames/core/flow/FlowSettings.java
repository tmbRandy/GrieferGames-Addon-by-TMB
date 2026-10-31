package tmb.randy.tmbgriefergames.core.flow;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.config.HopperSubConfig;
import tmb.randy.tmbgriefergames.core.config.SwordsSubConfig;

/**
 * Addon settings that flows can read as ${setting.name}, so built-in flows keep using the values the
 * player configured in the LabyMod settings.
 */
public final class FlowSettings {
    public static final String PREFIX = "setting.";

    private static final Map<String, Supplier<Object>> SETTINGS = new LinkedHashMap<>();

    static {
        register("vabk.loadTime", () -> swords().getVABKloadTime().get());
        register("vabk.switchCooldown", () -> swords().getVABKswitchCooldown().get());
        register("habk.cooldown", () -> swords().getHABKcooldown().get());

        register("hopper.enabled", () -> hopper().getEnabled().get());
        register("hopper.filter", () -> hopper().getFilterItem().get());
        register("hopper.radius", () -> hopper().getRadius().get());
        register("hopper.stackSize", () -> hopper().getStackSize().get());
        register("hopper.finalAction", () -> hopper().getFinalAction().get());
        register("hopper.autoSneak", () -> hopper().getAutoSneak().get());

        register("autoLoot", () -> Addon.settings().getAutoLoot().get());
    }

    private FlowSettings() {}

    private static void register(String name, Supplier<Object> value) {
        SETTINGS.put(name, value);
    }

    /** The current value of a setting (without the "setting." prefix), empty if it does not exist. */
    public static String get(String name) {
        Supplier<Object> value = SETTINGS.get(name);
        return value == null ? "" : String.valueOf(value.get());
    }

    private static SwordsSubConfig swords() {
        return Addon.settings().getSwordsSubConfig();
    }

    private static HopperSubConfig hopper() {
        return Addon.settings().getHopperSubConfig();
    }
}
