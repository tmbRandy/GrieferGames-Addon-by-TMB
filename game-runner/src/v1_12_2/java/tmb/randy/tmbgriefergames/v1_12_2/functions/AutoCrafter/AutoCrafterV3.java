package tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.config.AutoCrafterSubConfig;
import tmb.randy.tmbgriefergames.core.enums.AutoCrafterNewFinalAction;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.menu.RezepteMenu;
import tmb.randy.tmbgriefergames.core.menu.RezepteMenu.Compression;
import tmb.randy.tmbgriefergames.core.menu.RezepteMenu.CompressionRecipes;
import tmb.randy.tmbgriefergames.core.menu.RezepteMenu.MinecraftRecipes;
import tmb.randy.tmbgriefergames.core.menu.RezepteMenu.VanillaRecipe;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

/**
 * Fully automatic crafter: the player chooses a recipe and one source chest per material, then the function
 * takes the materials out of the chests, crafts via /rezepte and finally compresses or drops the result.
 */
public class AutoCrafterV3 extends ActiveFunction {
    private enum CompressionStep { IDLE, OPEN, COMPRESSING, FINISHED }

    /** Materials whose metadata does not matter for the given result (e.g. any planks for a piston). */
    private static final Map<String, Set<String>> META_FREE_MATERIALS = Map.ofEntries(
        Map.entry("minecraft:piston", Set.of("minecraft:planks")),
        Map.entry("minecraft:boat", Set.of("minecraft:planks")),
        Map.entry("minecraft:bed", Set.of("minecraft:wool", "minecraft:planks")),
        Map.entry("minecraft:bookshelf", Set.of("minecraft:planks")),
        Map.entry("minecraft:tripwire_hook", Set.of("minecraft:planks")),
        Map.entry("minecraft:wooden_axe", Set.of("minecraft:planks")),
        Map.entry("minecraft:wooden_pickaxe", Set.of("minecraft:planks")),
        Map.entry("minecraft:wooden_sword", Set.of("minecraft:planks")),
        Map.entry("minecraft:wooden_shovel", Set.of("minecraft:planks")),
        Map.entry("minecraft:wooden_hoe", Set.of("minecraft:planks")),
        Map.entry("minecraft:wooden_pressure_plate", Set.of("minecraft:planks")),
        Map.entry("minecraft:trapdoor", Set.of("minecraft:planks")),
        Map.entry("minecraft:wooden_button", Set.of("minecraft:planks")),
        Map.entry("minecraft:noteblock", Set.of("minecraft:planks")),
        Map.entry("minecraft:jukebox", Set.of("minecraft:planks")),
        Map.entry("minecraft:sign", Set.of("minecraft:planks")),
        Map.entry("minecraft:bowl", Set.of("minecraft:planks")),
        Map.entry("minecraft:stick", Set.of("minecraft:planks")),
        Map.entry("minecraft:daylight_detector", Set.of("minecraft:wooden_slab")),
        Map.entry("minecraft:chest", Set.of("minecraft:planks")),
        Map.entry("minecraft:crafting_table", Set.of("minecraft:planks"))
    );

    private static final int MAX_COMPRESSION_LEVEL = 6;
    private static final int MAX_EMPTY_CHEST_ATTEMPTS = 3;

    private ItemStack craftItem;
    private final Map<String, Integer> recipe = new HashMap<>();
    private final Map<String, BlockPos> sourceChests = new HashMap<>();
    private final Map<String, Integer> maxStackSizes = new HashMap<>();
    private int maxRecipeCount = 0;
    private CompressionStep compressionStep = CompressionStep.IDLE;
    private int compressionLevel;
    private boolean displayedSelectMessage;
    private int variantPage = -1;
    private int tickCounter = 0;

    private BlockPos lastEmptyChest = null;
    private int emptyChestCounter = 0;

    public AutoCrafterV3() {
        super(Functions.CRAFTV3.name());
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (isEnabled() && event.state() == State.PRESS && event.key() == Key.ESCAPE)
            stop();
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if (!isEnabled())
            return;

        String message = event.chatMessage().getPlainText();
        if (Const.Chat.CHEST_IN_USE.equals(message))
            event.setCancelled(true);
        else if (Const.Chat.NO_VANILLA_RECIPE.equals(message))
            stop();
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if (!isEnabled() || !handleDelay())
            return;

        StuckProtection.tick(Helper.getContainer());

        if (craftItem == null)
            selectRecipe();
        else if (allSourceChestsScanned())
            craft();
        else
            scanSourceChest();
    }

    @Override
    public boolean stop() {
        if (super.stop())
            resetState();
        return true;
    }

    private boolean handleDelay() {
        int delay = config().getDelay().get();
        if (delay > 0 && tickCounter++ < delay)
            return false;

        tickCounter = 0;
        return true;
    }

