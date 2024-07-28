package tmb.randy.tmbgriefergames.v1_8_9.util.AutoCrafter;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
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
import net.minecraft.util.Vec3;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.AutoCrafterNewFinalAction;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.v1_8_9.util.VersionisedBridge;
import tmb.randy.tmbgriefergames.v1_8_9.util.click.Click;
import tmb.randy.tmbgriefergames.v1_8_9.util.click.ClickManager;
import java.util.HashMap;
import java.util.Map;

import static tmb.randy.tmbgriefergames.v1_8_9.util.AutoCrafter.AutoCrafterV3.COMP_STATE.*;

public class AutoCrafterV3 {
    private int tickCounter = 0;

    enum COMP_STATE {
        IDLE, OPEN_COMP, COMP1, COMP2, COMP3, COMP4, COMP5, COMP6, FINISHED
    }

    private static final int[] RECIPE_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};

    private boolean active;
    private ItemStack craftItem;
    private Map<String, Integer> recipe = new HashMap<>();
    private Map<String, BlockPos> sourceChests = new HashMap<>();
    private int maxRecipeCount = 0;
    private COMP_STATE compState = IDLE;

    public void onKey(KeyEvent event) {
        if(active && event.state() == State.PRESS && event.key() == Key.ESCAPE) {
            stop();
        }
    }

    public void chatMessageReceived(ChatReceiveEvent event) {
        if(active && event.chatMessage().getPlainText().endsWith("Du kannst diese Kiste nicht öffnen, solange sie von einem anderen Spieler benutzt wird."))
            event.setCancelled(true);
    }

    public void onTick(GameTickEvent event) {
        if(active) {

            if(Addon.getSharedInstance().configuration().getAutoCrafterConfig().getDelay().get() > 0) {
                if(tickCounter < Addon.getSharedInstance().configuration().getAutoCrafterConfig().getDelay().get()) {
                    tickCounter++;
                    return;
                }
                tickCounter = 0;
            }

            StuckProtection.tick(Minecraft.getMinecraft().thePlayer.openContainer);

            if(craftItem == null) {
                if(Minecraft.getMinecraft().thePlayer.inventory.mainInventory[0] != null) {
                    if(Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerChest chest) {
                        if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                            IInventory chestInventory = chest.getLowerChestInventory();
                            if(chestInventory.getName().equals("§6Custom-Kategorien")) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 12, 0, 1));
                            } else if(chestInventory.getName().equals("§6Minecraft-Rezepte")) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 81, 0, 1));
                            } else if(chestInventory.getName().equals("§6Vanilla Bauanleitung") && areItemStacksEqual(chestInventory.getStackInSlot(25), Minecraft.getMinecraft().thePlayer.inventory.mainInventory[0])) {
                                craftItem = chestInventory.getStackInSlot(25);

                                for (int recipeSlot : RECIPE_SLOTS) {
                                    ItemStack stack = chestInventory.getStackInSlot(recipeSlot);
                                    if(!isStainedGlassPane(stack) && stack != null) {
                                        String key = getItemKey(stack);
                                        recipe.put(key, recipe.getOrDefault(key, 0) + 1);
                                    }
                                }

                                closeChest();

                                Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.autoCrafter.recipeSavedV3"));
                            }
                        }
                    } else {
                        VersionisedBridge.sendCommand("/rezepte");
                    }
                } else {
                    Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.autoCrafter.noItemFound"));
                    stop();
                }
            } else {
                if(allSourceChestsScanned()) {
                    craft();
                } else {
                    if(Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerChest chest) {
                        IInventory chestInventory = chest.getLowerChestInventory();
                        ItemStack chestItemStack = allItemsAreEqual(chestInventory);
                        if(chestItemStack != null) {
                            String itemKey = getItemKey(chestItemStack);
                            MovingObjectPosition trace = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1.0F);
                            if(trace != null && trace.typeOfHit == MovingObjectType.BLOCK) {
                                sourceChests.put(itemKey, trace.getBlockPos());
                                closeChest();

                                if(allSourceChestsScanned()) {
                                    Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.autoCrafter.startedCrafting"));
                                    maxRecipeCount = maxRecipeCraftCount();
                                } else {
                                    Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.autoCrafter.setChestForMaterial", chestItemStack.getDisplayName()));
                                }
                            }
                        } else {
                            Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.autoCrafter.mixedChest"));
                            closeChest();
                        }
                    }
                }
            }
        }
    }

    public void start() {
        active = true;
        Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.autoCrafter.V3started"));
    }
    public void stop() {
        active = false;
        craftItem = null;
        recipe = new HashMap<>();
        sourceChests = new HashMap<>();
        maxRecipeCount = 0;
        compState = IDLE;
        Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.autoCrafter.V3stopped"));
    }
    public void toggle() {
        if(active)
            stop();
        else
            start();
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
                    VersionisedBridge.sendCommand("/rezepte");
                } else {
                    BlockPos lookingAtBlock = getBlockLookingAt();
                    BlockPos neededBlock = sourceChests.get(nextItem);

                    if(neededBlock != null) {
                        if(lookingAtBlock != null) {
                            if(lookingAtBlock.equals(neededBlock)) {
                                if(!Minecraft.getMinecraft().thePlayer.isSneaking()) {
                                    MovingObjectPosition trace = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1.0F);
                                    Minecraft.getMinecraft().playerController.onPlayerRightClick(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer.getHeldItem(), lookingAtBlock, trace.sideHit, trace.hitVec);
                                }
                            } else {
                                lookAtBlockPos(neededBlock);
                            }
                        }
                    } else {
                        Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.autoCrafter.noSourceFound"));
                    }
                }

            } else if(Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerChest chest) {
                IInventory chestInventory = chest.getLowerChestInventory();

                switch (chestInventory.getName()) {
                    case "§6Custom-Kategorien": {
                        if(nextItem != null) {
                            closeChest();
                        } else {
                            if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 12, 0, 1));
                            }
                        }
                        break;
                    }
                    case "§6Minecraft-Rezepte": {
                        if(nextItem != null) {
                            closeChest();
                        } else {
                            if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                                int slot = getFirstSlotForCraftItem();
                                int translatedSlot = translateInventorySlotToContainerCHestSlot(slot);
                                if(slot > -1) {
                                    int clickSlot = translatedSlot + chest.getLowerChestInventory().getSizeInventory();
                                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, clickSlot, 0, 1));
                                }
                            }
                        }
                        break;
                    }
                    case "§6Vanilla Bauanleitung": {
                        if(nextItem != null)
                            closeChest();
                        else {
                            ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 52, 0, 1));
                            compState = IDLE;
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

                                    if (getItemKey(currentStack) != null && getItemKey(currentStack).equals(itemKeyForChest) && currentStack.stackSize == currentStack.getMaxStackSize()) {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, i, 0, 1));
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
                if(Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerChest) {
                    closeChest();
                } else if(Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerPlayer inv && Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
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
                    Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(Minecraft.getMinecraft().thePlayer));
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

            if (currentStack != null && currentStack.stackSize > 0) {
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

    private static boolean areItemStacksEqual(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem() && stack1.getMetadata() == stack2.getMetadata();
    }

    private static String getItemKey(ItemStack stack) {
        if (stack == null || stack.stackSize == 0)
            return null;

        return Item.itemRegistry.getNameForObject(stack.getItem()).toString() + ":" + stack.getMetadata();
    }

    public static boolean isStainedGlassPane(ItemStack stack) {
        if (stack == null || stack.stackSize == 0)
            return false;

        Item stainedGlassPaneItem = Item.getItemFromBlock(Blocks.stained_glass_pane);
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
        Minecraft.getMinecraft().thePlayer.closeScreen();
    }

    private int getCountOfItemKeyStacksInInventory(String itemKey) {
        int count = 0;

        for (ItemStack itemStack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
            String key = getItemKey(itemStack);
            if(key != null && key.equals(itemKey)) {
                count++;
            }
        }

        return count;
    }

    public static boolean isContainerOpen() {
        Minecraft mc = Minecraft.getMinecraft();
        Container openContainer = mc.thePlayer.openContainer;
        return openContainer != mc.thePlayer.inventoryContainer;
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
        for (int i = 0; i < Minecraft.getMinecraft().thePlayer.inventory.mainInventory.length; i++) {
            ItemStack itemStack = Minecraft.getMinecraft().thePlayer.inventory.mainInventory[i];
            if(getItemKey(itemStack).equals(getItemKey(craftItem))) {
                return i;
            }
        }
        return -1;
    }

    private void lookAtBlockPos(BlockPos pos) {
        Vec3 playerPos = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0F);
        Vec3 targetPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        double diffX = targetPos.xCoord - playerPos.xCoord;
        double diffY = targetPos.yCoord - playerPos.yCoord;
        double diffZ = targetPos.zCoord - playerPos.zCoord;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * (180 / Math.PI)) - 90.0F;
        float pitch = (float) (-(Math.atan2(diffY, diffXZ) * (180 / Math.PI)));

        Minecraft.getMinecraft().thePlayer.rotationYaw = yaw;
        Minecraft.getMinecraft().thePlayer.rotationPitch = pitch;
    }

    private BlockPos getBlockLookingAt() {
        MovingObjectPosition trace = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1.0F);
        if(trace != null && trace.typeOfHit == MovingObjectType.BLOCK) {
            return trace.getBlockPos();
        }

        return null;
    }

    private int translateInventorySlotToContainerCHestSlot(int slot) {
        if(slot >= 0 && slot <= 8)
            return slot + 27;
        else
            return slot - 9;
    }

    private int getNumperOfRecipeStacksInInventory() {
        int count = 0;
        for (ItemStack itemStack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
            String itemKey = getItemKey(itemStack);
            String recipeKey = getItemKey(craftItem);
            if(itemKey != null && itemKey.equals(recipeKey)) {
                count++;
            }

        }

        return count;
    }



    private void comp() {
        switch (compState) {
            case OPEN_COMP -> {
                if(Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerChest chest) {
                    IInventory inv = chest.getLowerChestInventory();
                    if(inv.getName().equalsIgnoreCase("§6Custom-Kategorien")) {
                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(Minecraft.getMinecraft().thePlayer.openContainer.windowId, 11, 0, 1));
                    } else if(inv.getName().equalsIgnoreCase("§6Item-Komprimierung-Bauanleitung")) {
                        int clickSlot = getBestCompSlot();
                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(Minecraft.getMinecraft().thePlayer.openContainer.windowId, clickSlot, 0, 1));
                    } else if(inv.getName().equalsIgnoreCase("§6Item-Komprimierung")) {
                        compState = COMP1;
                    } else if(inv.getName().equalsIgnoreCase("§6Vanilla Bauanleitung")) {
                        closeChest();
                    }
                } else {
                    VersionisedBridge.sendCommand("/rezepte");
                }
            }
            case COMP1, COMP2, COMP3, COMP4, COMP5, COMP6, FINISHED -> {
                if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                    if(Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerChest && Minecraft.getMinecraft().thePlayer.openContainer.getSlot(49).getStack() != null) {
                        String headName = Minecraft.getMinecraft().thePlayer.openContainer.getSlot(49).getStack().getDisplayName();
                        if(headName.contains("§6Komprimierungsstufe")) {
                            int step = Integer.parseInt(headName.replace("§6Komprimierungsstufe ", ""));

                            switch (compState) {
                                case COMP1 -> {
                                    if(step > 1)
                                        decreaseStep();
                                    else {
                                        compState = COMP2;
                                        compClick();
                                    }
                                }
                                case COMP2 -> {
                                    if(step == 2) {
                                        compState = COMP3;
                                        compClick();
                                    }
                                    else if (step < 2)
                                        increaseStep();
                                    else if(step > 2)
                                        decreaseStep();
                                }
                                case COMP3 -> {
                                    if(step == 3) {
                                        compState = COMP4;
                                        compClick();
                                    }
                                    else if (step < 3)
                                        increaseStep();
                                    else if(step > 3)
                                        decreaseStep();
                                }
                                case COMP4 -> {
                                    if(step == 4) {
                                        compState = COMP5;
                                        compClick();
                                    }
                                    else if (step < 4)
                                        increaseStep();
                                    else if(step > 4)
                                        decreaseStep();
                                }
                                case COMP5 -> {
                                    if(step == 5) {
                                        compState = COMP6;
                                        compClick();
                                    }
                                    else if (step < 5)
                                        increaseStep();
                                    else if(step > 5)
                                        decreaseStep();
                                }
                                case COMP6 -> {
                                    if(step == 6) {
                                        compClick();
                                        compState = FINISHED;
                                    }
                                    else if (step < 6)
                                        increaseStep();
                                    else if(step > 6)
                                        decreaseStep();
                                }
                                case FINISHED -> {
                                    closeChest();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void compClick() {
        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(Minecraft.getMinecraft().thePlayer.openContainer.windowId, 52, 0, 1));
    }

    private void increaseStep() {
        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(Minecraft.getMinecraft().thePlayer.openContainer.windowId, 50, 0, 1));
    }

    private void decreaseStep() {
        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(Minecraft.getMinecraft().thePlayer.openContainer.windowId, 48, 0, 1));
    }

    private int getBestCompSlot() {
        //Searches for the best slot in the players inventory to click on. Clicking a lower compression state saves some time.
        int bestSlot = 81;
        String bestSlotLevel = "";

        for (int i = 54; i < 90; i++) {
            Slot slot = Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.get(i);
            if(slot.getHasStack()) {
                ItemStack stack = slot.getStack();
                if(getItemKey(stack).equals(getItemKey(craftItem))) {
                    if(stack.hasTagCompound() && stack.getTagCompound() != null) {
                        if(stack.getTagCompound().hasKey("compressionLevel")) {
                            String level = stack.getTagCompound().getString("compressionLevel");

                            switch (level) {
                                case "ONE": {
                                    if(!bestSlotLevel.equals("ONE")) {
                                        bestSlot = i;
                                        bestSlotLevel = "ONE";
                                    }
                                    break;
                                }
                                case "TWO": {
                                    if(bestSlotLevel.isEmpty() || bestSlotLevel.equals("THREE") || bestSlotLevel.equals("FOUR") || bestSlotLevel.equals("FIVE") || bestSlotLevel.equals("SIX") || bestSlotLevel.equals("SEVEN")) {
                                        bestSlot = i;
                                        bestSlotLevel = "TWO";
                                    }
                                    break;
                                }
                                case "THREE": {
                                    if(bestSlotLevel.isEmpty() || bestSlotLevel.equals("FOUR") || bestSlotLevel.equals("FIVE") || bestSlotLevel.equals("SIX") || bestSlotLevel.equals("SEVEN")) {
                                        bestSlot = i;
                                        bestSlotLevel = "THREE";
                                    }
                                    break;
                                }
                                case "FOUR": {
                                    if(bestSlotLevel.isEmpty() || bestSlotLevel.equals("FIVE") || bestSlotLevel.equals("SIX") || bestSlotLevel.equals("SEVEN")) {
                                        bestSlot = i;
                                        bestSlotLevel = "FOUR";
                                    }
                                    break;
                                }
                                case "FIVE": {
                                    if(bestSlotLevel.isEmpty() || bestSlotLevel.equals("SIX") || bestSlotLevel.equals("SEVEN")) {
                                        bestSlot = i;
                                        bestSlotLevel = "FIVE";
                                    }
                                    break;
                                }
                                case "SIX": {
                                    if(bestSlotLevel.isEmpty() || bestSlotLevel.equals("SEVEN")) {
                                        bestSlot = i;
                                        bestSlotLevel = "SIX";
                                    }
                                    break;
                                }
                                case "SEVEN": {
                                    if(bestSlotLevel.isEmpty()) {
                                        bestSlot = i;
                                        bestSlotLevel = "SEVEN";
                                    }
                                    break;
                                }
                            }
                            continue;
                        }
                    }

                    // Found uncompressed item.
                    return i;
                }
            }
        }

        return bestSlot;
    }
}
