package tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter;

import static tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter.AutoCrafterV3.COMP_STATE.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import org.lwjgl.input.Keyboard;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.enums.AutoCrafterNewFinalAction;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.helper.Commander;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;
import tmb.randy.tmbgriefergames.v1_12_2.click.Click;
import tmb.randy.tmbgriefergames.v1_12_2.click.ClickManager;

public class AutoCrafterV3 extends ActiveFunction {
    enum COMP_STATE {
        IDLE, OPEN_COMP, COMP1, COMP2, COMP3, COMP4, COMP5, COMP6, FINISHED
    }

    private static final Map<String, Set<String>> META_FREE_MATERIALS = new HashMap<>() {{
        put("minecraft:piston", Set.of("minecraft:planks"));
        put("minecraft:boat", Set.of("minecraft:planks"));
        put("minecraft:bed", Set.of("minecraft:wool", "minecraft:planks"));
        put("minecraft:bookshelf", Set.of("minecraft:planks"));
        put("minecraft:tripwire_hook", Set.of("minecraft:planks"));
        put("minecraft:wooden_axe", Set.of("minecraft:planks"));
        put("minecraft:wooden_pickaxe", Set.of("minecraft:planks"));
        put("minecraft:wooden_sword", Set.of("minecraft:planks"));
        put("minecraft:wooden_shovel", Set.of("minecraft:planks"));
        put("minecraft:wooden_hoe", Set.of("minecraft:planks"));
        put("minecraft:wooden_pressure_plate", Set.of("minecraft:planks"));
        put("minecraft:trapdoor", Set.of("minecraft:planks"));
        put("minecraft:wooden_button", Set.of("minecraft:planks"));
        put("minecraft:noteblock", Set.of("minecraft:planks"));
        put("minecraft:jukebox", Set.of("minecraft:planks"));
        put("minecraft:sign", Set.of("minecraft:planks"));
        put("minecraft:bowl", Set.of("minecraft:planks"));
        put("minecraft:stick", Set.of("minecraft:planks"));
        put("minecraft:daylight_detector", Set.of("minecraft:wooden_slab"));
        put("minecraft:chest", Set.of("minecraft:planks"));
        put("minecraft:crafting_table", Set.of("minecraft:planks"));
    }};

    private static final int[] RECIPE_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final Set<String> RECIPE_MENU_NAMES = Set.of(
        Const.Menu.CUSTOM_KATEGORIEN, Const.Menu.MINECRAFT_REZEPTE, Const.Menu.VANILLA_BAUANLEITUNG);

    private ItemStack craftItem;
    private final Map<String, Integer> recipe = new HashMap<>();
    private final Map<String, BlockPos> sourceChests = new HashMap<>();
    private int maxRecipeCount = 0;
    private COMP_STATE compState = IDLE;
    private boolean displayedSelectMessage;
    private int variantPage = -1;
    private int tickCounter = 0;

    private BlockPos lastEmptyChest = null;
    private int emptyChestCounter = 0;
    private static final int MAX_EMPTY_CHEST_ATTEMPTS = 3;
    private boolean rezepteQueued = false;
    private final Map<String, Integer> maxStackSizes = new HashMap<>();

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
        if (!isEnabled()) return;

        String message = event.chatMessage().getPlainText();
        if (Const.Chat.CHEST_IN_USE.equals(message)) {
            event.setCancelled(true);
        } else if (Const.Chat.NO_VANILLA_RECIPE.equals(message)) {
            stop();
        }
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if (!isEnabled()) return;

        if (!handleDelay()) return;

        StuckProtection.tick(Helper.getPlayer().openContainer);

