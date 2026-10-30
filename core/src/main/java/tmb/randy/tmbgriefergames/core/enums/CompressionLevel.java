package tmb.randy.tmbgriefergames.core.enums;

public enum CompressionLevel {
    NONE, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN;

    public int getLevel() {
        return ordinal();
    }

    /** Parses the value of the GrieferGames compression NBT tag (e.g. "THREE"). */
    public static CompressionLevel fromTagValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}
