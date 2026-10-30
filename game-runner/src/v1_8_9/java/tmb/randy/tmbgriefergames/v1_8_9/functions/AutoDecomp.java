package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.Phase;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.helper.Commander;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

public class AutoDecomp extends ActiveFunction {
    private static final int TICKS_BETWEEN_STEPS = 5;
    private static final int MAX_LEVEL = 7;
    // Slots of the crafting table container
    private static final int RESULT = 0;
    private static final int FIRST_GRID_SLOT = 1;
    private static final int CENTER_GRID_SLOT = 5;
    private static final int FIRST_INVENTORY_SLOT = 10;

    private ItemStack compItem;
    private int counter;

    public AutoDecomp() {
        super(Functions.DECOMP.name());
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (event.state() == State.PRESS && event.key() == Key.ESCAPE)
            stop();
        else if (Addon.allKeysPressedAndGuiClosed(Addon.settings().getAutoCrafterConfig().getAutoDecompHotkey().get()))
            start();
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if (event.phase() == Phase.PRE && isEnabled() && ++counter >= TICKS_BETWEEN_STEPS) {
            counter = 0;
            decomp();
        }
    }

    @Override
    public boolean stop() {
        if (!super.stop())
            return false;

        compItem = null;
        return true;
    }

    private void decomp() {
        if (!Helper.isCraftingScreen()) {
            Commander.queue("/craft");
            return;
        }

        if (!Helper.isClickQueueEmpty())
            return;

        if (compItem == null) {
            ItemStack firstItem = Helper.getInventoryStack(0);
            if (!Helper.isEmpty(firstItem))
                compItem = firstItem.copy();
            return;
        }

        if (Helper.getContainerStack(RESULT) != null) {
            Helper.shiftClick(RESULT);
            return;
        }

        if (clearCraftingGrid())
            return;

        int[] stacksPerLevel = countStacksPerLevel();
        if (stacksPerLevel[0] > 0) {
            Helper.dropItems(stack -> Helper.isSameItem(stack, compItem) && !stack.hasDisplayName(), false);
            return;
        }

        for (int level = 1; level <= MAX_LEVEL; level++) {
            if (stacksPerLevel[level] > 0) {
                int slot = getSmallestStackOfLevel(level);
                if (slot >= FIRST_INVENTORY_SLOT) {
                    Helper.click(slot);
                    Helper.click(CENTER_GRID_SLOT);
                }
                return;
            }
        }
    }

    /** Moves everything out of the crafting grid, returns true if there was something to move. */
    private boolean clearCraftingGrid() {
        boolean clicked = false;
        for (int i = FIRST_GRID_SLOT; i < FIRST_INVENTORY_SLOT; i++) {
            if (Helper.getContainerStack(i) != null) {
                Helper.shiftClick(i);
                clicked = true;
            }
        }
        return clicked;
    }

    private int[] countStacksPerLevel() {
        int[] output = new int[MAX_LEVEL + 1];
        for (int i = 0; i < Helper.INVENTORY_SIZE; i++) {
            ItemStack stack = Helper.getInventoryStack(i);
            if (Helper.isSameItem(stack, compItem))
                output[stack.hasDisplayName() ? Helper.getRomanSuffixLevel(stack.getDisplayName()) : 0]++;
        }
        return output;
    }

    private int getSmallestStackOfLevel(int level) {
        String suffix = Helper.getRomanSuffix(level);
        int smallestSlot = 0;
        int smallestCount = Integer.MAX_VALUE;

        for (int i = FIRST_INVENTORY_SLOT; i < Helper.getContainerSize(); i++) {
            ItemStack stack = Helper.getContainerStack(i);
            if (!Helper.isSameItem(stack, compItem) || !stack.hasDisplayName() || !stack.getDisplayName().endsWith(suffix))
                continue;

            if (Helper.getCount(stack) < smallestCount) {
                smallestCount = Helper.getCount(stack);
                smallestSlot = i;
            }
        }
        return smallestSlot;
    }
}
