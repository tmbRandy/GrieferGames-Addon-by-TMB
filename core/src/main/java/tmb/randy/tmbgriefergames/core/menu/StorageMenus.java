package tmb.randy.tmbgriefergames.core.menu;

/** Storage like menus the ItemShifter and Eject functions work with. */
public final class StorageMenus {

    private StorageMenus() {}

    /** Plot storage ("Lager: ..."), emptied by /auswurf. */
    public static final class Storage {
        public static final String TITLE_PREFIX = "§0Lager: ";
        public static final int STORAGE_SLOTS = 25;

        private Storage() {}
    }

    /** Endless chest: the menu is named like the stored item, slot 11 hands out all items. */
    public static final class EndlessChest {
        public static final int TAKE_ALL = 11;

        private EndlessChest() {}
    }

    /** "Wähle deine Komprimierung": choose which compression level should be taken out. */
    public static final class CompressionSelection {
        public static final String TITLE = "§6Wähle deine Komprimierung";

        public static final int FIRST_OPTION = 10;
        public static final int LAST_OPTION = 16;

        private CompressionSelection() {}
    }

    public static final class SpecialItems {
        public static final String TITLE = "§6spezielle Items";

        private SpecialItems() {}
    }

    public static final class SpawnerStorage {
        public static final String TITLE = "§6Spawner - Lager";
        /** Numeric item ids of the menu decoration, which must not be moved. */
        public static final int[] MENU_ITEM_IDS = {160, 384, 347, 76, 54};

        private SpawnerStorage() {}
    }
}