    // ---------------------------------------------------------------- Recipe selection

    private void selectRecipe() {
        ItemStack firstItem = Helper.getInventoryStack(0);
        if (Helper.isEmpty(firstItem)) {
            Addon.displayNotification(Addon.translate("autoCrafter.noItemFound"));
            stop();
            return;
        }

        String title = Helper.getChestTitle();
        if (title == null) {
            RezepteMenu.requestOpen();
            return;
        }

        if (!Helper.isClickQueueEmpty())
            return;

        switch (title) {
            case RezepteMenu.TITLE -> Helper.shiftClick(RezepteMenu.MINECRAFT_RECIPES);
            case MinecraftRecipes.TITLE -> {
                if (Helper.isItem(firstItem, Const.Id.GOLD_INGOT))
                    clickGoldIngotRecipe();
                else
                    Helper.shiftClick(RezepteMenu.FIRST_HOTBAR_SLOT);
            }
            case VanillaRecipe.TITLE -> saveRecipe(firstItem);
        }
    }

    private void saveRecipe(ItemStack firstItem) {
        ItemStack result = Helper.getContainerStack(VanillaRecipe.RESULT);
        if (!areItemStacksEqual(result, firstItem))
            return;

        ItemStack variantIndicator = Helper.getContainerStack(RezepteMenu.PAGE_INDICATOR);
        if (Helper.isItem(variantIndicator, Const.Id.SKULL)) {
            if (!displayedSelectMessage) {
                Addon.displayNotification(Addon.translate("autoCrafter.chooseVariant"));
                displayedSelectMessage = true;
            }

            if (!Key.ENTER.isPressed())
                return;

            variantPage = Helper.parseNumberAfter(variantIndicator.getDisplayName(), VanillaRecipe.VARIANT_PREFIX);
        }

        craftItem = result.copy();
        for (int slot : VanillaRecipe.INGREDIENTS) {
            ItemStack stack = Helper.getContainerStack(slot);
            if (stack != null && !Helper.isItem(stack, Const.Id.STAINED_GLASS_PANE))
                recipe.merge(getItemKey(stack), 1, Integer::sum);
        }

        Helper.closeScreen();
        Addon.displayNotification(Addon.translate("autoCrafter.recipeSavedV3"));
    }

    private void scanSourceChest() {
        String title = Helper.getChestTitle();
        if (title == null)
            return;

        if (RezepteMenu.isRecipeTitle(title)) {
            Helper.closeScreen();
            return;
        }

        ItemStack chestItem = getUniformChestItem();
        if (chestItem == null) {
            Addon.displayNotification(Addon.translate("autoCrafter.mixedChest"));
            Helper.closeScreen();
            return;
        }

        BlockPos chestPos = Helper.getBlockPosLookingAt();
        if (chestPos == null)
            return;

        String itemKey = getItemKey(chestItem);
        sourceChests.put(itemKey, chestPos);
        maxStackSizes.put(itemKey, chestItem.getMaxStackSize());
        Helper.closeScreen();

        if (allSourceChestsScanned()) {
            Addon.displayNotification(Addon.translate("autoCrafter.startedCrafting"));
            maxRecipeCount = getMaxRecipeCount();
        } else {
            Addon.displayNotification(Addon.translate("autoCrafter.setChestForMaterial", chestItem.getDisplayName()));
        }
    }

    // ---------------------------------------------------------------- Crafting

    private void craft() {
        AutoCrafterNewFinalAction finalAction = config().getFinalActionV3().get();

        if (countInventoryStacks(getItemKey(craftItem)) <= 1 || (finalAction == AutoCrafterNewFinalAction.COMP && compressionStep == CompressionStep.FINISHED)) {
            collectAndCraft();
        } else {
            switch (finalAction) {
                case DROP -> dropCraftedItems();
                case COMP -> compressCraftedItems();
            }
        }
    }

    private void collectAndCraft() {
        if (Helper.isInventoryScreen())
            Helper.closeScreen();

        String nextItem = getNextItemToTake();
        String title = Helper.getChestTitle();

        if (!Helper.isContainerOpen())
            walkToSourceChest(nextItem);
        else if (title != null)
            handleOpenChest(title, nextItem);
    }

    private void walkToSourceChest(String nextItem) {
        if (nextItem == null) {
            RezepteMenu.requestOpen();
            return;
        }

        BlockPos neededChest = sourceChests.get(nextItem);
        if (neededChest == null) {
            Addon.displayNotification(Addon.translate("autoCrafter.noSourceFound"));
            return;
        }

        BlockPos lookingAt = Helper.getBlockPosLookingAt();
        if (lookingAt == null)
            return;

        if (!lookingAt.equals(neededChest))
            Helper.lookAtBlockPos(neededChest);
        else if (!Helper.getPlayer().isSneaking())
            Helper.rightClickBlockLookingAt();
    }

