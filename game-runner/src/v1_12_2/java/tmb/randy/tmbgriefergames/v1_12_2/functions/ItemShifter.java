package tmb.randy.tmbgriefergames.v1_12_2.functions;

import java.util.LinkedList;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;
import tmb.randy.tmbgriefergames.v1_12_2.click.Click;
import tmb.randy.tmbgriefergames.v1_12_2.click.ClickManager;

public class ItemShifter extends Function {

    private String itemToMove = "";
    private int idToMove = 0;

    private ContainerChest currentChest;
    private boolean topToBottom = true;

    private final LinkedList<Click> toSend = new LinkedList<>();

    private final int[] spawnerMenuItemIDs = new int[] {160, 384, 347, 76, 54};

    private static ItemShifter sharedInstance;

    public ItemShifter() {
        super(Functions.ITEMSHIFTER);
    }

    public static ItemShifter getSharedInsance() {
        if(sharedInstance == null) {
            sharedInstance = new ItemShifter();
        }
        return sharedInstance;
    }

    public void stopShifting() {
        itemToMove = "";
        idToMove = 0;
        currentChest = null;
        topToBottom = true;
    }

    public void startShifting() {
        this.currentChest = (ContainerChest) Helper.getPlayer().openContainer;

        if(currentChest.getLowerChestInventory().getName().equals("§6Wähle deine Komprimierung") && topToBottom) {
            outerLoop : for (int i = 16; i >= 10 ; i--) {
                ItemStack stack = currentChest.getLowerChestInventory().getStackInSlot(i);
                if(stack.hasTagCompound() && stack.getTagCompound() != null) {
                    NBTTagCompound display = stack.getTagCompound().getCompoundTag("display");
                    NBTTagList lore = display.getTagList("Lore", 8);

                    if(lore.tagCount() > 0) {
                        for (int j = 0; j < lore.tagCount(); j++) {
                            String string = lore.getStringTagAt(j);

                            if(string.startsWith("§e") && string.endsWith(" Verfügbar")) {
                                int count = Integer.parseInt(string.replace("§e", "").replace(" Verfügbar", ""). replace(".", ""));
                                if(count > 0) {
                                    for (int k = 0; k < count; k++) {
                                        shiftClick(i);
                                    }
                                    sendQueue();
                                    break outerLoop;
                                }
                            }
                        }
                    }
                }
            }
        } else if(currentChest.getLowerChestInventory().getName().equals(Helper.getPlayer().inventory.mainInventory.getFirst().getDisplayName()) && topToBottom) {
            if(Helper.isInventoryFull())
                return;
            shiftClick(11);
            sendQueue();
        } else if(currentChest.getLowerChestInventory().getName().equals("§6spezielle Items") && topToBottom) {
            if(Helper.isInventoryFull())
                return;
            for (int i = 0; i < currentChest.getLowerChestInventory().getSizeInventory(); i++) {
                ItemStack stack = currentChest.getLowerChestInventory().getStackInSlot(i);
                if(stack != null && !stack.isEmpty()) {
                    if(stack.hasDisplayName() && stack.getDisplayName().equals(Helper.getPlayer().inventory.mainInventory.getFirst().getDisplayName())) {
                        shiftClick(i);
                        sendQueue();
                        break;
                    }
                }
            }
        } else {
            this.itemToMove = Helper.getPlayer().getHeldItemMainhand().getDisplayName();

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

                int currentItemID = Item.getIdFromItem(fromSlot.getStack().getItem());
                String currentItemName = fromSlot.getStack().getDisplayName();

                if(fromSlot.getHasStack() && availableSlots > 0) {
                    if(!(isSpawner && ArrayUtils.contains(spawnerMenuItemIDs, currentItemID))) {
                        if(this.idToMove == 0 || (this.idToMove == currentItemID && this.itemToMove.equals(currentItemName))) {
                            this.shiftClick(from);
                            availableSlots -= 1;
                        }
                    }
                }
            }

            this.idToMove = 0;
            this.itemToMove = "";
            this.sendQueue();
        }
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if((Keyboard.isKeyDown(Key.ARROW_LEFT.getId()) && Keyboard.isKeyDown(Key.ARROW_UP.getId()) && Keyboard.isKeyDown(Key.ARROW_RIGHT.getId())) || !(Helper.getPlayer().openContainer instanceof ContainerChest chest))
            return;

        if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
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
            } else if (Keyboard.isKeyDown(Key.ARROW_DOWN.getId()) && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.SLOW) && !Addon.getSharedInstance().getActiveFunction(Functions.COMP).isEnabled()) {
                setTopToBottom(true);
                startShifting();
            }
        }
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if ((event.key() == Key.ARROW_UP || event.key() == Key.ARROW_LEFT || event.key() == Key.ARROW_RIGHT) &&
            Keyboard.isKeyDown(Key.ARROW_UP.getId()) && Keyboard.isKeyDown(Key.ARROW_LEFT.getId()) && Keyboard.isKeyDown(Key.ARROW_RIGHT.getId()) &&
            Helper.getPlayer().openContainer != null && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM) && Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
            ClickManager.getSharedInstance().dropInventory();
        }
    }

    private boolean checkIfContainerIsSpawner() {
        if(Helper.getPlayer().openContainer == null)
            return false;

        IInventory inv = ((ContainerChest) Helper.getPlayer().openContainer).getLowerChestInventory();
        return inv.getName().equalsIgnoreCase("§6Spawner - Lager");
    }

    private void shiftClick(int slot) {
        this.toSend.addLast(new Click(Helper.getPlayer().openContainer.windowId, slot, 0, ClickType.QUICK_MOVE));
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
                Minecraft.getMinecraft().playerController.windowClick(this.currentChest.windowId, slot, 0, ClickType.PICKUP, Helper.getPlayer());
                ItemStack stack = (this.currentChest.inventorySlots.get(slot)).getStack();

                this.idToMove = Item.getIdFromItem(stack.getItem());
                this.itemToMove = stack.getDisplayName();

                return;
            }
        }

        //Find empty slot in Destination

        for(int slot = fromMin; slot <= fromMax; slot++) {
            if (!(this.currentChest.inventorySlots.get(slot)).getHasStack())
            {
                Minecraft.getMinecraft().playerController.windowClick(this.currentChest.windowId, slot, 0, ClickType.PICKUP, Helper.getPlayer());
                ItemStack stack = (this.currentChest.inventorySlots.get(slot)).getStack();

                this.idToMove = Item.getIdFromItem(stack.getItem());
                this.itemToMove = stack.getDisplayName();

                return;
            }
        }

    }

    public void setTopToBottom(boolean topToBottom) {
        this.topToBottom = topToBottom;
    }

    private void sendQueue() {
        ClickManager.getSharedInstance().queueClicks(QueueType.SLOW, toSend);
        this.toSend.clear();
    }

}