package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.click.Click;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.flow.FlowManager;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.menu.HopperMenu;
import tmb.randy.tmbgriefergames.core.menu.StorageMenus.CompressionSelection;
import tmb.randy.tmbgriefergames.core.menu.StorageMenus.EndlessChest;
import tmb.randy.tmbgriefergames.core.menu.StorageMenus.SpawnerStorage;
import tmb.randy.tmbgriefergames.core.menu.StorageMenus.SpecialItems;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

/**
 * Moves stacks between a chest and the inventory with the arrow keys (UP: inventory -> chest, DOWN: chest -> inventory).
 * If the player holds a stack on the cursor, only stacks of that item are moved.
 */
public class ItemShifter extends Function {
    private static final int SMALL_CHEST = 27;
    private static final int LARGE_CHEST = 54;
    private static final String AUTOCOMP_FLOW = "AutoComp";

    private ItemStack itemToMove;

    public ItemShifter() {
        super(Functions.ITEMSHIFTER.name());
    }

    public void stopShifting() {
        itemToMove = null;
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        String title = Helper.getChestTitle();
        boolean compHotkeyPressed = Key.ARROW_LEFT.isPressed() && Key.ARROW_UP.isPressed() && Key.ARROW_RIGHT.isPressed();
        if (compHotkeyPressed || title == null || !Helper.isChestScreen() || !isSlowQueueEmpty())
            return;

        if (Key.ARROW_UP.isPressed()) {
            if (title.equals(HopperMenu.TITLE))
                maxOutHopperRadius();
            else
                shiftIntoChest();
        } else if (Key.ARROW_DOWN.isPressed() && !FlowManager.get().isRunning(AUTOCOMP_FLOW)) {
            shiftOutOfChest(title);
        }
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (event.state() == State.PRESS && Addon.allKeysPressed(Addon.settings().getInventoryDrop().get())
            && Helper.isInventoryScreen() && Helper.isClickQueueEmpty()) {
            Helper.dropItems(stack -> true, false);
        }
    }

    private void maxOutHopperRadius() {
        shiftClick(HopperMenu.SHORTCUT_SLOT);
        for (int i = 0; i < HopperMenu.MAX_RADIUS_STEPS; i++)
            shiftClick(HopperMenu.RADIUS_INCREASE);
    }

    private void shiftIntoChest() {
        int chestSize = Helper.getChestSize();
        if (chestSize == SMALL_CHEST || chestSize == LARGE_CHEST)
            shiftStacks(chestSize, Helper.getContainerSize() - 1, 0, chestSize - 1);
    }

    private void shiftOutOfChest(String title) {
        ItemStack firstItem = Helper.getInventoryStack(0);
        String firstItemName = Helper.isEmpty(firstItem) ? null : firstItem.getDisplayName();

        if (title.equals(CompressionSelection.TITLE)) {
            takeAvailableCompression();
        } else if (title.equals(firstItemName)) {
            if (!Helper.isInventoryFull())
                shiftClick(EndlessChest.TAKE_ALL);
        } else if (title.equals(SpecialItems.TITLE)) {
            if (!Helper.isInventoryFull()) {
                int slot = Helper.findContainerSlot(0, Helper.getChestSize() - 1, stack -> stack.hasDisplayName() && stack.getDisplayName().equals(firstItemName));
                if (slot >= 0)
                    shiftClick(slot);
            }
        } else {
            int chestSize = Helper.getChestSize();
            if (chestSize == SMALL_CHEST || chestSize == LARGE_CHEST)
                shiftStacks(0, chestSize - 1, chestSize, Helper.getContainerSize() - 1);
        }
    }

    /** Takes all available items of the highest compression level that has any. */
    private void takeAvailableCompression() {
        for (int slot = CompressionSelection.LAST_OPTION; slot >= CompressionSelection.FIRST_OPTION; slot--) {
            int available = getAvailableCount(Helper.getContainerStack(slot));
            if (available > 0) {
                for (int i = 0; i < available; i++)
                    shiftClick(slot);
                return;
            }
        }
    }

    private static int getAvailableCount(ItemStack stack) {
        for (String line : Helper.getLore(stack)) {
            if (line.startsWith(Const.Lore.VERFUEGBAR_COLOR) && line.endsWith(Const.Lore.VERFUEGBAR_SUFFIX)) {
                String number = line.substring(Const.Lore.VERFUEGBAR_COLOR.length(), line.length() - Const.Lore.VERFUEGBAR_SUFFIX.length());
                return Integer.parseInt(number.replace(".", ""));
            }
        }
        return 0;
    }

    private void shiftStacks(int fromMin, int fromMax, int destMin, int destMax) {
        depositHeldStack(destMin, destMax, fromMin, fromMax);

        int freeSlots = countFreeSlots(destMin, destMax);
        boolean spawnerStorage = Helper.isChestOpen(SpawnerStorage.TITLE);

        for (int slot = fromMin; slot <= fromMax && freeSlots > 0; slot++) {
            ItemStack stack = Helper.getContainerStack(slot);
            if (stack == null || (spawnerStorage && ArrayUtils.contains(SpawnerStorage.MENU_ITEM_IDS, Item.getIdFromItem(stack.getItem()))))
                continue;

            if (itemToMove == null || (stack.getItem() == itemToMove.getItem() && stack.getDisplayName().equals(itemToMove.getDisplayName()))) {
                shiftClick(slot);
                freeSlots--;
            }
        }

        itemToMove = null;
    }

    /** Puts a stack held on the cursor into the first free slot and remembers it as the only item to move. */
    private void depositHeldStack(int destMin, int destMax, int fromMin, int fromMax) {
        int freeSlot = findFreeSlot(destMin, destMax);
        if (freeSlot < 0)
            freeSlot = findFreeSlot(fromMin, fromMax);
        if (freeSlot < 0)
            return;

        Helper.windowClick(new Click(Helper.getWindowId(), freeSlot, Click.Mode.PICKUP));
        itemToMove = Helper.getContainerStack(freeSlot);
    }

    private static int findFreeSlot(int from, int to) {
        for (int slot = from; slot <= to; slot++) {
            if (Helper.getContainerStack(slot) == null)
                return slot;
        }
        return -1;
    }

    private static int countFreeSlots(int from, int to) {
        int count = 0;
        for (int slot = from; slot <= to; slot++) {
            if (Helper.getContainerStack(slot) == null)
                count++;
        }
        return count;
    }

    private static void shiftClick(int slot) {
        Helper.clicks().shiftClick(QueueType.SLOW, slot);
    }

    private static boolean isSlowQueueEmpty() {
        return Helper.clicks().isClickQueueEmpty(QueueType.SLOW);
    }
}
