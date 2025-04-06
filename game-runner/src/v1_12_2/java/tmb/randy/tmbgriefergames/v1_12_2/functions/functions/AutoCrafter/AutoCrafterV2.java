package tmb.randy.tmbgriefergames.v1_12_2.functions.functions.AutoCrafter;

import java.util.LinkedList;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.Phase;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.v1_12_2.functions.Helper;
import tmb.randy.tmbgriefergames.v1_12_2.functions.click.Click;
import tmb.randy.tmbgriefergames.v1_12_2.functions.click.ClickManager;

public class AutoCrafterV2 extends ActiveFunction {
    private enum STATE {
        OPEN_RECEIPTS, OPEN_CRAFT_PAGE, CRAFT,
        OPEN_INVENTORY, DROP_ITEMS,
        GO_BACK, OPEN_COMP, COMP1, COMP2, COMP3, COMP4, COMP5, COMP6, RESTART
    }

    private int cooldown;
    private final LinkedList<Click> toSend = new LinkedList<>();
    private Item itemToCraft;
    private int subIDtoCraft = 0;
    private STATE currentState = STATE.OPEN_RECEIPTS;

    public AutoCrafterV2() {
        super(Functions.CRAFTV2);
    }

    @Override
    public void cbChangedEvent(CbChangedEvent event) {
        stop();
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if(event.phase() == Phase.PRE && isEnabled()) {
            Container cont = Helper.getPlayer().openContainer;

            switch (currentState) {
                case OPEN_RECEIPTS -> {
                    if(cont instanceof ContainerChest chest) {
                        IInventory inv = chest.getLowerChestInventory();
                        if (inv.getName().equalsIgnoreCase("§6Custom-Kategorien")) {
                            click(12);
                        } else if(inv.getName().equalsIgnoreCase("§6Minecraft-Rezepte")) {
                            currentState = STATE.OPEN_CRAFT_PAGE;
                        }
                    } else {
                        Addon.sendCommand("/rezepte");
                    }
                }
                case OPEN_CRAFT_PAGE -> {
                    if(cont instanceof ContainerChest chest) {
                        IInventory inv = chest.getLowerChestInventory();
                        if(inv.getName().equalsIgnoreCase("§6Minecraft-Rezepte")) {
                            if(itemToCraft != null) {
                                if(itemToCraft.equals(Items.GOLD_INGOT)) {
                                    int slot = getSlotForGoldIngot();
                                    if(slot > 0) {
                                        click(slot);
                                    } else {
                                        click(53);
                                    }
                                } else {
                                    int slot = getSlotForItemToCraft();
                                    if(slot >= 54) {
                                        click(slot);
                                    }
                                }
                            }
                        } else if (inv.getName().equalsIgnoreCase("§6Vanilla Bauanleitung")) {
                            currentState = STATE.CRAFT;
                        }
                    }
                }
                case CRAFT -> {
                    if(itemToCraft == null) {
                        itemToCraft = Helper.getPlayer().openContainer.getSlot(25).getStack().getItem();
                        subIDtoCraft = Helper.getPlayer().openContainer.getSlot(25).getStack().getMetadata();
                    }

                    if(getSlotCountOfItemInInventory() >= 27) {
                        switch (Addon.getSharedInstance().configuration().getAutoCrafterConfig().getFinalActionV2().get()) {
                            case COMP -> currentState = STATE.GO_BACK;
                            case DROP -> {
                                Helper.getPlayer().closeScreen();
                                currentState = STATE.OPEN_INVENTORY;
                            }
                        }
                    } else {
                        if(cooldown < 6) {
                            cooldown++;
                            return;
                        }

                        click(52);
                        cooldown = 0;

                    }
                }
                case OPEN_INVENTORY -> {
                    if(Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
                        currentState = STATE.DROP_ITEMS;
                    } else {
                        Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(Helper.getPlayer()));
                    }
                }
                case DROP_ITEMS -> {
                    if(Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
                        if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                            if(getSlotCountOfItemInInventory() <= 1) {
                                Helper.getPlayer().closeScreen();
                                currentState = STATE.OPEN_RECEIPTS;
                            } else {
                                ClickManager.getSharedInstance().dropItemsFromInventory(itemToCraft, subIDtoCraft, true);
                            }
                        }
                    }
                }
                case GO_BACK -> {
                    if(cont instanceof ContainerChest chest) {
                        IInventory inv = chest.getLowerChestInventory();
                        if(inv.getName().equalsIgnoreCase("§6Vanilla Bauanleitung") || inv.getName().equalsIgnoreCase("§6Minecraft-Rezepte")) {
                            if(itemToCraft != null) {
                                click(45);
                            }
                        } else if(inv.getName().equalsIgnoreCase("§6Custom-Kategorien")) {
                            currentState = STATE.OPEN_COMP;
                        }
                    }
                }
                case OPEN_COMP -> {
                    if(cont instanceof ContainerChest chest) {
                        IInventory inv = chest.getLowerChestInventory();
                        if(inv.getName().equalsIgnoreCase("§6Custom-Kategorien")) {
                            click(11);
                        } else if(inv.getName().equalsIgnoreCase("§6Item-Komprimierung-Bauanleitung")) {
                            click(81);
                        } else if(inv.getName().equalsIgnoreCase("§6Item-Komprimierung")) {
                            currentState = STATE.COMP1;
                        }
                    }
                }
                case COMP1, COMP2, COMP3, COMP4, COMP5, COMP6 -> {
                    if(ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                        if(cont instanceof ContainerChest) {
                            String headName = Helper.getPlayer().openContainer.getSlot(49).getStack().getDisplayName();
                            if(headName.contains("§6Komprimierungsstufe")) {
                                int step = Integer.parseInt(headName.replace("§6Komprimierungsstufe ", ""));

                                switch (currentState) {
                                    case COMP1 -> {
                                        if(step > 1)
                                            decreaseStep();
                                        else {
                                            currentState = STATE.COMP2;
                                            click(52);
                                        }
                                    }
                                    case COMP2 -> {
                                        if(step == 2) {
                                            currentState = STATE.COMP3;
                                            click(52);
                                        }
                                        else if (step < 2)
                                            increaseStep();
                                        else
                                            decreaseStep();
                                    }
                                    case COMP3 -> {
                                        if(step == 3) {
                                            currentState = STATE.COMP4;
                                            click(52);
                                        }
                                        else if (step < 3)
                                            increaseStep();
                                        else
                                            decreaseStep();
                                    }
                                    case COMP4 -> {
                                        if(step == 4) {
                                            currentState = STATE.COMP5;
                                            click(52);
                                        }
                                        else if (step < 4)
                                            increaseStep();
                                        else
                                            decreaseStep();
                                    }
                                    case COMP5 -> {
                                        if(step == 5) {
                                            currentState = STATE.COMP6;
                                            click(52);
                                        }
                                        else if (step < 5)
                                            increaseStep();
                                        else
                                            decreaseStep();
                                    }
                                    case COMP6 -> {
                                        if(step == 6) {
                                            click(52);
                                            currentState = STATE.RESTART;
                                        }
                                        else if (step < 6)
                                            increaseStep();
                                        else
                                            decreaseStep();
                                    }
                                }
                            }
                        }
                    }
                }
                case RESTART -> {
                    if(cont instanceof ContainerChest chest) {
                        IInventory inv = chest.getLowerChestInventory();

                        if(inv.getName().equalsIgnoreCase("§6Minecraft-Rezepte")) {
                            currentState = STATE.OPEN_CRAFT_PAGE;
                        } else if(inv.getName().equalsIgnoreCase("§6Custom-Kategorien")) {
                            click(12);
                        } else {
                            click(45);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if(event.state() == State.PRESS && event.key() == Key.ESCAPE && isEnabled()) {
            stop();
        }
    }

    private void increaseStep() {
        click(50);
    }
    private void decreaseStep() {
        click(48);
    }

    @Override
    public boolean start() {
        if(super.start()) {
            if(!Helper.getPlayer().inventory.getStackInSlot(0).isEmpty()) {
                itemToCraft = Helper.getPlayer().inventory.getStackInSlot(0).getItem();
                subIDtoCraft = Helper.getPlayer().inventory.getStackInSlot(0).getMetadata();
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean stop() {
        if(super.stop()) {
            itemToCraft = null;
            subIDtoCraft = 0;
            currentState = STATE.OPEN_RECEIPTS;
            return true;
        }

        return false;
    }

    private void click(int slot) {
        this.toSend.addLast(new Click(Helper.getPlayer().openContainer.windowId, slot, 0, ClickType.QUICK_MOVE));
        ClickManager.getSharedInstance().queueClicks(QueueType.MEDIUM, this.toSend);
        this.toSend.clear();
    }

    private int getSlotCountOfItemInInventory() {
        int count = 0;
        for (ItemStack itemStack : Helper.getPlayer().inventory.mainInventory) {
            if(itemStack.getItem().equals(itemToCraft) && itemStack.getMetadata() == subIDtoCraft) {
                count++;
            }
        }
        return count;
    }

    private int getSlotForItemToCraft() {
        int slotCount = Helper.getPlayer().openContainer.inventorySlots.size();
        for (int i = 54; i < slotCount; i++) {
            ItemStack stack = Helper.getPlayer().openContainer.getSlot(i).getStack();
            if(!stack.isEmpty()) {
                if(stack.getItem().equals(itemToCraft) && stack.getMetadata() == subIDtoCraft) {
                    return i;
                }
            }
        }
        return -1;
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
}