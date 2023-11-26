package tmb.randy.tmbgriefergames.v1_12_2.util.click;

import net.minecraft.inventory.ClickType;

public record Click(int windowID, int slot, int data, ClickType action) {

    @Override
    public String toString() {
        return "window: " + windowID + " slot: " + slot;
    }
}
