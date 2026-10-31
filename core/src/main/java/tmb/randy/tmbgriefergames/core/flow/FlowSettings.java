package tmb.randy.tmbgriefergames.core.flow;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.labymod.api.client.gui.screen.key.Key;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.config.AutoFisherSubConfig;
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

        register("habk.enabled", () -> swords().getHABKenabled().get());

        register("autoFisher.enabled", () -> fisher().getEnabled().get());
        register("autoFisher.allowEC", () -> fisher().getAllowEC().get());
        register("autoFisher.dropRods", () -> fisher().getAutoDropRods().get());
        register("autoFisher.dropBows", () -> fisher().getAutoDropBows().get());
        register("autoFisher.dropSaddles", () -> fisher().getAutoDropSaddle().get());
        register("autoFisher.dropFish", () -> fisher().getAutoDropFish().get());
        register("autoFisher.dropBooks", () -> fisher().getAutoDropBooks().get());
        register("autoFisher.dropLilypads", () -> fisher().getAutoDropLilypads().get());

        register("autoCrafter.finalActionV2", () -> Addon.settings().getAutoCrafterConfig().getFinalActionV2().get());
        register("autoLoot", () -> Addon.settings().getAutoLoot().get());
        register("cooldownNotifier", () -> Addon.settings().getChatConfig().getCooldownNotifier().get());
    }

    private static final Map<String, Supplier<Key[]>> KEY_SETTINGS = Map.of(
        "autoComp.hotkey", () -> Addon.settings().getAutoCrafterConfig().getAutoCompHotkey().get(),
        "autoDecomp.hotkey", () -> Addon.settings().getAutoCrafterConfig().getAutoDecompHotkey().get(),
        "vabk.hotkey", () -> swords().getVABKhotkey().get(),
        "infinityMiner.hotkey", () -> Addon.settings().getInfinityMiner().get(),
        "inventoryDrop.hotkey", () -> Addon.settings().getInventoryDrop().get(),
        "plotSwitch.next", () -> Addon.settings().getPlotSwitchSubConfig().getNextPlot().get(),
        "plotSwitch.previous", () -> Addon.settings().getPlotSwitchSubConfig().getPreviousPlot().get()
    );

    private FlowSettings() {}

    private static void register(String name, Supplier<Object> value) {
        SETTINGS.put(name, value);
    }

    /** The current value of a setting (without the "setting." prefix), empty if it does not exist. */
    public static String get(String name) {
        Supplier<Object> value = SETTINGS.get(name);
        return value == null ? "" : String.valueOf(value.get());
    }

    /** A hotkey setting, an empty array if it does not exist. */
    public static Key[] getKeys(String name) {
        Supplier<Key[]> keys = KEY_SETTINGS.get(name);
        return keys == null ? new Key[0] : keys.get();
    }

    private static SwordsSubConfig swords() {
        return Addon.settings().getSwordsSubConfig();
    }

    private static AutoFisherSubConfig fisher() {
        return Addon.settings().getAutoFisherSubConfig();
    }

    private static HopperSubConfig hopper() {
        return Addon.settings().getHopperSubConfig();
    }
}
