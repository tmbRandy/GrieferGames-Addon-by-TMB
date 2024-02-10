package tmb.randy.tmbgriefergames.v1_8_9.util;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.v1_8_9.util.click.Click;
import tmb.randy.tmbgriefergames.v1_8_9.util.click.ClickManager;
import java.util.HashMap;
import java.util.LinkedList;

public class AutoCrafter
{
    private ContainerWorkbench inv;
    private int[] stored;
    private int[] meta;
    private String[] names;
    private String output;
    private Item outputItem;
    private LinkedList<Click> toSend;

    private Simulator simulator;
    private boolean endlessModeToggle = false;

    public AutoCrafter()
    {
        this.stored = new int[9];
        this.meta = new int[9];
        this.names = new String[9];
        this.output = "";
        this.toSend = new LinkedList<>();
    }


    public void onKeyEvent(KeyEvent event) {

        if(Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.openContainer != null && Minecraft.getMinecraft().currentScreen instanceof GuiCrafting) {
            if(event.key() == Key.ENTER) {
                if(Key.L_SHIFT.isPressed()) {
                    storeCrafting();
                    Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.autoCrafter.recipeSaved"));
                } else if(Addon.getSharedInstance().configuration().getAutoCrafterConfig().getEndlessMode().get() && event.state() == State.PRESS) {
                    this.setEndlessModeToggle(!endlessModeToggle);
                } else if(!Addon.getSharedInstance().configuration().getAutoCrafterConfig().getEndlessMode().get() && ClickManager.getSharedInstance().isClickQueueEmpty(getCraftingSpeed())) {
                    craft();
                }
            }
        }
    }

    public void onTickEvent(GameTickEvent event) {
        if(endlessModeToggle && ClickManager.getSharedInstance().isClickQueueEmpty(getCraftingSpeed())) {
            craft();
        }
    }

    public void storeCrafting()
    {
        this.inv = (ContainerWorkbench)Minecraft.getMinecraft().thePlayer.openContainer;

        if (!(this.inv.inventorySlots.get(0)).getHasStack())
        {
            return;
        }
        ItemStack result = (this.inv.inventorySlots.get(0)).getStack();
        for (int i = 0; i < 9; i++)
        {
            if (this.inv.inventorySlots.get(i+1) != null)
            {
                ItemStack stack = (this.inv.inventorySlots.get(i+1)).getStack();
                if (stack != null) {
                    this.stored[i] = Item.getIdFromItem(stack.getItem());
                    this.meta[i] = stack.getItemDamage();
                    this.names[i] = stack.getDisplayName();
                } else {
                    this.stored[i] = 0;
                    this.meta[i] = 0;
                    this.names[i] = "";
                }
            }
        }
        this.output = result.getDisplayName();
        this.outputItem = result.getItem();
    }

