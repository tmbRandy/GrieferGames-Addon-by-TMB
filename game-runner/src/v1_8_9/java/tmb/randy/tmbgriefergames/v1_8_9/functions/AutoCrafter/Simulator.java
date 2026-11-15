package tmb.randy.tmbgriefergames.v1_8_9.functions.AutoCrafter;

import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

/** Simulates the clicks queued by the AutoCrafter, so later clicks can be planned before earlier ones are sent. */
public class Simulator {
    private final ItemStack[] slots;
    private ItemStack held;

    public Simulator(int size) {
        slots = new ItemStack[size];
        for (int i = 0; i < size; i++)
            slots[i] = Helper.getContainerStack(i);
    }

    public ItemStack stackAt(int slot) {
        return slots[slot];
    }

    public void leftClick(int slot) {
        if (held != null) {
            if (slots[slot] == null) {
                slots[slot] = held;
                held = null;
            }
        } else if (slots[slot] != null) {
            held = slots[slot];
            slots[slot] = null;
        }
    }

    /** Moves the stack into the first free slot of the player inventory part. */
    public void shiftClick(int slot) {
        for (int i = slots.length - Helper.INVENTORY_SIZE; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = slots[slot];
                slots[slot] = null;
                return;
            }
        }
    }
}
