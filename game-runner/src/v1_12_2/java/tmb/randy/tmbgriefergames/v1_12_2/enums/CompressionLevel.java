package tmb.randy.tmbgriefergames.v1_12_2.enums;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public enum CompressionLevel {
    UNCOMPRESSED,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN;

    public static CompressionLevel fromStack(ItemStack itemStack) {
        if (itemStack.isEmpty() || !itemStack.hasTagCompound())
            return UNCOMPRESSED;

        NBTTagCompound nbt = itemStack.getTagCompound();

        if (!nbt.hasKey("compressionLevel"))
            return UNCOMPRESSED;

        String level = nbt.getString("compressionLevel");

        return switch (level) {
            case "ONE" -> ONE;
            case "TWO" -> TWO;
            case "THREE" -> THREE;
            case "FOUR" -> FOUR;
            case "FIVE" -> FIVE;
            case "SIX" -> SIX;
            case "SEVEN" -> SEVEN;
            default -> UNCOMPRESSED;
        };
    }
}