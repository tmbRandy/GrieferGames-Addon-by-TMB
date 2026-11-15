package tmb.randy.tmbgriefergames.v1_8_9.functions.AutoCrafter;

import java.util.HashMap;
import java.util.Map;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.click.Click;
import tmb.randy.tmbgriefergames.core.click.ClickManager;
import tmb.randy.tmbgriefergames.core.config.AutoCrafterSubConfig;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

/** Repeats a crafting recipe stored with SHIFT + ENTER in the crafting table. */
public class AutoCrafterV1 extends Function {
    // Slots of the crafting table container
    private static final int RESULT = 0;
    private static final int GRID_SIZE = 9;
    private static final int FIRST_INVENTORY_SLOT = 10;
    private static final int CONTAINER_SIZE = 46;

    private final ItemStack[] recipe = new ItemStack[GRID_SIZE];
    private ItemStack output;
    private Simulator simulator;
    private boolean endlessModeActive = false;

    public AutoCrafterV1() {
        super(Functions.CRAFTV1.name());
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (!Helper.isCraftingScreen() || event.key() != Key.ENTER || event.state() != State.PRESS)
            return;

        if (Key.L_SHIFT.isPressed()) {
            storeRecipe();
            Addon.displayNotification(Addon.translate("autoCrafter.recipeSaved"));
        } else if (config().getEndlessMode().get()) {
            setEndlessModeActive(!endlessModeActive);
        } else if (isQueueEmpty()) {
            craft();
        }
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if (endlessModeActive && isQueueEmpty())
            craft();
    }

    private void storeRecipe() {
        ItemStack result = Helper.getContainerStack(RESULT);
        if (result == null)
            return;

        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack stack = Helper.getContainerStack(i + 1);
            recipe[i] = stack == null ? null : stack.copy();
        }
        output = result.copy();
    }

    private void craft() {
        if (!Helper.isCraftingScreen() || output == null)
            return;

        depositHeldItem();
        simulator = new Simulator(CONTAINER_SIZE);
        dropCraftedItems();
        simulator = new Simulator(CONTAINER_SIZE);

        if (!hasAllMaterials()) {
            clearGrid();
            return;
        }

        for (int i = 0; i < GRID_SIZE; i++) {
            int gridSlot = i + 1;
            ItemStack current = simulator.stackAt(gridSlot);
            if (current != null) {
                if (recipe[i] != null && isRecipeItem(current, recipe[i]))
                    continue;
                shiftClick(gridSlot);
            }

            if (recipe[i] != null && !moveIngredientInto(gridSlot, recipe[i]))
                return;
        }

        shiftClick(RESULT);
    }

    /** Puts a stack the player might be holding on the cursor back into the inventory (or drops it). */
    private void depositHeldItem() {
        for (int i = FIRST_INVENTORY_SLOT; i < CONTAINER_SIZE; i++) {
            if (Helper.getContainerStack(i) == null) {
                sendDirect(i);
                return;
            }
        }

        for (int i = 1; i <= GRID_SIZE; i++) {
            if (Helper.getContainerStack(i) == null) {
                sendDirect(i);
                return;
            }
        }

        for (int i = FIRST_INVENTORY_SLOT; i < CONTAINER_SIZE; i++) {
            if (isIngredient(Helper.getContainerStack(i))) {
                sendDirect(i);
                sendDirect(ClickManager.OUTSIDE_SLOT);
                return;
            }
        }
    }

    private boolean moveIngredientInto(int gridSlot, ItemStack ingredient) {
        boolean onlyFullStacks = config().getOnlyFullStacks().get();

        for (int i = CONTAINER_SIZE - 1; i >= FIRST_INVENTORY_SLOT; i--) {
            ItemStack stack = simulator.stackAt(i);
            if (stack != null && isRecipeItem(stack, ingredient) && (Helper.isFullStack(stack) || !onlyFullStacks)) {
                click(i);
                click(gridSlot);
                return true;
            }
        }
        return false;
    }

    private boolean hasAllMaterials() {
        Map<String, Integer> needed = new HashMap<>();
        for (ItemStack ingredient : recipe) {
            if (ingredient != null)
                needed.merge(getKey(ingredient), 1, Integer::sum);
        }

        for (int i = 1; i < CONTAINER_SIZE && !needed.isEmpty(); i++) {
            ItemStack stack = simulator.stackAt(i);
            if (stack != null)
                needed.computeIfPresent(getKey(stack), (key, count) -> count > Helper.getCount(stack) ? count - Helper.getCount(stack) : null);
        }
        return needed.isEmpty();
    }

    private void clearGrid() {
        for (int i = 1; i <= GRID_SIZE; i++) {
            if (simulator.stackAt(i) != null)
                shiftClick(i);
        }
    }

    private void dropCraftedItems() {
        if (!config().getAutoDrop().get())
            return;

        for (int i = FIRST_INVENTORY_SLOT; i < CONTAINER_SIZE; i++) {
            ItemStack stack = simulator.stackAt(i);
            if (stack != null && stack.getItem() == output.getItem() && stack.getDisplayName().equals(output.getDisplayName())) {
                queue(i, Click.Mode.PICKUP);
                queue(ClickManager.OUTSIDE_SLOT, Click.Mode.PICKUP);
            }
        }
    }

    private boolean isIngredient(ItemStack stack) {
        for (ItemStack ingredient : recipe) {
            if (ingredient != null && stack != null && ingredient.getItem() == stack.getItem())
                return true;
        }
        return false;
    }

    private static boolean isRecipeItem(ItemStack stack, ItemStack ingredient) {
        return Helper.isSameItem(stack, ingredient) && stack.getDisplayName().equals(ingredient.getDisplayName());
    }

    private static String getKey(ItemStack stack) {
        return Helper.getRegistryName(stack) + ":" + stack.getMetadata();
    }

    private void click(int slot) {
        queue(slot, Click.Mode.PICKUP);
        simulator.leftClick(slot);
    }

    private void shiftClick(int slot) {
        queue(slot, Click.Mode.QUICK_MOVE);
        simulator.shiftClick(slot);
    }

    private void queue(int slot, Click.Mode mode) {
        Helper.clicks().addClick(getCraftingSpeed(), new Click(Helper.getWindowId(), slot, mode));
    }

    private static void sendDirect(int slot) {
        Helper.windowClick(new Click(Helper.getWindowId(), slot, Click.Mode.PICKUP));
    }

    private void setEndlessModeActive(boolean active) {
        endlessModeActive = active;
        Addon.displayNotification(Addon.translate(active ? "autoCrafter.active" : "autoCrafter.inactive"));
    }

    private boolean isQueueEmpty() {
        return Helper.clicks().isClickQueueEmpty(getCraftingSpeed());
    }

    private static QueueType getCraftingSpeed() {
        return config().getAutoCraftSpeed().get();
    }

    private static AutoCrafterSubConfig config() {
        return Addon.settings().getAutoCrafterConfig();
    }
}
