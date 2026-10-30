package tmb.randy.tmbgriefergames.core.click;

/**
 * A single inventory click. The version specific {@link ClickManager} translates the
 * {@link Mode} into the click type of the running Minecraft version.
 */
public record Click(int windowId, int slot, Mode mode) {

    public enum Mode {
        /** Normal left click (pick up / put down). */
        PICKUP,
        /** Shift click (move the stack into the other inventory). */
        QUICK_MOVE
    }
}
