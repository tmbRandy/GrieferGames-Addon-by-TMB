package tmb.randy.tmbgriefergames.core.helper;

import java.text.NumberFormat;
import java.util.Locale;
import tmb.randy.tmbgriefergames.core.click.ClickManager;
import tmb.randy.tmbgriefergames.core.enums.QueueType;

/**
 * Version independent helper functions. The version specific {@code Helper} classes extend this
 * class, so every function here is also reachable through {@code Helper.xyz()}.
 */
public abstract class BaseHelper {
    public static final int INVENTORY_SIZE = 36;
    public static final int HOTBAR_SIZE = 9;

    private static final String[] ROMAN_NUMERALS = {"", "I", "II", "III", "IV", "V", "VI", "VII"};

    protected BaseHelper() {}

    // ---------------------------------------------------------------- Clicks

    public static ClickManager clicks() {
        return ClickManager.getSharedInstance();
    }

    public static void click(int slot) {
        clicks().click(QueueType.MEDIUM, slot);
    }

    public static void shiftClick(int slot) {
        clicks().shiftClick(QueueType.MEDIUM, slot);
    }

    public static void dropClick(int slot) {
        clicks().dropClick(slot);
    }

    public static boolean isClickQueueEmpty() {
        return clicks().isClickQueueEmpty(QueueType.MEDIUM);
    }

    // ---------------------------------------------------------------- Slots

    /**
     * Translates an index of the player's main inventory (0-8 hotbar, 9-35 storage) into the slot id of an
     * open container whose player inventory part starts at {@code firstPlayerSlot}.
     */
    public static int toContainerSlot(int inventoryIndex, int firstPlayerSlot) {
        return firstPlayerSlot + (inventoryIndex < HOTBAR_SIZE ? inventoryIndex + 27 : inventoryIndex - HOTBAR_SIZE);
    }

    // ---------------------------------------------------------------- Parsing

    /** Returns the number that follows {@code prefix} in {@code text}, or -1 if there is none. */
    public static int parseNumberAfter(String text, String prefix) {
        if (text == null || !text.startsWith(prefix))
            return -1;

        try {
            return Integer.parseInt(text.substring(prefix.length()).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Parses GrieferGames coordinates like {@code "12.0;64.0;-3.0"} into {x, y, z}, or null if invalid. */
    public static int[] parseCoordinates(String text) {
        String[] parts = text.replace("\"", "").replace(".0", "").trim().split(";");
        if (parts.length != 3)
            return null;

        try {
            return new int[] {Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim())};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Returns the compression step encoded as roman numeral at the end of a display name ("Stein III" -> 3). */
    public static int getRomanSuffixLevel(String displayName) {
        for (int level = ROMAN_NUMERALS.length - 1; level >= 1; level--) {
            if (displayName.endsWith(" " + ROMAN_NUMERALS[level]))
                return level;
        }
        return 0;
    }

    public static String getRomanSuffix(int level) {
        return " " + ROMAN_NUMERALS[level];
    }

    // ---------------------------------------------------------------- Formatting

    /** Formats a money difference for the action bar, e.g. "§a§l+1.234,50$". */
    public static String formatMoneyDifference(double difference) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.GERMANY);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return (difference < 0 ? "§c§l" : "§a§l+") + format.format(difference) + "$";
    }
}
