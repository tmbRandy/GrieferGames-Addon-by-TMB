package tmb.randy.tmbgriefergames.v1_8_9.functions.AutoCrafter;

import static tmb.randy.tmbgriefergames.v1_8_9.functions.AutoCrafter.AutoCrafterV3.COMP_STATE.*;

import java.util.HashMap;
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
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.input.Keyboard;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.AutoCrafterNewFinalAction;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.helper.I19n;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;
import tmb.randy.tmbgriefergames.v1_8_9.click.Click;
import tmb.randy.tmbgriefergames.v1_8_9.click.ClickManager;

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

    private ItemStack craftItem;
    private final Map<String, Integer> recipe = new HashMap<>();
    private final Map<String, BlockPos> sourceChests = new HashMap<>();
    private int maxRecipeCount = 0;
    private COMP_STATE compState = IDLE;
    private boolean displayedSelectMessage;
    private int variantPage = -1;
    private int tickCounter = 0;
    
    // Empty chest detection
    private BlockPos lastEmptyChest = null;
    private int emptyChestCounter = 0;
    private static final int MAX_EMPTY_CHEST_ATTEMPTS = 3;

    public AutoCrafterV3() {
        super(Functions.CRAFTV3);
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (isEnabled() && event.state() == State.PRESS && event.key() == Key.ESCAPE) {
            stop();
        }
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if (!isEnabled()) return;
        
        String message = event.chatMessage().getPlainText();
        if ("Du kannst diese Kiste nicht öffnen, solange sie von einem anderen Spieler benutzt wird.".equals(message)) {
            event.setCancelled(true);
        } else if ("[Rezepte] Es konnte kein Vanilla Rezept für dieses Item gefunden werden.".equals(message)) {
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
        int delay = Addon.getSharedInstance().configuration().getAutoCrafterConfig().getDelay().get();
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
        ItemStack firstItem = Helper.getPlayer().inventory.mainInventory[0];
        if (firstItem == null) {
            Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.noItemFound"));
            stop();
            return;
        }

        Container container = Helper.getPlayer().openContainer;
        if (!(container instanceof ContainerChest chest)) {
            Addon.sendCommand("/rezepte");
            return;
        }

        if (!ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) return;

        IInventory chestInventory = chest.getLowerChestInventory();
        String chestName = chestInventory.getName();

        switch (chestName) {
            case "§6Custom-Kategorien" -> click(chest.windowId, 12);
            case "§6Minecraft-Rezepte" -> handleMinecraftRecipes(chest, firstItem);
            case "§6Vanilla Bauanleitung" -> handleVanillaBauanleitung(chest, chestInventory);
        }
    }

    private void handleMinecraftRecipes(ContainerChest chest, ItemStack firstItem) {
        if (firstItem.getItem().equals(Items.gold_ingot)) {
            int slot = getSlotForGoldIngot();
            click(chest.windowId, slot > 0 ? slot : 53);
        } else {
            click(chest.windowId, 81);
        }
    }

    private void handleVanillaBauanleitung(ContainerChest chest, IInventory chestInventory) {
        if (!areItemStacksEqual(chestInventory.getStackInSlot(25), Helper.getPlayer().inventory.mainInventory[0])) {
            return;
        }

        ItemStack pageIndicator = chestInventory.getStackInSlot(49);
        if (pageIndicator != null && pageIndicator.getItem() == Items.skull) {
            if (!displayedSelectMessage) {
                Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.chooseVariant"));
                displayedSelectMessage = true;
            }

            if (!Keyboard.isKeyDown(Key.ENTER.getId())) return;
            
            String variantPageNumberString = pageIndicator.getDisplayName().replace("§6Variante ", "");
            variantPage = Integer.parseInt(variantPageNumberString);
        }

        craftItem = chestInventory.getStackInSlot(25);
        extractRecipe(chestInventory);
        closeChest();
        Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.recipeSavedV3"));
    }

    private void extractRecipe(IInventory chestInventory) {
        for (int recipeSlot : RECIPE_SLOTS) {
            ItemStack stack = chestInventory.getStackInSlot(recipeSlot);
            if (!isStainedGlassPane(stack) && stack != null && stack.stackSize > 0) {
                String key = getItemKey(stack);
                recipe.merge(key, 1, Integer::sum);
            }
        }
    }

    private void handleChestScanning() {
        Container container = Helper.getPlayer().openContainer;
        if (!(container instanceof ContainerChest chest)) return;

        IInventory chestInventory = chest.getLowerChestInventory();
        ItemStack chestItemStack = allItemsAreEqual(chestInventory);
        
        if (chestItemStack == null) {
            Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.mixedChest"));
            closeChest();
            return;
        }

        String itemKey = getItemKey(chestItemStack);
        MovingObjectPosition trace = Helper.getPlayer().rayTrace(5, 1.0F);
        
        if (trace != null && trace.typeOfHit == MovingObjectType.BLOCK) {
            sourceChests.put(itemKey, trace.getBlockPos());
            closeChest();

            if (allSourceChestsScanned()) {
                Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.startedCrafting"));
                maxRecipeCount = maxRecipeCraftCount();
            } else {
                Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.setChestForMaterial", chestItemStack.getDisplayName()));
            }
        }
    }

    private void craft() {
        int numberOfFinishedStacks = getNumberOfRecipeStacksInInventory();
        AutoCrafterNewFinalAction finalAction = Addon.getSharedInstance().configuration().getAutoCrafterConfig().getFinalActionV3().get();
        
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
            Addon.sendCommand("/rezepte");
            return;
        }

        BlockPos lookingAtBlock = Helper.getBlockPosLookingAt();
        BlockPos neededBlock = sourceChests.get(nextItem);

        if (neededBlock == null) {
            Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.noSourceFound"));
            return;
        }

        if (lookingAtBlock != null) {
            if (lookingAtBlock.equals(neededBlock)) {
                if (!Helper.getPlayer().isSneaking()) {
                    MovingObjectPosition trace = Helper.getPlayer().rayTrace(5, 1.0F);
                    if (trace != null) {
                        Minecraft.getMinecraft().playerController.onPlayerRightClick(
                            Helper.getPlayer(), Helper.getWorld(), Helper.getPlayer().getHeldItem(),
                            lookingAtBlock, trace.sideHit, trace.hitVec
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
            case "§6Custom-Kategorien" -> {
                if (nextItem == null && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                    click(chest.windowId, 12);
                } else if (nextItem != null) {
                    closeChest();
                }
            }
            case "§6Minecraft-Rezepte" -> handleMinecraftRecipesCrafting(chest, nextItem);
            case "§6Vanilla Bauanleitung" -> handleVanillaBauanleitungCrafting(chest, nextItem);
            default -> handleMaterialChest(chest, chestInventory);
        }
    }

    private void handleMinecraftRecipesCrafting(ContainerChest chest, String nextItem) {
        if (nextItem != null) {
            closeChest();
            return;
        }

        if (!ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) return;

        if (craftItem.getItem().equals(Items.gold_ingot)) {
            int slot = getSlotForGoldIngot();
            click(chest.windowId, slot > 0 ? slot : 53);
        } else {
            int slot = getFirstSlotForCraftItem();
            if (slot > -1) {
                int translatedSlot = translateInventorySlotToContainerChestSlot(slot);
                int clickSlot = translatedSlot + chest.getLowerChestInventory().getSizeInventory();
                click(chest.windowId, clickSlot);
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
            if (pageIndicatorSkull != null && pageIndicatorSkull.stackSize > 0) {
                String pageIndicatorName = pageIndicatorSkull.getDisplayName().replace("§6Variante ", "");
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

        // Prüfe zuerst, ob die Kiste leer ist
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
        int neededStacks = (recipe.get(itemKeyForChest) * maxRecipeCount) - stacksInInventory;

        int stacksTaken = 0;
        int size = chestInventory.getSizeInventory();
        
        for (int i = 0; i < size && stacksTaken < neededStacks; i++) {
            ItemStack currentStack = chestInventory.getStackInSlot(i);
            if (getItemKey(currentStack) != null && 
                getItemKey(currentStack).equals(itemKeyForChest) && 
                currentStack.stackSize == currentStack.getMaxStackSize()) {
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
            Addon.sendCommand("/rezepte");
            return;
        }

        IInventory inv = chest.getLowerChestInventory();
        String name = inv.getName();

        switch (name) {
            case "§6Custom-Kategorien" -> click(11);
            case "§6Item-Komprimierung-Bauanleitung" -> click(getBestCompSlot());
            case "§6Item-Komprimierung" -> compState = COMP1;
            case "§6Vanilla Bauanleitung" -> closeChest();
        }
    }

    private void handleCompSteps(Container container) {
        if (!ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) return;
        
        ItemStack stepIndicator = container.getSlot(49).getStack();
        if (!(container instanceof ContainerChest) || stepIndicator == null) return;

        String name = stepIndicator.getDisplayName();
        if (!name.contains("§6Komprimierungsstufe")) return;

        int step = Integer.parseInt(name.replace("§6Komprimierungsstufe ", ""));
        int expectedStep = compState.ordinal() - COMP_STATE.COMP1.ordinal() + 1;

        if (compState == COMP_STATE.FINISHED) {
            closeChest();
        } else if (compState == COMP1 && step > 1) {
            // Spezialfall: Bei COMP1 immer zuerst auf Stufe 1 gehen
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
        StuckProtection.reset();
    }

    private int getSlotForGoldIngot() {
        Container container = Helper.getPlayer().openContainer;
        for (int i = 10; i < 44; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack != null && stack.getItem().equals(Items.gold_ingot) && stack.stackSize == 1) {
                return i;
            }
        }
        return -1;
    }

    private int maxRecipeCraftCount() {
        int slots = recipe.values().stream().mapToInt(Integer::intValue).sum();
        AutoCrafterNewFinalAction finalAction = Addon.getSharedInstance().configuration().getAutoCrafterConfig().getFinalActionV3().get();
        return finalAction == AutoCrafterNewFinalAction.COMP ? (27 / slots) : (32 / slots);
    }

    private ItemStack allItemsAreEqual(IInventory inventory) {
        ItemStack firstStack = null;
        int size = inventory.getSizeInventory();

        for (int i = 0; i < size; i++) {
            ItemStack currentStack = inventory.getStackInSlot(i);
            if (currentStack != null && currentStack.stackSize > 0) {
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
        if (stack == null || stack.stackSize == 0) return null;

        String itemName = Item.itemRegistry.getNameForObject(stack.getItem()).toString();

        if (craftItem != null) {
            String craftItemKey = Item.itemRegistry.getNameForObject(craftItem.getItem()).toString();
            if (META_FREE_MATERIALS.containsKey(craftItemKey) &&
                META_FREE_MATERIALS.get(craftItemKey).contains(itemName)) {
                return itemName;
            }
        }

        return itemName + ":" + stack.getMetadata();
    }

    public static boolean isStainedGlassPane(ItemStack stack) {
        return stack != null && stack.stackSize > 0 && 
               stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane);
    }

    private boolean allSourceChestsScanned() {
        return recipe.keySet().stream().allMatch(sourceChests::containsKey);
    }

    private void closeChest() {
        Minecraft.getMinecraft().displayGuiScreen(null);
        Helper.getPlayer().closeScreen();
    }

    private int getCountOfItemKeyStacksInInventory(String itemKey) {
        return (int) java.util.Arrays.stream(Helper.getPlayer().inventory.mainInventory)
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
                int neededCount = (entry.getValue() * maxRecipeCount) - getCountOfItemKeyStacksInInventory(entry.getKey());
                return neededCount >= 1;
            })
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    private int getFirstSlotForCraftItem() {
        ItemStack[] inventory = Helper.getPlayer().inventory.mainInventory;
        String craftItemKey = getItemKey(craftItem);
        
        for (int i = 0; i < inventory.length; i++) {
            if (craftItemKey.equals(getItemKey(inventory[i]))) {
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
        return (int) java.util.Arrays.stream(Helper.getPlayer().inventory.mainInventory)
            .filter(stack -> recipeKey.equals(getItemKey(stack)))
            .count();
    }

    private void click(int slot) {
        click(Helper.getPlayer().openContainer.windowId, slot);
    }

    private void click(int windowId, int slot) {
        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(windowId, slot, 0, 1));
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
        // Prüfe ob die Kiste komplett leer ist
        boolean isEmpty = true;
        for (int i = 0; i < chestInventory.getSizeInventory(); i++) {
            ItemStack stack = chestInventory.getStackInSlot(i);
            if (stack != null && stack.stackSize > 0) {
                isEmpty = false;
                break;
            }
        }
        
        if (isEmpty) {
            // Hole die aktuelle Chest-Position
            MovingObjectPosition trace = Helper.getPlayer().rayTrace(5, 1.0F);
            if (trace != null && trace.typeOfHit == MovingObjectType.BLOCK) {
                BlockPos currentChestPos = trace.getBlockPos();
                
                // Prüfe ob es dieselbe Kiste wie beim letzten Mal ist
                if (currentChestPos.equals(lastEmptyChest)) {
                    emptyChestCounter++;
                } else {
                    // Neue leere Kiste - Counter zurücksetzen
                    lastEmptyChest = currentChestPos;
                    emptyChestCounter = 1;
                }
                
                // Wenn die Kiste 3x hintereinander leer war, stoppe
                if (emptyChestCounter >= MAX_EMPTY_CHEST_ATTEMPTS) {
                    Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.emptyChestDetected"));
                    stop();
                    return true;
                }
            }
        } else {
            // Kiste ist nicht leer - Counter zurücksetzen
            lastEmptyChest = null;
            emptyChestCounter = 0;
        }
        
        return isEmpty;
    }
}