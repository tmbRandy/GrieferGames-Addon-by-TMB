package tmb.randy.tmbgriefergames.v1_8_9.functions.AutoCrafter;

import java.util.HashMap;
import java.util.LinkedList;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.helper.I19n;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;
import tmb.randy.tmbgriefergames.v1_8_9.click.Click;
import tmb.randy.tmbgriefergames.v1_8_9.click.ClickManager;

public class AutoCrafterV1 extends Function {
    private ContainerWorkbench inv;
    private final int[] stored;
    private final int[] meta;
    private final String[] names;
    private String output;
    private int outputID;
    private final LinkedList<Click> toSend;

    private Simulator simulator;
    private boolean endlessModeToggle = false;

    public AutoCrafterV1() {
        super(Functions.CRAFTV1);
        this.stored = new int[9];
        this.meta = new int[9];
        this.names = new String[9];
        this.output = "";
        this.toSend = new LinkedList<>();
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if(Helper.getPlayer() != null && Helper.getPlayer().openContainer != null && Minecraft.getMinecraft().currentScreen instanceof GuiCrafting) {
            if(event.key() == Key.ENTER && event.state() == State.PRESS) {
                if(Key.L_SHIFT.isPressed()) {
                    storeCrafting();
                    Addon.getSharedInstance().displayNotification(I19n.translate("autoCrafter.recipeSaved"));
                } else if(Addon.getSharedInstance().configuration().getAutoCrafterConfig().getEndlessMode().get() && event.state() == State.PRESS) {
                    this.setEndlessModeToggle(!endlessModeToggle);
                } else if(!Addon.getSharedInstance().configuration().getAutoCrafterConfig().getEndlessMode().get() && ClickManager.getSharedInstance().isClickQueueEmpty(getCraftingSpeed())) {
                    craft();
                }
            }
        }
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if(endlessModeToggle && ClickManager.getSharedInstance().isClickQueueEmpty(getCraftingSpeed()))
            craft();
    }

    public void storeCrafting() {
        this.inv = (ContainerWorkbench)Helper.getPlayer().openContainer;

        Slot resultSlot = this.inv.getSlot(0);
        if (!resultSlot.getHasStack())
        {
            return;
        }
        ItemStack result = resultSlot.getStack();
        for (int i = 0; i < 9; i++)
        {
            if (this.inv.getSlot(i+1) != null)
            {
                ItemStack stack = this.inv.getSlot(i+1).getStack();
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
        this.outputID = Item.getIdFromItem(result.getItem());
    }

    public void craft()
    {
        if(!(Helper.getPlayer().openContainer instanceof ContainerWorkbench)) {
            return;
        }

        this.toSend.clear();
        this.inv = (ContainerWorkbench)Helper.getPlayer().openContainer;

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
            for (int j = 45; j >= 10; j--) {
                ItemStack curr = this.simulator.stackAt(j);
                if (curr == null) continue;
                String name = curr.getDisplayName();
                boolean isFullStack = curr.stackSize == curr.getMaxStackSize() || !Addon.getSharedInstance().configuration().getAutoCrafterConfig().getOnlyFullStacks().get();
                if (Item.getIdFromItem(curr.getItem()) == stored[i] && curr.getItemDamage() == meta[i] && name.equals(names[i]) && isFullStack) {
                    this.click(j);
                    this.click(i+1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                ItemStack displayStack = new ItemStack(Item.getItemById(stored[i]));
                displayStack.setItemDamage(meta[i]);
                this.sendQueue();
                return;
            }
        }

        this.shiftClick(0);
        this.sendQueue();
    }

    private void depositHeld() {
        for (int i = 10; i < 46; i++) {
            if (!this.inv.getSlot(i).getHasStack())
            {
                Minecraft.getMinecraft().playerController.windowClick(this.inv.windowId, i, 0, 0, Helper.getPlayer());
                return;
            }
        }

        for (int i = 1; i < 10; i++) {
            if (!this.inv.getSlot(i).getHasStack())
            {
                Minecraft.getMinecraft().playerController.windowClick(this.inv.windowId, i, 0, 0, Helper.getPlayer());
                return;
            }
        }

        for (int i = 10; i < 46; i++) {
            if (ArrayUtils.contains(stored, Item.getIdFromItem(this.inv.getSlot(i).getStack().getItem())))
            {
                Minecraft.getMinecraft().playerController.windowClick(this.inv.windowId, i, 0, 0, Helper.getPlayer());
                Minecraft.getMinecraft().playerController.windowClick(this.inv.windowId, -999, 0, 0, Helper.getPlayer());
                return;
            }
        }
    }

    private boolean checkMaterials() {
        HashMap<String, Integer> needed = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            if (this.stored[i] != 0) {
                needed.merge(stored[i] + ":" + meta[i], 1, Integer::sum);
            }
        }
        for (int i = 1; i <= 45; i++) {
            this.inv = (ContainerWorkbench)Helper.getPlayer().openContainer;
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
            if (needed.isEmpty())
                break;
        }

        if (!needed.isEmpty()) {
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
        this.toSend.addLast(new Click(this.inv.windowId, slot, 0, 1)); // 1 = SHIFT_CLICK in 1.8.9
        this.simulator.shiftClick(slot);
    }

    private void dropItems() {
        if(Addon.getSharedInstance().configuration().getAutoCrafterConfig().getAutoDrop().get()) {
            for (int j = 10; j <= 45; j++) {
                ItemStack curr = this.simulator.stackAt(j);
                if (curr == null) continue;
                int currentID = Item.getIdFromItem(curr.getItem());

                if (curr.getDisplayName().equals(output) && currentID == outputID) {
                    dropClick(j);
                }
            }
        }
    }

    private void dropClick(int slot) {
        this.toSend.addLast(new Click(this.inv.windowId, slot, 0, 0)); // 0 = PICKUP in 1.8.9
        this.toSend.addLast(new Click(this.inv.windowId, -999, 0, 0));
    }

    private void click(int slot) {
        this.toSend.addLast(new Click(this.inv.windowId, slot, 0, 0)); // 0 = PICKUP in 1.8.9
        this.simulator.leftClick(slot);
    }

    private void sendQueue() {
        ClickManager.getSharedInstance().queueClicks(getCraftingSpeed(), toSend);
        this.toSend.clear();
    }

    private void setEndlessModeToggle(boolean value) {
        endlessModeToggle = value;
        Addon.getSharedInstance().displayNotification(I19n.translate(value ? "autoCrafter.active" : "autoCrafter.inactive"));
    }

    private QueueType getCraftingSpeed() {
        return Addon.getSharedInstance().configuration().getAutoCrafterConfig().getAutoCraftSpeed().get();
    }
}