        if (craftItem == null) {
            handleRecipeSelection();
        } else if (allSourceChestsScanned()) {
            craft();
        } else {
            handleChestScanning();
        }
    }

    @Override
    public boolean stop() {
        if (super.stop()) {
            resetState();
        }
        return true;
    }

    private boolean handleDelay() {
        int delay = Addon.settings().getAutoCrafterConfig().getDelay().get();
        if (delay > 0) {
            if (tickCounter < delay) {
                tickCounter++;
                return false;
            }
            tickCounter = 0;
        }
        return true;
    }

    private void handleRecipeSelection() {
        ItemStack firstItem = Helper.getPlayer().inventory.mainInventory.getFirst();
        if (firstItem.isEmpty()) {
            Addon.displayNotification(Addon.translate("autoCrafter.noItemFound"));
            stop();
            return;
        }

        Container container = Helper.getPlayer().openContainer;
        if (!(container instanceof ContainerChest chest)) {
            if (!rezepteQueued) {
                Commander.queue(Const.Cmd.REZEPTE);
                rezepteQueued = true;
            }
            return;
        }
        rezepteQueued = false;

        if (!ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) return;

        IInventory chestInventory = chest.getLowerChestInventory();
        String chestName = chestInventory.getName();

        switch (chestName) {
            case Const.Menu.CUSTOM_KATEGORIEN -> click(chest.windowId, 12);
            case Const.Menu.MINECRAFT_REZEPTE -> handleMinecraftRecipes(chest, firstItem);
            case Const.Menu.VANILLA_BAUANLEITUNG -> handleVanillaBauanleitung(chest, chestInventory);
        }
    }

    private void handleMinecraftRecipes(ContainerChest chest, ItemStack firstItem) {
        if (firstItem.getItem().equals(Items.GOLD_INGOT)) {
            int slot = getSlotForGoldIngot(Addon.settings().getAutoCrafterConfig().getGoldBlockToIngot().get());
            click(chest.windowId, slot > 0 ? slot : 53);
        } else {
            click(chest.windowId, 81);
        }
    }

    private void handleVanillaBauanleitung(ContainerChest chest, IInventory chestInventory) {
        if (!areItemStacksEqual(chestInventory.getStackInSlot(25), Helper.getPlayer().inventory.mainInventory.getFirst()))
            return;

        ItemStack pageIndicator = chestInventory.getStackInSlot(49);
        if (!pageIndicator.isEmpty() && pageIndicator.getItem() == Items.SKULL) {
            if (!displayedSelectMessage) {
                Addon.displayNotification(Addon.translate("autoCrafter.chooseVariant"));
                displayedSelectMessage = true;
            }

            if (!Keyboard.isKeyDown(Key.ENTER.getId())) return;

            String variantPageNumberString = pageIndicator.getDisplayName().replace(Const.Menu.VARIANT_PREFIX, "");
            variantPage = Integer.parseInt(variantPageNumberString);
        }

        craftItem = chestInventory.getStackInSlot(25);
        extractRecipe(chestInventory);
        closeChest();
        Addon.displayNotification(Addon.translate("autoCrafter.recipeSavedV3"));
    }

    private void extractRecipe(IInventory chestInventory) {
        for (int recipeSlot : RECIPE_SLOTS) {
            ItemStack stack = chestInventory.getStackInSlot(recipeSlot);
            if (!isStainedGlassPane(stack) && !stack.isEmpty()) {
                recipe.merge(getItemKey(stack), 1, Integer::sum);
            }
        }
    }

    private void handleChestScanning() {
        Container container = Helper.getPlayer().openContainer;
        if (!(container instanceof ContainerChest chest)) return;

        IInventory chestInventory = chest.getLowerChestInventory();
        if (RECIPE_MENU_NAMES.contains(chestInventory.getName())) {
            closeChest();
            return;
        }

        ItemStack chestItemStack = allItemsAreEqual(chestInventory);

        if (chestItemStack == null) {
            Addon.displayNotification(Addon.translate("autoCrafter.mixedChest"));
            closeChest();
            return;
        }

        String itemKey = getItemKey(chestItemStack);
        RayTraceResult trace = Helper.getPlayer().rayTrace(5, 1.0F);

        if (trace != null && trace.typeOfHit == Type.BLOCK) {
            sourceChests.put(itemKey, trace.getBlockPos());
            maxStackSizes.put(itemKey, chestItemStack.getMaxStackSize());
            closeChest();

            if (allSourceChestsScanned()) {
                Addon.displayNotification(Addon.translate("autoCrafter.startedCrafting"));
                maxRecipeCount = maxRecipeCraftCount();
            } else {
                Addon.displayNotification(Addon.translate("autoCrafter.setChestForMaterial", chestItemStack.getDisplayName()));
            }
        }
    }

    private void craft() {
        int numberOfFinishedStacks = getNumberOfRecipeStacksInInventory();
        AutoCrafterNewFinalAction finalAction = Addon.settings().getAutoCrafterConfig().getFinalActionV3().get();

        if (numberOfFinishedStacks <= 1 || (finalAction == AutoCrafterNewFinalAction.COMP && compState == FINISHED)) {
            handleCrafting();
        } else {
            handleFinalAction(finalAction);
        }
    }

    private void handleCrafting() {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
            closeChest();
        }

        String nextItem = getNextItemToTake();

        if (!isContainerOpen()) {
            handleContainerClosed(nextItem);
        } else if (Helper.getPlayer().openContainer instanceof ContainerChest chest) {
            handleContainerOpen(chest, nextItem);
        }
    }

    private void handleContainerClosed(String nextItem) {
        if (nextItem == null) {
            if (!rezepteQueued) {
                Commander.queue(Const.Cmd.REZEPTE);
                rezepteQueued = true;
            }
            return;
        }

        BlockPos lookingAtBlock = Helper.getBlockPosLookingAt();
        BlockPos neededBlock = sourceChests.get(nextItem);

        if (neededBlock == null) {
            Addon.displayNotification(Addon.translate("autoCrafter.noSourceFound"));
            return;
        }

        if (lookingAtBlock != null) {
            if (lookingAtBlock.equals(neededBlock)) {
                if (!Helper.getPlayer().isSneaking()) {
                    RayTraceResult trace = Helper.getPlayer().rayTrace(5, 1.0F);
                    if (trace != null) {
                        Minecraft.getMinecraft().playerController.processRightClickBlock(
                            Helper.getPlayer(), Helper.getWorld(), lookingAtBlock, trace.sideHit, trace.hitVec, EnumHand.MAIN_HAND
                        );
                    }
                }
            } else {
                Helper.lookAtBlockPos(neededBlock);
            }
        }
    }

    private void handleContainerOpen(ContainerChest chest, String nextItem) {
        IInventory chestInventory = chest.getLowerChestInventory();
        String chestName = chestInventory.getName();

        switch (chestName) {
            case Const.Menu.CUSTOM_KATEGORIEN -> {
                rezepteQueued = false;
                if (nextItem == null && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                    click(chest.windowId, 12);
                } else if (nextItem != null) {
                    closeChest();
                }
            }
            case Const.Menu.MINECRAFT_REZEPTE -> handleMinecraftRecipesCrafting(chest, nextItem);
            case Const.Menu.VANILLA_BAUANLEITUNG -> handleVanillaBauanleitungCrafting(chest, nextItem);
            default -> handleMaterialChest(chest, chestInventory);
        }
    }

    private void handleMinecraftRecipesCrafting(ContainerChest chest, String nextItem) {
        if (nextItem != null) {
            closeChest();
            return;
        }

        if (!ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) return;

        if (craftItem.getItem().equals(Items.GOLD_INGOT)) {
            int slot = getSlotForGoldIngot(Addon.settings().getAutoCrafterConfig().getGoldBlockToIngot().get());
            click(chest.windowId, slot > 0 ? slot : 53);
        } else {
            int slot = getFirstSlotForCraftItem();
            if (slot > -1) {
                int translatedSlot = translateInventorySlotToContainerChestSlot(slot);
                click(chest.windowId, translatedSlot + chest.getLowerChestInventory().getSizeInventory());
            }
        }
    }

    private void handleVanillaBauanleitungCrafting(ContainerChest chest, String nextItem) {
        if (!ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) return;

        if (nextItem != null) {
            closeChest();
            return;
        }

        IInventory chestInventory = chest.getLowerChestInventory();
        if (variantPage != -1) {
            ItemStack pageIndicatorSkull = chestInventory.getStackInSlot(49);
            if (!pageIndicatorSkull.isEmpty()) {
                String pageIndicatorName = pageIndicatorSkull.getDisplayName().replace(Const.Menu.VARIANT_PREFIX, "");
                int currentPage = Integer.parseInt(pageIndicatorName);

                if (currentPage == variantPage) {
                    click(chest.windowId, 52);
                    compState = IDLE;
                } else if (currentPage < variantPage) {
                    click(chest.windowId, 50);
                } else {
                    click(chest.windowId, 48);
                }
            }
        } else {
            click(chest.windowId, 52);
            compState = IDLE;
        }
    }

    private void handleMaterialChest(ContainerChest chest, IInventory chestInventory) {
        if (!ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) return;

        if (checkEmptyChest(chestInventory)) {
            closeChest();
            return;
        }

        ItemStack itemStack = allItemsAreEqual(chestInventory);
        if (itemStack == null) {
            closeChest();
            return;
        }

        String itemKeyForChest = getItemKey(itemStack);
        int stacksInInventory = getCountOfItemKeyStacksInInventory(itemKeyForChest);
        int maxStackSize = maxStackSizes.getOrDefault(itemKeyForChest, itemStack.getMaxStackSize());
        int targetStacks = (int) Math.ceil((double) maxRecipeCount * recipe.get(itemKeyForChest) / maxStackSize);
        int neededStacks = targetStacks - stacksInInventory;

        int stacksTaken = 0;
        int size = chestInventory.getSizeInventory();

        for (int i = 0; i < size && stacksTaken < neededStacks; i++) {
            ItemStack currentStack = chestInventory.getStackInSlot(i);
            if (getItemKey(currentStack) != null &&
                getItemKey(currentStack).equals(itemKeyForChest) &&
                currentStack.getCount() == currentStack.getMaxStackSize()) {
                click(chest.windowId, i);
                stacksTaken++;
            }
        }

        if (ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
            closeChest();
        }
    }

    private void handleFinalAction(AutoCrafterNewFinalAction finalAction) {
        switch (finalAction) {
            case DROP -> handleDropAction();
            case COMP -> handleCompAction();
        }
    }

    private void handleDropAction() {
        Container container = Helper.getPlayer().openContainer;

        if (container instanceof ContainerChest) {
            closeChest();
        } else if (container instanceof ContainerPlayer inv &&
            Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
            dropCraftedItems(inv);
        } else {
            Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(Helper.getPlayer()));
        }
    }

    private void dropCraftedItems(ContainerPlayer inv) {
        if (!ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) return;

        boolean skippedFirst = false;
        String craftItemKey = getItemKey(craftItem);

        for (int i = 9; i < inv.inventorySlots.size(); i++) {
            Slot slot = inv.getSlot(i);
            if (slot.getHasStack()) {
                String key = getItemKey(slot.getStack());
                if (craftItemKey.equals(key)) {
                    if (skippedFirst) {
                        ClickManager.getSharedInstance().dropClick(i);
                    } else {
                        skippedFirst = true;
                    }
                }
            }
        }
    }

    private void handleCompAction() {
        if (compState == IDLE) {
            closeChest();
            compState = OPEN_COMP;
        }
        comp();
    }

    private void comp() {
        Container container = Helper.getPlayer().openContainer;

        switch (compState) {
            case OPEN_COMP -> handleCompOpen(container);
            case COMP1, COMP2, COMP3, COMP4, COMP5, COMP6, FINISHED -> handleCompSteps(container);
        }
    }

    private void handleCompOpen(Container container) {
        if (!(container instanceof ContainerChest chest)) {
            if (!rezepteQueued) {
                Commander.queue(Const.Cmd.REZEPTE);
                rezepteQueued = true;
            }
            return;
        }

        IInventory inv = chest.getLowerChestInventory();
        String name = inv.getName();

        switch (name) {
            case Const.Menu.CUSTOM_KATEGORIEN -> {
                rezepteQueued = false;
                click(11);
            }
            case Const.Menu.ITEM_KOMPRIMIERUNG_BAUANLEITUNG -> click(getBestCompSlot());
            case Const.Menu.ITEM_KOMPRIMIERUNG -> compState = COMP1;
            case Const.Menu.VANILLA_BAUANLEITUNG -> closeChest();
        }
    }

    private void handleCompSteps(Container container) {
        if (!ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) return;

        ItemStack stepIndicator = container.getSlot(49).getStack();
        if (!(container instanceof ContainerChest) || stepIndicator.isEmpty()) return;

        String name = stepIndicator.getDisplayName();
        if (!name.startsWith(Const.Comp.LEVEL_PREFIX)) return;

        int step = Integer.parseInt(name.replace(Const.Comp.LEVEL_PREFIX, ""));
        int expectedStep = compState.ordinal() - COMP_STATE.COMP1.ordinal() + 1;

        if (compState == COMP_STATE.FINISHED) {
            closeChest();
        } else if (compState == COMP1 && step > 1) {
            decreaseStep();
        } else if (step == expectedStep) {
            compClick();
            compState = COMP_STATE.values()[compState.ordinal() + 1];
        } else if (step < expectedStep) {
            increaseStep();
        } else {
            decreaseStep();
        }
    }

    private void resetState() {
        displayedSelectMessage = false;
        variantPage = -1;
        craftItem = null;
        recipe.clear();
        sourceChests.clear();
        maxRecipeCount = 0;
        compState = IDLE;
        lastEmptyChest = null;
        emptyChestCounter = 0;
        rezepteQueued = false;
        maxStackSizes.clear();
        StuckProtection.reset();
    }

    private int getSlotForGoldIngot(Boolean blockToIngot) {
        for (int i = 10; i < 44; i++) {
            ItemStack stack = Helper.getPlayer().openContainer.getSlot(i).getStack();
            if (!stack.isEmpty() && stack.getItem().equals(Items.GOLD_INGOT)) {
                if((blockToIngot && stack.getCount() == 9) || (!blockToIngot && stack.getCount() == 1))
                    return i;
            }
        }
        return -1;
    }

    private int maxRecipeCraftCount() {
        AutoCrafterNewFinalAction finalAction = Addon.settings().getAutoCrafterConfig().getFinalActionV3().get();
        int targetSlots = Helper.getPlayer().inventory.mainInventory.size() - (finalAction == AutoCrafterNewFinalAction.COMP ? 10 : 2);
        int lo = 1, hi = targetSlots * 64, result = 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            if (slotsNeededForCrafts(mid) <= targetSlots) {
                result = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return result;
    }

    private int slotsNeededForCrafts(int crafts) {
        return recipe.entrySet().stream()
            .mapToInt(e -> (int) Math.ceil((double) crafts * e.getValue() / maxStackSizes.getOrDefault(e.getKey(), 64)))
            .sum();
    }

    private ItemStack allItemsAreEqual(IInventory inventory) {
        ItemStack firstStack = null;
        int size = inventory.getSizeInventory();

        for (int i = 0; i < size; i++) {
            ItemStack currentStack = inventory.getStackInSlot(i);
            if (!currentStack.isEmpty()) {
                if (firstStack == null) {
                    firstStack = currentStack;
                } else if (!areItemStacksEqual(firstStack, currentStack)) {
                    return null;
                }
            }
        }
        return firstStack;
    }

    private boolean areItemStacksEqual(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() != stack2.getItem()) return false;
        String key1 = getItemKey(stack1);
        String key2 = getItemKey(stack2);
        return key1 != null && key1.equals(key2);
    }

    private String getItemKey(ItemStack stack) {
        if (stack == null || stack.getCount() == 0) return null;

        String itemName = Item.REGISTRY.getNameForObject(stack.getItem()).toString();

        if (craftItem != null) {
            String craftItemKey = Item.REGISTRY.getNameForObject(craftItem.getItem()).toString();
            if (META_FREE_MATERIALS.containsKey(craftItemKey) &&
                META_FREE_MATERIALS.get(craftItemKey).contains(itemName)) {
                return itemName;
            }
        }

        return itemName + ":" + stack.getMetadata();
    }

    public static boolean isStainedGlassPane(ItemStack stack) {
        return stack != null && !stack.isEmpty() &&
            stack.getItem() == Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE);
    }

    private boolean allSourceChestsScanned() {
        return recipe.keySet().stream().allMatch(sourceChests::containsKey);
    }

    private void closeChest() {
        Minecraft.getMinecraft().displayGuiScreen(null);
        Helper.getPlayer().closeScreen();
    }

    private int getCountOfItemKeyStacksInInventory(String itemKey) {
        return (int) Helper.getPlayer().inventory.mainInventory.stream()
            .filter(stack -> itemKey.equals(getItemKey(stack)))
            .count();
    }

    public static boolean isContainerOpen() {
        Container openContainer = Helper.getPlayer().openContainer;
        return openContainer != Helper.getPlayer().inventoryContainer;
    }

    private String getNextItemToTake() {
        return recipe.entrySet().stream()
            .filter(entry -> {
                int maxStackSize = maxStackSizes.getOrDefault(entry.getKey(), 64);
                int targetStacks = (int) Math.ceil((double) maxRecipeCount * entry.getValue() / maxStackSize);
                return getCountOfItemKeyStacksInInventory(entry.getKey()) < targetStacks;
            })
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    private int getFirstSlotForCraftItem() {
        List<ItemStack> inventory = Helper.getPlayer().inventory.mainInventory;
        String craftItemKey = getItemKey(craftItem);
        for (int i = 0; i < inventory.size(); i++) {
            if (craftItemKey.equals(getItemKey(inventory.get(i)))) {
                return i;
            }
        }
        return -1;
    }

    private int translateInventorySlotToContainerChestSlot(int slot) {
        return (slot >= 0 && slot <= 8) ? slot + 27 : slot - 9;
    }

    private int getNumberOfRecipeStacksInInventory() {
        String recipeKey = getItemKey(craftItem);
        return (int) Helper.getPlayer().inventory.mainInventory.stream()
            .filter(stack -> recipeKey.equals(getItemKey(stack)))
            .count();
    }

    private void click(int slot) {
        click(Helper.getPlayer().openContainer.windowId, slot);
    }

    private void click(int windowId, int slot) {
        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(windowId, slot, 0, ClickType.QUICK_MOVE));
    }

    private void compClick() {
        click(52);
    }

    private void increaseStep() {
        click(50);
    }

    private void decreaseStep() {
        click(48);
    }

    private int getBestCompSlot() {
        Container container = Helper.getPlayer().openContainer;
        String targetKey = getItemKey(craftItem);
        String[] priority = {"", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN"};

        int bestSlot = 81;
        int bestLevel = 8;

        for (int i = 54; i < 90; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (!slot.getHasStack()) continue;

            ItemStack stack = slot.getStack();
            if (!getItemKey(stack).equals(targetKey)) continue;

            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("compressionLevel")) {
                String level = stack.getTagCompound().getString("compressionLevel");
                for (int j = 1; j < priority.length; j++) {
                    if (level.equals(priority[j]) && j < bestLevel) {
                        bestSlot = i;
                        bestLevel = j;
                        break;
                    }
                }
            } else {
                return i;
            }
        }

        return bestSlot;
    }

    private boolean checkEmptyChest(IInventory chestInventory) {
        boolean isEmpty = true;
        for (int i = 0; i < chestInventory.getSizeInventory(); i++) {
            if (!chestInventory.getStackInSlot(i).isEmpty()) {
                isEmpty = false;
                break;
            }
        }

        if (isEmpty) {
            RayTraceResult trace = Helper.getPlayer().rayTrace(5, 1.0F);
            if (trace != null && trace.typeOfHit == Type.BLOCK) {
                BlockPos currentChestPos = trace.getBlockPos();
                if (currentChestPos.equals(lastEmptyChest)) {
                    emptyChestCounter++;
                } else {
                    lastEmptyChest = currentChestPos;
                    emptyChestCounter = 1;
                }
                if (emptyChestCounter >= MAX_EMPTY_CHEST_ATTEMPTS) {
                    Addon.displayNotification(Addon.translate("autoCrafter.emptyChestDetected"));
                    stop();
                    return true;
                }
            }
        } else {
            lastEmptyChest = null;
            emptyChestCounter = 0;
        }

        return isEmpty;
    }
}
