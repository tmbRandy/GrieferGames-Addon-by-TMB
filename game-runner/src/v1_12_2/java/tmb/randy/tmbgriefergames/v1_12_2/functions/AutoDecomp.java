package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.AutoDecompMaster;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;
import tmb.randy.tmbgriefergames.v1_12_2.click.Click;
import tmb.randy.tmbgriefergames.v1_12_2.click.ClickManager;

public class AutoDecomp extends AutoDecompMaster {

    private Item compItem;
    private int compSubID;

    @Override
    protected void decomp() {
        if (Helper.getPlayer().openContainer == null
                || !(Minecraft.getMinecraft().currentScreen instanceof GuiCrafting)) {
            queueCraftCommand();
            return;
        }
        Container container = Helper.getPlayer().openContainer;
        if (!ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) return;

        if (compItem == null) {
            ItemStack slot0 = Helper.getFirstInventoryStack();
            if (!Helper.isStackEmpty(slot0)) {
                compItem = slot0.getItem();
                compSubID = slot0.getMetadata();
            }
            return;
        }

        if (container.getSlot(0).getHasStack()) {
            ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, 0, 0, ClickType.QUICK_MOVE));
            return;
        }
        if (doesCraftingFieldContainItems()) {
            for (int i = 1; i < 10; i++) {
                if (container.getSlot(i).getHasStack())
                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, i, 0, ClickType.QUICK_MOVE));
            }
            return;
        }

        int[] compSteps = getCompCountForItem();
        if (compSteps[0] > 0) {
            dropUncompressedItems();
            return;
        }
        for (int step = 1; step <= 7; step++) {
            if (compSteps[step] > 0) {
                int slot = getBestSlotForCompStep(step);
                if (slot >= 10) {
                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, slot, 0, ClickType.PICKUP));
                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, 5, 0, ClickType.PICKUP));
                }
                break;
            }
        }
    }

    @Override
    public boolean stop() {
        if (super.stop()) {
            compItem = null;
            compSubID = 0;
            return true;
        }
        return false;
    }

    private int[] getCompCountForItem() {
        int[] output = new int[8];
        for (ItemStack stack : Helper.getPlayer().inventory.mainInventory) {
            if (Helper.isStackEmpty(stack)) continue;
            if (!stack.getItem().equals(compItem) || stack.getMetadata() != compSubID) continue;
            int level = 0;
            if (stack.hasDisplayName()) {
                for (int step = 7; step >= 1; step--) {
                    if (stack.getDisplayName().endsWith(" " + ROMAN[step])) {
                        level = step;
                        break;
                    }
                }
            }
            output[level]++;
        }
        return output;
    }

    private boolean doesCraftingFieldContainItems() {
        if (!(Helper.getPlayer().openContainer instanceof ContainerWorkbench)) return false;
        for (int i = 1; i < 10; i++) {
            if (Helper.getPlayer().openContainer.getSlot(i).getHasStack()) return true;
        }
        return false;
    }

    private void dropUncompressedItems() {
        if (!(Helper.getPlayer().openContainer instanceof ContainerWorkbench)) return;
        for (int i = 10; i < 46; i++) {
            if (!Helper.getPlayer().openContainer.getSlot(i).getHasStack()) continue;
            ItemStack stack = Helper.getPlayer().openContainer.getSlot(i).getStack();
            if (stack.getItem().equals(compItem) && stack.getMetadata() == compSubID && !stack.hasDisplayName())
                ClickManager.getSharedInstance().dropClick(i);
        }
    }

    private int getBestSlotForCompStep(int step) {
        if (!(Helper.getPlayer().openContainer instanceof ContainerWorkbench)) return 0;
        Container container = Helper.getPlayer().openContainer;
        String suffix = " " + ROMAN[step];
        int lowestSlot = 0;
        int lowestCount = Integer.MAX_VALUE;

        for (int i = 10; i < container.inventorySlots.size(); i++) {
            if (!container.inventorySlots.get(i).getHasStack()) continue;
            ItemStack stack = container.inventorySlots.get(i).getStack();
            if (!stack.getItem().equals(compItem) || stack.getMetadata() != compSubID) continue;
            if (!stack.hasDisplayName() || !stack.getDisplayName().endsWith(suffix)) continue;
            if (Helper.getStackSize(stack) < lowestCount) {
                lowestCount = Helper.getStackSize(stack);
                lowestSlot = i;
            }
        }
        return lowestSlot;
    }
}
