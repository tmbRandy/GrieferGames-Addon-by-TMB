package tmb.randy.tmbgriefergames.core.menu;

/** The settings menu that opens when a hopper is right clicked ("Trichter-Einstellungen"). */
public final class HopperMenu {
    public static final String TITLE = "§6Trichter-Einstellungen";

    public static final int STACK_SIZE = 10;
    public static final int MULTI_CONNECT = 15;
    public static final int CONNECT = 16;
    public static final int FILTER = 28;
    public static final int RADIUS_DECREASE = 30;
    public static final int RADIUS = 31;
    public static final int RADIUS_INCREASE = 32;
    /** Clicked by the ItemShifter "arrow up" shortcut before the radius is maxed out. */
    public static final int SHORTCUT_SLOT = 49;
    /** Player inventory slot whose item is used as filter item. */
    public static final int FILTER_SOURCE = 72;

    public static final int MAX_RADIUS_STEPS = 15;

    private HopperMenu() {}

    /** "Trichter-Mehrfach-Verbindungen": list of all multi connections of a hopper. */
    public static final class MultiConnections {
        public static final String TITLE = "§6Trichter-Mehrfach-Verbindungen";

        public static final int CONNECTION_SLOTS = 44;
        public static final int ADD_CONNECTION = 53;

        private MultiConnections() {}
    }
}