    private void handleOpenChest(String title, String nextItem) {
        if (!Helper.isClickQueueEmpty())
            return;

        // Materials are still missing, so every recipe menu is closed to walk to the next source chest
        boolean recipeMenu = RezepteMenu.isRecipeTitle(title);
        if (recipeMenu && nextItem != null) {
            Helper.closeScreen();
            return;
        }

        switch (title) {
            case RezepteMenu.TITLE -> Helper.shiftClick(RezepteMenu.MINECRAFT_RECIPES);
            case MinecraftRecipes.TITLE -> openCraftItemRecipe();
            case VanillaRecipe.TITLE -> craftRecipe();
            default -> takeMaterials();
        }
    }

    private void openCraftItemRecipe() {
        if (Helper.isItem(craftItem, Const.Id.GOLD_INGOT)) {
            clickGoldIngotRecipe();
            return;
        }

        String craftKey = getItemKey(craftItem);
        int slot = Helper.findInventorySlot(stack -> craftKey.equals(getItemKey(stack)));
        if (slot >= 0)
            Helper.shiftClick(Helper.toContainerSlot(slot, RezepteMenu.FIRST_PLAYER_SLOT));
    }

    private void craftRecipe() {
        int slot = RezepteMenu.CONFIRM;

        if (variantPage != -1) {
            ItemStack variantIndicator = Helper.getContainerStack(RezepteMenu.PAGE_INDICATOR);
            if (variantIndicator == null)
                return;
            slot = RezepteMenu.navigate(Helper.parseNumberAfter(variantIndicator.getDisplayName(), VanillaRecipe.VARIANT_PREFIX), variantPage);
        }

        Helper.shiftClick(slot);
        if (slot == RezepteMenu.CONFIRM)
            compressionStep = CompressionStep.IDLE;
    }

    private void takeMaterials() {
        if (isEmptyChestDetected()) {
            Helper.closeScreen();
            return;
        }

        ItemStack chestItem = getUniformChestItem();
        String itemKey = chestItem == null ? null : getItemKey(chestItem);
        if (itemKey == null || !recipe.containsKey(itemKey)) {
            Helper.closeScreen();
            return;
        }

        int neededStacks = getTargetStacks(itemKey) - countInventoryStacks(itemKey);
        for (int i = 0; i < Helper.getChestSize() && neededStacks > 0; i++) {
            ItemStack stack = Helper.getContainerStack(i);
            if (itemKey.equals(getItemKey(stack)) && Helper.isFullStack(stack)) {
                Helper.shiftClick(i);
                neededStacks--;
            }
        }

        if (Helper.isClickQueueEmpty())
            Helper.closeScreen();
    }

    // ---------------------------------------------------------------- Final actions

    private void dropCraftedItems() {
        if (Helper.getChestTitle() != null) {
            Helper.closeScreen();
        } else if (!Helper.isInventoryScreen()) {
            Helper.openInventory();
        } else if (Helper.isClickQueueEmpty()) {
            String craftKey = getItemKey(craftItem);
            Helper.dropItems(stack -> craftKey.equals(getItemKey(stack)), true);
        }
    }

    private void compressCraftedItems() {
        if (compressionStep == CompressionStep.IDLE) {
            Helper.closeScreen();
            compressionStep = CompressionStep.OPEN;
        }

        if (compressionStep == CompressionStep.OPEN)
            openCompression();
        else
            compress();
    }

    private void openCompression() {
        String title = Helper.getChestTitle();
        if (title == null) {
            RezepteMenu.requestOpen();
            return;
        }

        switch (title) {
            case RezepteMenu.TITLE -> Helper.shiftClick(RezepteMenu.COMPRESSION);
            case CompressionRecipes.TITLE -> {
                String craftKey = getItemKey(craftItem);
                int slot = Helper.findLowestCompressionSlot(stack -> craftKey.equals(getItemKey(stack)));
                Helper.shiftClick(slot >= 0 ? Helper.toContainerSlot(slot, RezepteMenu.FIRST_PLAYER_SLOT) : RezepteMenu.FIRST_HOTBAR_SLOT);
            }
            case Compression.TITLE -> {
                compressionLevel = 1;
                compressionStep = CompressionStep.COMPRESSING;
            }
            case VanillaRecipe.TITLE -> Helper.closeScreen();
        }
    }

    private void compress() {
        if (!Helper.isClickQueueEmpty() || Helper.getChestTitle() == null)
            return;

        ItemStack indicator = Helper.getContainerStack(RezepteMenu.PAGE_INDICATOR);
        int level = indicator == null ? -1 : Compression.parseLevel(indicator.getDisplayName());
        if (level < 0)
            return;

        if (compressionStep == CompressionStep.FINISHED) {
            Helper.closeScreen();
            return;
        }

        int slot = RezepteMenu.navigate(level, compressionLevel);
        Helper.shiftClick(slot);

        if (slot == RezepteMenu.CONFIRM && ++compressionLevel > MAX_COMPRESSION_LEVEL)
            compressionStep = CompressionStep.FINISHED;
    }

