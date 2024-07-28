package tmb.randy.tmbgriefergames.v1_8_9.util;

import java.util.LinkedList;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.v1_8_9.util.click.Click;
import tmb.randy.tmbgriefergames.v1_8_9.util.click.ClickManager;


public class ItemShifter {

    private Item itemToMove;
    private ContainerChest currentChest;
    private boolean topToBottom = true;

    private final LinkedList<Click> toSend = new LinkedList<>();

    private final Item[] spawnerMenuItems = new Item[] {
        Item.getItemById(160),
        Item.getItemById(384),
        Item.getItemById(347),
        Item.getItemById(76),
        Item.getItemById(54)
    };

    private static ItemShifter sharedInstance;

    public static ItemShifter getSharedInsance() {
        if(sharedInstance == null) {
            sharedInstance = new ItemShifter();
        }
        return sharedInstance;
    }

    public void stopShifting() {
        itemToMove = null;
        currentChest = null;
        topToBottom = true;
    }

    public void startShifting() {
        this.currentChest = (ContainerChest) Minecraft.getMinecraft().thePlayer.openContainer;

        if(Minecraft.getMinecraft().thePlayer.getHeldItem() == null)
            this.itemToMove = null;
        else
            this.itemToMove = Minecraft.getMinecraft().thePlayer.getHeldItem().getItem();

        int containerSize = currentChest.inventorySlots.size();

        int fromMin = 0;
        int fromMax;
        int destMin = 0;
        int destMax;

        if(topToBottom) {
            if(containerSize == 63) {
                fromMax = 26;
                destMin = 27;
                destMax = 62;
            } else if(containerSize == 90) {
                fromMax = 53;
                destMin = 54;
                destMax = 89;
            } else {return;}
        } else {
            if(containerSize == 63) {
                destMax = 26;
                fromMin = 27;
                fromMax = 62;
            } else if(containerSize == 90) {
                destMax = 53;
                fromMin = 54;
                fromMax = 89;
            } else {return;}
        }

        depositHeld(fromMin, fromMax, destMin, destMax);

        int availableSlots = getEmptySlotsInRange(destMin, destMax);
        boolean isSpawner = this.checkIfContainerIsSpawner();


        for(int from = fromMin; from <= fromMax; from++) {

            Slot fromSlot = this.currentChest.inventorySlots.get(from);

            if(fromSlot.getStack() == null)
                continue;

            Item currentItem = fromSlot.getStack().getItem();
            String currentItemName = fromSlot.getStack().getDisplayName();

            if(fromSlot.getHasStack() && availableSlots > 0) {
                if(!(isSpawner && ArrayUtils.contains(spawnerMenuItems, currentItem))) {
                    if(itemToMove == null || (itemToMove.equals(currentItem) && this.itemToMove.equals(currentItemName))) {
                        this.shiftClick(from);
                        availableSlots -= 1;
                    }
                }
            }
        }

        this.itemToMove = null;
        this.sendQueue();
    }

    public void tick(GameTickEvent event) {
        if((Keyboard.isKeyDown(Key.ARROW_LEFT.getId()) && Keyboard.isKeyDown(Key.ARROW_UP.getId()) && Keyboard.isKeyDown(Key.ARROW_RIGHT.getId())) || !(Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerChest))
            return;

        if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.openContainer != null && Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
            ContainerChest chest = ((ContainerChest) Minecraft.getMinecraft().thePlayer.openContainer);
            IInventory inv = chest.getLowerChestInventory();

            if (currentChest != null && inv.getName().contains("Komprimierung")) {
                stopShifting();
            }

            if (Keyboard.isKeyDown(Key.ARROW_UP.getId()) && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.SLOW)) {
                if (inv.getName().equalsIgnoreCase("§6Trichter-Einstellungen") && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.SLOW)) {
                    shiftClick(49);
                    for (int i = 0; i < 15; i++) {
                        shiftClick(32);
                    }
                } else {
                    setTopToBottom(false);
                    startShifting();
                }
            } else if (Keyboard.isKeyDown(Key.ARROW_DOWN.getId()) && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.SLOW)) {
                setTopToBottom(true);
                startShifting();
            }
        }
    }

    public void onKey(KeyEvent event) {
        if ((event.key() == Key.ARROW_UP || event.key() == Key.ARROW_LEFT || event.key() == Key.ARROW_RIGHT) &&
            Keyboard.isKeyDown(Key.ARROW_UP.getId()) && Keyboard.isKeyDown(Key.ARROW_LEFT.getId()) && Keyboard.isKeyDown(Key.ARROW_RIGHT.getId()) &&
            Minecraft.getMinecraft().thePlayer.openContainer != null && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM) && Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
            ClickManager.getSharedInstance().dropInventory();
        }
    }

    private boolean checkIfContainerIsSpawner() {
        if(Minecraft.getMinecraft().thePlayer.openContainer == null)
            return false;

        IInventory inv = ((ContainerChest) Minecraft.getMinecraft().thePlayer.openContainer).getLowerChestInventory();
        return inv.getName().equalsIgnoreCase("§6Spawner - Lager");
    }

    private void shiftClick(int slot) {
        this.toSend.addLast(new Click(Minecraft.getMinecraft().thePlayer.openContainer.windowId, slot, 0, 1));
    }

    private int getEmptySlotsInRange(int start, int end) {
        int output = 0;

        for(int i = start; i <= end; i++) {
            if(!this.currentChest.inventorySlots.get(i).getHasStack()) {
                output++;
            }
        }

        return output;
    }

    private void depositHeld(int fromMin, int fromMax, int destMin, int destMax) {
        for(int slot = destMin; slot <= destMax; slot++) {
            if (!(this.currentChest.inventorySlots.get(slot)).getHasStack())
            {
                Minecraft.getMinecraft().playerController.windowClick(this.currentChest.windowId, slot, 0, 0, Minecraft.getMinecraft().thePlayer);
                ItemStack stack = (this.currentChest.inventorySlots.get(slot)).getStack();

                if(stack == null) {
                    itemToMove = null;
                } else {
                    this.itemToMove = stack.getItem();
                }
                return;
            }
        }


        for(int slot = fromMin; slot <= fromMax; slot++) {
            if (!(this.currentChest.inventorySlots.get(slot)).getHasStack())
            {
                Minecraft.getMinecraft().playerController.windowClick(this.currentChest.windowId, slot, 0, 0, Minecraft.getMinecraft().thePlayer);
                ItemStack stack = (this.currentChest.inventorySlots.get(slot)).getStack();

                if(stack == null)
                    return;

                this.itemToMove = stack.getItem();

                return;
            }
        }

    }

    public void setTopToBottom(boolean topToBottom) {
        this.topToBottom = topToBottom;
    }

    private void sendQueue() {
        if(checkIfContainerIsSpawner()) {
            ClickManager.getSharedInstance().queueClicks(QueueType.SLOW, toSend);
        } else {
            ClickManager.getSharedInstance().queueClicks(QueueType.SLOW, toSend);
        }
        this.toSend.clear();
    }

}
