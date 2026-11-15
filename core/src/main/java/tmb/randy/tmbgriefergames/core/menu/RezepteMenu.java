package tmb.randy.tmbgriefergames.core.menu;

import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.helper.BaseHelper;
import tmb.randy.tmbgriefergames.core.helper.Commander;

/**
 * The {@code /rezepte} menu ("Custom-Kategorien") and all of its sub menus.
 * All slot ids are container slot ids (chest part first, then the player inventory).
 */
public final class RezepteMenu {
    public static final String TITLE = "§6Custom-Kategorien";

    public static final int COMPRESSION = 11;
    public static final int MINECRAFT_RECIPES = 12;

    // Navigation bar that all paged sub menus share
    public static final int BACK = 45;
    public static final int PREVIOUS_PAGE = 48;
    public static final int PAGE_INDICATOR = 49;
    public static final int NEXT_PAGE = 50;
    public static final int CONFIRM = 52;

    /** All sub menus are double chests, so the player inventory starts behind the 54 chest slots. */
    public static final int FIRST_PLAYER_SLOT = 54;
    public static final int FIRST_HOTBAR_SLOT = BaseHelper.toContainerSlot(0, FIRST_PLAYER_SLOT);

    private RezepteMenu() {}

    public static void open() {
        Commander.queue(Const.Cmd.REZEPTE);
    }

    /** Opens the menu unless a command is still pending, so it is safe to call this every tick. */
    public static void requestOpen() {
        if (Commander.canSend())
            open();
    }

    /** Returns the slot that has to be clicked to get from page {@code current} to page {@code target}. */
    public static int navigate(int current, int target) {
        if (current < target)
            return NEXT_PAGE;
        if (current > target)
            return PREVIOUS_PAGE;
        return CONFIRM;
    }

    /** "Minecraft-Rezepte": overview of all vanilla recipes. Clicking an inventory item opens its recipe. */
    public static final class MinecraftRecipes {
        public static final String TITLE = "§6Minecraft-Rezepte";

        /** Gold ingots have two recipes (nuggets / block) which are both listed in this slot range. */
        public static final int FIRST_GOLD_INGOT_SLOT = 10;
        public static final int LAST_GOLD_INGOT_SLOT = 43;
        public static final int GOLD_INGOT_FALLBACK = 53;

        private MinecraftRecipes() {}
    }

    /** "Vanilla Bauanleitung": the crafting recipe of a single item. */
    public static final class VanillaRecipe {
        public static final String TITLE = "§6Vanilla Bauanleitung";
        public static final String VARIANT_PREFIX = "§6Variante ";

        public static final int[] INGREDIENTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        public static final int RESULT = 25;

        private VanillaRecipe() {}
    }

    /** "Item-Komprimierung-Bauanleitung": choose the item that should be compressed. */
    public static final class CompressionRecipes {
        public static final String TITLE = "§6Item-Komprimierung-Bauanleitung";

        private CompressionRecipes() {}
    }

    /** "Item-Komprimierung": the compression recipe of the chosen item, one page per compression level. */
    public static final class Compression {
        public static final String TITLE = "§6Item-Komprimierung";
        public static final String LEVEL_PREFIX = "§6Komprimierungsstufe ";

        private Compression() {}

        /** Reads the level from the name of the page indicator, -1 if it is not a level indicator. */
        public static int parseLevel(String pageIndicatorName) {
            return BaseHelper.parseNumberAfter(pageIndicatorName, LEVEL_PREFIX);
        }
    }

    /** True for the main menu and the vanilla recipe sub menus (not for the compression menus). */
    public static boolean isRecipeTitle(String title) {
        return TITLE.equals(title) || MinecraftRecipes.TITLE.equals(title) || VanillaRecipe.TITLE.equals(title);
    }
}
