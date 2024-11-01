package tmb.randy.tmbgriefergames.v1_8_9.util.AutoCrafter;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import java.util.List;

public class Simulator
{
    private final int size;
    private final ItemStack[] slots;
    private ItemStack held;

    public Simulator(List<Slot> inventorySlots, int size) {
        this.size = size;
        this.slots = new ItemStack[size];
        for (int i = 0; i < size; i++)
        {
            this.slots[i] = inventorySlots.get(i).getStack();
        }
    }

    public ItemStack stackAt(int slot)
    {
        return this.slots[slot];
    }

    public void leftClick(int slot) {
        if (this.held != null) {
            if (this.slots[slot] != null)
                return;

            this.slots[slot] = this.held;
            this.held = null;
        } else {
            if (this.slots[slot] == null)
                return;

            this.held = this.slots[slot];
            this.slots[slot] = null;
        }
    }

    public void shiftClick(int slot) {
        int destination = this.findFirstFreeSlot();
        if (destination == 0)
            return;

        this.slots[destination] = this.slots[slot];
        this.slots[slot] = null;
    }

    private int findFirstFreeSlot() {
        for (int i = this.size - 36; i < this.size; i++)
        {
            if (this.slots[i] == null)
                return i;
        }
        return 0;
    }
}