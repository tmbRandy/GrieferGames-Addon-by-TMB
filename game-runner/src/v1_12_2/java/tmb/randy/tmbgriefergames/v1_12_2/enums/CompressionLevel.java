package tmb.randy.tmbgriefergames.v1_12_2.enums;

import net.minecraft.item.ItemStack;

public enum CompressionLevel {
    NONE(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7);

    private final int level;

    CompressionLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static CompressionLevel fromItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound() || stack.getTagCompound() == null) {
            return NONE;
        }
        if (!stack.getTagCompound().hasKey("CompressionLevel")) {
            return NONE;
        }
        try {
            return CompressionLevel.valueOf(stack.getTagCompound().getString("CompressionLevel").toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}