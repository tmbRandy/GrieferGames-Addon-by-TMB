package tmb.randy.tmbgriefergames.core.flow;

/** Something that happened in the game and can fire trigger blocks. {@code data} depends on the type. */
public record FlowEvent(Type type, String data) {

    public enum Type {
        /** data: the plain chat message */
        CHAT,
        /** data: the chest title */
        MENU_OPEN,
        /** data: the new CB */
        CB_CHANGE,
        /** data: the key name */
        KEY,
        FISH_BITE,
        INVENTORY_FULL,
        /** data: button and action, e.g. "RIGHT_RELEASE" or "LEFT_CLICK" */
        MOUSE,
        /** data: the message or command the player sent */
        CHAT_SENT,
        /** Fired every tick for running flows (timer triggers) */
        TICK
    }
}
