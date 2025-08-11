package tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter;

import static tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter.AutoCrafterV3.COMP_STATE.COMP1;
import static tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter.AutoCrafterV3.COMP_STATE.FINISHED;
import static tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter.AutoCrafterV3.COMP_STATE.IDLE;
import static tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter.AutoCrafterV3.COMP_STATE.OPEN_COMP;

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
import tmb.randy.tmbgriefergames.core.enums.AutoCrafterNewFinalAction;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.helper.I19n;
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

    private ItemStack craftItem;
    private Map<String, Integer> recipe = new HashMap<>();
    private Map<String, BlockPos> sourceChests = new HashMap<>();
    private int maxRecipeCount = 0;
    private COMP_STATE compState = IDLE;
    private boolean displayedSelectMessage;
    private int variantPage = -1;
    private int tickCounter = 0;

    public AutoCrafterV3() {
        super(Functions.CRAFTV3);
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if(isEnabled() && event.state() == State.PRESS && event.key() == Key.ESCAPE)
            stop();
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if(isEnabled()) {
            if(event.chatMessage().getPlainText().equals("Du kannst diese Kiste nicht öffnen, solange sie von einem anderen Spieler benutzt wird."))
                event.setCancelled(true);
            else if(event.chatMessage().getPlainText().equals("[Rezepte] Es konnte kein Vanilla Rezept für dieses Item gefunden werden."))
                stop();
        }
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if(isEnabled()) {
            if(Addon.getSharedInstance().configuration().getAutoCrafterConfig().getDelay().get() > 0) {
                if(tickCounter < Addon.getSharedInstance().configuration().getAutoCrafterConfig().getDelay().get()) {
                    tickCounter++;
                    return;
                }
                tickCounter = 0;
            }

            StuckProtection.tick(Helper.getPlayer().openContainer);

            if(craftItem == null) {
                if(!Helper.getPlayer().inventory.mainInventory.getFirst().isEmpty()) {
                    if(Helper.getPlayer().openContainer instanceof ContainerChest chest) {
                        if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                            IInventory chestInventory = chest.getLowerChestInventory();
                            if(chestInventory.getName().equals("§6Custom-Kategorien")) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 12, 0, ClickType.QUICK_MOVE));
                            } else if(chestInventory.getName().equals("§6Minecraft-Rezepte")) {
                                if(Helper.getPlayer().inventory.mainInventory.getFirst().getItem().equals(Items.GOLD_INGOT)) {
                                    int slot = getSlotForGoldIngot();
                                    if(slot > 0)
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, slot, 0, ClickType.QUICK_MOVE));
                                    else
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 53, 0, ClickType.QUICK_MOVE));
                                } else
                                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 81, 0, ClickType.QUICK_MOVE));

                            } else if(chestInventory.getName().equals("§6Vanilla Bauanleitung") && areItemStacksEqual(chestInventory.getStackInSlot(25), Helper.getPlayer().inventory.mainInventory.getFirst())) {
                                ItemStack pageIndicator = chestInventory.getStackInSlot(49);
                                if(pageIndicator != null && pageIndicator.getItem() == Items.SKULL) {
                                    if(!displayedSelectMessage) {
                                        Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.chooseVariant"));
                                        displayedSelectMessage = true;
                                    }

                                    if(!Keyboard.isKeyDown(Key.ENTER.getId()))
                                        return;
                                    else {
                                        String variantPageNumberString = pageIndicator.getDisplayName().replace("§6Variante ", "");
                                        variantPage = Integer.parseInt(variantPageNumberString);
                                    }
                                }

                                craftItem = chestInventory.getStackInSlot(25);

                                for (int recipeSlot : RECIPE_SLOTS) {
                                    ItemStack stack = chestInventory.getStackInSlot(recipeSlot);
                                    if(!isStainedGlassPane(stack) && stack != null && !stack.isEmpty()) {
                                        String key = getItemKey(stack);
                                        recipe.put(key, recipe.getOrDefault(key, 0) + 1);
                                    }
                                }

                                closeChest();

                                Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.recipeSavedV3"));
                            }
                        }
                    } else {
                        Addon.sendCommand("/rezepte");
                    }
                } else {
                    Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.noItemFound"));
                    stop();
                }
            } else {
                if(allSourceChestsScanned()) {
                    craft();
                } else {
                    if(Helper.getPlayer().openContainer instanceof ContainerChest chest) {
                        IInventory chestInventory = chest.getLowerChestInventory();
                        ItemStack chestItemStack = allItemsAreEqual(chestInventory);
                        if(chestItemStack != null) {
                            String itemKey = getItemKey(chestItemStack);
                            RayTraceResult trace = Helper.getPlayer().rayTrace(5, 1.0F);
                            if(trace != null && trace.typeOfHit == Type.BLOCK) {
                                sourceChests.put(itemKey, trace.getBlockPos());
                                closeChest();

                                if(allSourceChestsScanned()) {
                                    Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.startedCrafting"));
                                    maxRecipeCount = maxRecipeCraftCount();
                                } else {
                                    Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.setChestForMaterial", chestItemStack.getDisplayName()));
                                }
                            }
                        } else {
                            Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.mixedChest"));
                            closeChest();
                        }
                    }
                }
            }
        }
    }



    @Override
    public boolean stop() {
       if(super.stop()) {
           displayedSelectMessage = false;
           variantPage = -1;
           craftItem = null;
           recipe = new HashMap<>();
           sourceChests = new HashMap<>();
           maxRecipeCount = 0;
           compState = IDLE;
           StuckProtection.reset();
       }
       return true;
    }

    private void craft() {
        int numberOfFinishedStacks = getNumperOfRecipeStacksInInventory();
        if(numberOfFinishedStacks <= 1 || (Addon.getSharedInstance().configuration().getAutoCrafterConfig().getFinalActionV3().get() == AutoCrafterNewFinalAction.COMP && compState == FINISHED)) {

            if(Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
                closeChest();
            }

            //Craft more
            String nextItem = getNextItemToTake();

            if(!isContainerOpen()) {
                if(nextItem == null) {
                    Addon.sendCommand("/rezepte");
                } else {
                    BlockPos lookingAtBlock = Helper.getBlockPosLookingAt();
                    BlockPos neededBlock = sourceChests.get(nextItem);

                    if(neededBlock != null) {
                        if(lookingAtBlock != null) {
                            if(lookingAtBlock.equals(neededBlock)) {
                                if(!Helper.getPlayer().isSneaking()) {
                                    RayTraceResult trace = Helper.getPlayer().rayTrace(5, 1.0F);
                                    if(trace != null) {
                                        Minecraft.getMinecraft().playerController.processRightClickBlock(Helper.getPlayer(), Helper.getWorld(), lookingAtBlock, trace.sideHit, trace.hitVec, EnumHand.MAIN_HAND);
                                    }
                                }
                            } else {
                                Helper.lookAtBlockPos(neededBlock);
                            }
                        }
                    } else {
                        Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.noSourceFound"));
                    }
                }

            } else if(Helper.getPlayer().openContainer instanceof ContainerChest chest) {
                IInventory chestInventory = chest.getLowerChestInventory();

                switch (chestInventory.getName()) {
                    case "§6Custom-Kategorien": {
                        if(nextItem != null) {
                            closeChest();
                        } else {
                            if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 12, 0, ClickType.QUICK_MOVE));
                            }
                        }
                        break;
                    }
                    case "§6Minecraft-Rezepte": {
                        if(nextItem != null) {
                            closeChest();
                        } else {
                            if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                                if(craftItem.getItem().equals(Items.GOLD_INGOT)) {
                                    int slot = getSlotForGoldIngot();
                                    if(slot > 0) {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, slot, 0, ClickType.QUICK_MOVE));
                                    } else {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 53, 0, ClickType.QUICK_MOVE));
                                    }
                                } else {
                                    int slot = getFirstSlotForCraftItem();
                                    int translatedSlot = translateInventorySlotToContainerCHestSlot(slot);
                                    if(slot > -1) {
                                        int clickSlot = translatedSlot + chest.getLowerChestInventory().getSizeInventory();
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, clickSlot, 0, ClickType.QUICK_MOVE));
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case "§6Vanilla Bauanleitung": {
                        if(!ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM))
                            return;

                        if(nextItem != null)
                            closeChest();
                        else {
                            if(variantPage != -1) {
                                if(chestInventory.getStackInSlot(49) != null && !chestInventory.getStackInSlot(49).isEmpty()) {
                                    ItemStack pageIndicatorSkull = chestInventory.getStackInSlot(49);
                                    String pageIndicatorName = pageIndicatorSkull.getDisplayName().replace("§6Variante ", "");
                                    int currentPage = Integer.parseInt(pageIndicatorName);
                                    if(currentPage == variantPage) {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 52, 0, ClickType.QUICK_MOVE));
                                        compState = IDLE;
                                    } else if (currentPage < variantPage) {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 50, 0, ClickType.QUICK_MOVE));
                                    } else {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 48, 0, ClickType.QUICK_MOVE));
                                    }
                                }
                            } else {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 52, 0, ClickType.QUICK_MOVE));
                                compState = IDLE;
                            }
                        }
                        break;
                    }
                    default: {
                        if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                            ItemStack itemStack = allItemsAreEqual(chestInventory);
                            if(itemStack != null) {
                                String itemKeyForChest = getItemKey(itemStack);
                                int stacksInInventory = getCountOfItemKeyStacksInInventory(itemKeyForChest);
                                int neededStacks = (recipe.get(itemKeyForChest) * maxRecipeCount) - stacksInInventory;

                                int size = chestInventory.getSizeInventory();

                                int stacksTaken = 0;
                                for (int i = 0; i < size; i++) {
                                    ItemStack currentStack = chestInventory.getStackInSlot(i);

                                    if (getItemKey(currentStack) != null && getItemKey(currentStack).equals(itemKeyForChest) && currentStack.getCount() == currentStack.getMaxStackSize()) {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, i, 0, ClickType.QUICK_MOVE));
                                        stacksTaken++;
                                    }

                                    if(stacksTaken >= neededStacks)
                                        break;
                                }

                                if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                                    closeChest();
                                }
                            } else {
                                closeChest();
                            }
                        }
                        break;
                    }
                }
            }
        } else {
            if(Addon.getSharedInstance().configuration().getAutoCrafterConfig().getFinalActionV3().get() == AutoCrafterNewFinalAction.DROP) {
                //Drop crafted items
                if(Helper.getPlayer().openContainer instanceof ContainerChest) {
                    closeChest();
                } else if(Helper.getPlayer().openContainer instanceof ContainerPlayer inv && Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
                    boolean skippedFirst = false;
                    if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                        int size = inv.inventorySlots.size();
                        for(int i = 9; i < size; i++) {
                            if(inv.getSlot(i).getHasStack()) {
                                ItemStack stack = inv.getSlot(i).getStack();
                                String key = getItemKey(stack);
                                String craftItemKey = getItemKey(craftItem);
                                if(key.equals(craftItemKey)) {
                                    if(skippedFirst)
                                        ClickManager.getSharedInstance().dropClick(i);
                                    else
                                        skippedFirst = true;
                                }
                            }
                        }
                    }
                } else {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(Helper.getPlayer()));
                }
            } else if(Addon.getSharedInstance().configuration().getAutoCrafterConfig().getFinalActionV3().get() == AutoCrafterNewFinalAction.COMP) {
                if(compState == IDLE) {
                    closeChest();
                    compState = OPEN_COMP;
                }

                comp();
            }
        }
    }

    private int getSlotForGoldIngot() {
        for (int i = 10; i < 44; i++) {
            ItemStack stack = Helper.getPlayer().openContainer.getSlot(i).getStack();
            if(!stack.isEmpty()) {
                if(stack.getItem().equals(Items.GOLD_INGOT)) {
                    if(stack.getCount() == 1) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private int maxRecipeCraftCount() {
        int slots = 0;

        for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
            slots += entry.getValue();
        }

        return Addon.getSharedInstance().configuration().getAutoCrafterConfig().getFinalActionV3().get() == AutoCrafterNewFinalAction.COMP ? (27 / slots) : (32 / slots);
    }

    private ItemStack allItemsAreEqual(IInventory inventory) {
        int size = inventory.getSizeInventory();
        ItemStack firstStack = null;

        for (int i = 0; i < size; i++) {
            ItemStack currentStack = inventory.getStackInSlot(i);

            if (currentStack != ItemStack.EMPTY) {
                if (firstStack == null) {
                    firstStack = currentStack;
                } else {
                    if (!areItemStacksEqual(firstStack, currentStack)) {
                        return null;
                    }
                }
            }
        }
        return firstStack;
    }

    private boolean areItemStacksEqual(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() != stack2.getItem())
            return false;

        String key1 = getItemKey(stack1);
        String key2 = getItemKey(stack2);

        return key1 != null && key1.equals(key2);
    }

    private String getItemKey(ItemStack stack) {
        if (stack == null || stack.getCount() == 0)
            return null;

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
        if (stack == null || stack.isEmpty())
            return false;

        Item stainedGlassPaneItem = Item.getItemFromBlock(net.minecraft.init.Blocks.STAINED_GLASS_PANE);
        return stack.getItem() == stainedGlassPaneItem;
    }

    private boolean allSourceChestsScanned() {
        for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
            String item = entry.getKey();

            if(!sourceChests.containsKey(item))
                return false;
        }

        return true;
    }

    private void closeChest() {
        Minecraft.getMinecraft().displayGuiScreen(null);
        Helper.getPlayer().closeScreen();
    }

    private int getCountOfItemKeyStacksInInventory(String itemKey) {
        int count = 0;

        for (ItemStack itemStack : Helper.getPlayer().inventory.mainInventory) {
            String key = getItemKey(itemStack);
            if(key != null && key.equals(itemKey)) {
                count++;
            }
        }

        return count;
    }

    public static boolean isContainerOpen() {
        Container openContainer = Helper.getPlayer().openContainer;
        return openContainer != Helper.getPlayer().inventoryContainer;
    }

    private String getNextItemToTake() {
        for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
            String item = entry.getKey();
            Integer count = entry.getValue();

            int neededCount = (count * maxRecipeCount) - getCountOfItemKeyStacksInInventory(item);

            if(neededCount >= 1) {
                return item;
            }
        }

        return null;
    }

    private int getFirstSlotForCraftItem() {
        for (int i = 0; i < Helper.getPlayer().inventory.mainInventory.size(); i++) {
            ItemStack itemStack = Helper.getPlayer().inventory.mainInventory.get(i);
            if(getItemKey(craftItem).equals(getItemKey(itemStack))) {
                return i;
            }
        }
        return -1;
    }

    private int translateInventorySlotToContainerCHestSlot(int slot) {
        if(slot >= 0 && slot <= 8)
            return slot + 27;
        else
            return slot - 9;
    }

    private int getNumperOfRecipeStacksInInventory() {
        int count = 0;
        for (ItemStack itemStack : Helper.getPlayer().inventory.mainInventory) {
            String itemKey = getItemKey(itemStack);
            String recipeKey = getItemKey(craftItem);
            if(itemKey != null && itemKey.equals(recipeKey)) {
                count++;
            }

        }

        return count;
    }

    private void comp() {
        var player = Helper.getPlayer();
        var container = player.openContainer;

        switch (compState) {
            case OPEN_COMP -> {
                if (container instanceof ContainerChest chest) {
                    IInventory inv = chest.getLowerChestInventory();
                    String name = inv.getName();

                    if (name.equalsIgnoreCase("§6Custom-Kategorien")) {
                        click(11);
                    } else if (name.equalsIgnoreCase("§6Item-Komprimierung-Bauanleitung")) {
                        click(getBestCompSlot());
                    } else if (name.equalsIgnoreCase("§6Item-Komprimierung")) {
                        compState = COMP1;
                    } else if (name.equalsIgnoreCase("§6Vanilla Bauanleitung")) {
                        closeChest();
                    }
                } else {
                    Addon.sendCommand("/rezepte");
                }
            }
            case COMP1, COMP2, COMP3, COMP4, COMP5, COMP6, FINISHED -> {
                if (!ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) return;
                if (!(container instanceof ContainerChest) || container.getSlot(49).getStack() == null) return;

                String name = container.getSlot(49).getStack().getDisplayName();
                if (!name.contains("§6Komprimierungsstufe")) return;

                int step = Integer.parseInt(name.replace("§6Komprimierungsstufe ", ""));
                int expectedStep = compState.ordinal() - COMP_STATE.COMP1.ordinal() + 1;

                if (compState == COMP_STATE.FINISHED) {
                    closeChest();
                    return;
                }

                if (step == expectedStep) {
                    compClick();
                    compState = COMP_STATE.values()[compState.ordinal() + 1];
                } else if (step < expectedStep) {
                    increaseStep();
                } else {
                    decreaseStep();
                }
            }
        }
    }

    private void click(int slot) {
        ClickManager.getSharedInstance().addClick(
            QueueType.MEDIUM,
            new Click(Helper.getPlayer().openContainer.windowId, slot, 0, ClickType.QUICK_MOVE)
        );
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
        var container = Helper.getPlayer().openContainer;
        String targetKey = getItemKey(craftItem);

        String[] priority = { "", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN" };
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
                return i; // unkomprimiertes Item sofort nehmen
            }
        }

        return bestSlot;
    }
}