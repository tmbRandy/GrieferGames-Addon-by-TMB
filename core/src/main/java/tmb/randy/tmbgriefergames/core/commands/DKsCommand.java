package tmb.randy.tmbgriefergames.core.commands;

import java.util.ArrayList;
import java.util.List;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.helper.Commander;

/** /dks &lt;amount&gt;: shows how many items of which compression level fill the given number of double chests. */
public class DKsCommand extends DescribedCommand {
    private static final int ITEMS_PER_DK = 64 * 9 * 6;
    private static final int MAX_DKS = 99999;
    /** Symbols of the compression levels 1 to 7. */
    private static final String[] LEVEL_SYMBOLS = {"Ⅰ", "Ⅱ", "Ⅲ", "Ⅳ", "Ⅴ", "Ⅵ", "Ⅶ"};
    private static final String LEVEL_STYLE = "§6";
    private static final String SEPARATOR_STYLE = "§7";
    private static final String SEPARATOR = " • ";

    public DKsCommand() {
        super("dks", "dk");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        if (!Addon.isGG())
            return false;

        if (!Commander.canSend() || arguments.length != 1)
            return true;

        int numberOfDKs = Integer.parseInt(arguments[0]);
        if (numberOfDKs > MAX_DKS)
            return true;

        // Fill the highest levels first, the lower levels get what is left
        double restItems = numberOfDKs * ITEMS_PER_DK;
        List<String> parts = new ArrayList<>();
        for (int level = LEVEL_SYMBOLS.length; level >= 1; level--) {
            double itemsForLevel = Math.pow(9, level);
            int amount = (int) (restItems / itemsForLevel);
            restItems -= itemsForLevel * amount;

            if (amount > 0)
                parts.add(LEVEL_STYLE + amount + " * " + LEVEL_SYMBOLS[level - 1] + SEPARATOR_STYLE);
        }

        if (restItems > 0)
            parts.add(LEVEL_STYLE + restItems + " Items" + SEPARATOR_STYLE);

        Addon.displayNotification("§e" + numberOfDKs + " DKs" + SEPARATOR_STYLE + " = " + String.join(SEPARATOR, parts));
        return true;
    }
}