    public void craft()
    {
        if(!(Minecraft.getMinecraft().thePlayer.openContainer instanceof  ContainerWorkbench)) {
            return;
        }

        this.toSend.clear();
        this.inv = (ContainerWorkbench)Minecraft.getMinecraft().thePlayer.openContainer;

        int n = 0;
        for (int i = 0; i < 9; i++) n += this.stored[i];
        if (n == 0)	return;

        this.depositHeld();

        this.simulator = new Simulator(this.inv.inventorySlots, 46);

        dropItems();
        this.simulator = new Simulator(this.inv.inventorySlots, 46);


        if (!this.checkMaterials())
            return;


        for (int i = 0; i < 9; i++) {
            ItemStack stack = this.simulator.stackAt(i+1);
            if (stack != null) {
                int currID = Item.getIdFromItem(stack.getItem());
                int currMeta = stack.getItemDamage();
                String name = stack.getDisplayName();
                if (currID == stored[i] && currMeta == meta[i] && name.equals(names[i]))
                    continue;
                else if (stored[i] == 0)
                {
                    this.shiftClick(i+1);
                    continue;
                }
                else
                    this.shiftClick(i+1);
            }

            if (stored[i] == 0)
                continue;
            boolean found = false;
            for (int j = 45; j >= 10; j--)
            {
                ItemStack curr = this.simulator.stackAt(j);

                if(curr == null)
                    continue;

                String name = curr.getDisplayName();

                boolean isFullStack = curr.stackSize == curr.getMaxStackSize() || !Addon.getSharedInstance().configuration().getAutoCrafterConfig().getOnlyFullStacks().get();
                if (curr != null && Item.getIdFromItem(curr.getItem()) == stored[i] && curr.getItemDamage() == meta[i] && name.equals(names[i]) && isFullStack) {
                    this.click(j);
                    this.click(i+1);
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                int amount = 0;
                int slot = -1;
                for (int j = 1; j <=9; j++)
                {
                    ItemStack curr = this.simulator.stackAt(j);

                    if (curr != null && Item.getIdFromItem(curr.getItem()) == stored[i] && curr.getItemDamage() == meta[i] && curr.stackSize > amount)
                    {
                        String name = curr.getDisplayName();
                        if(name.equals(names.equals(names[i]))) {
                            amount = curr.stackSize;
                            slot = j;
                            found = true;
                        }
                    }
                }
                if (found)
                {
                    this.rightClick(slot);
                    this.click(i+1);
                }
                if (!found)
                {
                    ItemStack displayStack = new ItemStack(Item.getItemById(stored[i]));
                    displayStack.setItemDamage(meta[i]);
                    this.sendQueue();
                    return;
                }
            }
        }

        this.shiftClick(0);
        this.sendQueue();


    }

    private void depositHeld() {
        for (int i = 10; i < 46; i++) {
            if (!((Slot)this.inv.inventorySlots.get(i)).getHasStack())
            {
                Minecraft.getMinecraft().playerController.windowClick(this.inv.windowId, i, 0, 0, Minecraft.getMinecraft().thePlayer);
                return;
            }
        }


        for (int i = 1; i < 10; i++) {
            if (!((Slot)this.inv.inventorySlots.get(i)).getHasStack())
            {
                Minecraft.getMinecraft().playerController.windowClick(this.inv.windowId,
                    i, 0, 0, Minecraft.getMinecraft().thePlayer);
                return;
            }
        }


        for (int i = 10; i < 46; i++) {
            if (ArrayUtils.contains(stored, Item.getIdFromItem(this.inv.inventorySlots.get(i).getStack().getItem())))
            {
                Minecraft.getMinecraft().playerController.windowClick(this.inv.windowId, i, 0, 0, Minecraft.getMinecraft().thePlayer);
                Minecraft.getMinecraft().playerController.windowClick(this.inv.windowId, -999, 0, 0, Minecraft.getMinecraft().thePlayer);
                return;
            }
        }


    }

    private boolean checkMaterials()
    {
        HashMap<String, Integer> needed = new HashMap<String, Integer>();
        for (int i = 0; i < 9; i++) {
            if (this.stored[i] != 0) {
                Integer count = needed.get(stored[i] + ":" + meta[i]);
                if (count == null)
                    needed.put(stored[i] + ":" + meta[i], 1);
                else
                    needed.put(stored[i] + ":" + meta[i], count + 1);
            }
        }
        for (int i = 1; i <= 45; i++) {
            this.inv = (ContainerWorkbench)Minecraft.getMinecraft().thePlayer.openContainer;
            if (this.simulator.stackAt(i) == null)
                continue;
            ItemStack stack = this.simulator.stackAt(i);
            if (stack == null)
                continue;
            String item = Item.getIdFromItem(stack.getItem()) + ":" + stack.getItemDamage();
            Integer count = needed.get(item);
            if (count == null)
                continue;
            else
            {
                if (stack.stackSize >= count)
                    needed.remove(item);
                else
                    needed.put(item, count - stack.stackSize);
            }
            if (needed.size() == 0)
                break;
        }

        if (needed.size() > 0) {
            String item = needed.keySet().iterator().next();
            ItemStack displayStack = new ItemStack(Item.getItemById(Integer.parseInt(item.split(":")[0])));
            displayStack.setItemDamage(Integer.parseInt(item.split(":")[1]));
            this.toSend.clear();
            for (int k = 1; k < 10; k++)
                if (this.simulator.stackAt(k) != null)
                    this.shiftClick(k);
            this.sendQueue();
            return false;
        }
        return true;
    }

    private void shiftClick(int slot) {
        this.toSend.addLast(new Click(this.inv.windowId, slot, 0, 1));
        this.simulator.shiftClick(slot);
    }

    private void dropItems() {

        if(Addon.getSharedInstance().configuration().getAutoCrafterConfig().getAutoDrop().get()) {
            for (int j = 10; j <= 45; j++) {
                ItemStack curr = this.simulator.stackAt(j);

                if(curr == null)
                    continue;

                Item currentItem = curr.getItem();

                if (curr.getDisplayName().equals(output) && currentItem.equals(outputItem)) {
                    dropClick(j);
                }
            }
        }
    }

    private void dropClick(int slot) {
        this.toSend.addLast(new Click(this.inv.windowId, slot, 0, 0));
        this.toSend.addLast(new Click(this.inv.windowId, -999, 0, 0));
    }

    private void click(int slot) {
        this.toSend.addLast(new Click(this.inv.windowId, slot, 0, 0));
        this.simulator.leftClick(slot);
    }

    private void rightClick(int slot) {
        this.toSend.addLast(new Click(this.inv.windowId, slot, 1, 0));
        this.simulator.rightClick(slot);
    }

    private void sendQueue() {
        ClickManager.getSharedInstance().queueClicks(getCraftingSpeed(), toSend);
        this.toSend.clear();
    }

    private void setEndlessModeToggle(boolean value) {
        endlessModeToggle = value;
        if(value) {
            Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.autoCrafter.active"));
        } else {
            Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.autoCrafter.inactive"));
        }
    }

    private QueueType getCraftingSpeed() {
        return Addon.getSharedInstance().configuration().getAutoCrafterConfig().getAutoCraftSpeed().get();
    }
}