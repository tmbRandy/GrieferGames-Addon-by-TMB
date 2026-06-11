package tmb.randy.tmbgriefergames.v1_12_2.click;

import net.minecraft.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

public record Click(int windowID, int slot, int data, ClickType action) {

    @Override
    public @NotNull String toString() {
        return "window: " + windowID + " slot: " + slot;
    }
}