    // ---------------------------------------------------------------- Helpers

    private void clickGoldIngotRecipe() {
        int wantedCount = config().getGoldBlockToIngot().get() ? 9 : 1;
        int slot = Helper.findContainerSlot(MinecraftRecipes.FIRST_GOLD_INGOT_SLOT, MinecraftRecipes.LAST_GOLD_INGOT_SLOT,
            stack -> Helper.isItem(stack, Const.Id.GOLD_INGOT) && Helper.getCount(stack) == wantedCount);
        Helper.shiftClick(slot > 0 ? slot : MinecraftRecipes.GOLD_INGOT_FALLBACK);
    }

    private void resetState() {
        displayedSelectMessage = false;
        variantPage = -1;
        craftItem = null;
        recipe.clear();
        sourceChests.clear();
        maxStackSizes.clear();
        maxRecipeCount = 0;
        compressionStep = CompressionStep.IDLE;
        lastEmptyChest = null;
        emptyChestCounter = 0;
        StuckProtection.reset();
    }

    /** Highest number of crafts whose materials still fit into the inventory. */
    private int getMaxRecipeCount() {
        int reservedSlots = config().getFinalActionV3().get() == AutoCrafterNewFinalAction.COMP ? 10 : 2;
        int targetSlots = Helper.INVENTORY_SIZE - reservedSlots;
        int low = 1, high = targetSlots * 64, result = 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            if (getSlotsNeededForCrafts(mid) <= targetSlots) {
                result = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return result;
    }

    private int getSlotsNeededForCrafts(int crafts) {
        int slots = 0;
        for (Map.Entry<String, Integer> entry : recipe.entrySet())
            slots += (int) Math.ceil((double) crafts * entry.getValue() / maxStackSizes.getOrDefault(entry.getKey(), 64));
        return slots;
    }

    private int getTargetStacks(String itemKey) {
        return (int) Math.ceil((double) maxRecipeCount * recipe.get(itemKey) / maxStackSizes.getOrDefault(itemKey, 64));
    }

    private String getNextItemToTake() {
        for (String itemKey : recipe.keySet()) {
            if (countInventoryStacks(itemKey) < getTargetStacks(itemKey))
                return itemKey;
        }
        return null;
    }

    private boolean allSourceChestsScanned() {
        return sourceChests.keySet().containsAll(recipe.keySet());
    }

    /** The item of the open chest if all its stacks are the same item, otherwise null. */
    private ItemStack getUniformChestItem() {
        ItemStack first = null;
        for (int i = 0; i < Helper.getChestSize(); i++) {
            ItemStack stack = Helper.getContainerStack(i);
            if (stack == null)
                continue;

            if (first == null)
                first = stack;
            else if (!areItemStacksEqual(first, stack))
                return null;
        }
        return first;
    }

    private boolean areItemStacksEqual(ItemStack first, ItemStack second) {
        String key = getItemKey(first);
        return key != null && !Helper.isEmpty(second) && first.getItem() == second.getItem() && key.equals(getItemKey(second));
    }

    /** Identifies a material, ignoring the metadata where the recipe accepts every variant. */
    private String getItemKey(ItemStack stack) {
        if (Helper.isEmpty(stack))
            return null;

        String itemName = Helper.getRegistryName(stack);
        if (craftItem != null && META_FREE_MATERIALS.getOrDefault(Helper.getRegistryName(craftItem), Set.of()).contains(itemName))
            return itemName;

        return itemName + ":" + stack.getMetadata();
    }

    private int countInventoryStacks(String itemKey) {
        return Helper.countInventory(stack -> itemKey.equals(getItemKey(stack)));
    }

    /** Stops the crafter if the same source chest was found empty several times in a row. */
    private boolean isEmptyChestDetected() {
        for (int i = 0; i < Helper.getChestSize(); i++) {
            if (Helper.getContainerStack(i) != null) {
                lastEmptyChest = null;
                emptyChestCounter = 0;
                return false;
            }
        }

        BlockPos chestPos = Helper.getBlockPosLookingAt();
        if (chestPos != null) {
            emptyChestCounter = chestPos.equals(lastEmptyChest) ? emptyChestCounter + 1 : 1;
            lastEmptyChest = chestPos;

            if (emptyChestCounter >= MAX_EMPTY_CHEST_ATTEMPTS) {
                Addon.displayNotification(Addon.translate("autoCrafter.emptyChestDetected"));
                stop();
            }
        }
        return true;
    }

    private static AutoCrafterSubConfig config() {
        return Addon.settings().getAutoCrafterConfig();
    }
}
