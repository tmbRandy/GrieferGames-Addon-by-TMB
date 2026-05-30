package tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter;

import java.util.List;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.core.functions.AutoCrafterSimulator;

public class Simulator extends AutoCrafterSimulator<ItemStack> {

    public Simulator(List<Slot> inventorySlots, int size)
    {
        super(size);
        for (int i = 0; i < size; i++)
            setStack(i, inventorySlots.get(i).getStack());
    }
}
