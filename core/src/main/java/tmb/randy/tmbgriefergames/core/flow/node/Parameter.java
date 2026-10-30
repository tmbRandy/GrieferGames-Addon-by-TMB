package tmb.randy.tmbgriefergames.core.flow.node;

/** An editable value of a block, e.g. the command of "send command" or the slot of "click slot". */
public record Parameter(String key, Kind kind, String defaultValue) {

    public enum Kind { TEXT, NUMBER, BOOLEAN }

    public static Parameter text(String key, String defaultValue) {
        return new Parameter(key, Kind.TEXT, defaultValue);
    }

    public static Parameter number(String key, int defaultValue) {
        return new Parameter(key, Kind.NUMBER, String.valueOf(defaultValue));
    }

    public static Parameter bool(String key, boolean defaultValue) {
        return new Parameter(key, Kind.BOOLEAN, String.valueOf(defaultValue));
    }
}
