package tmb.randy.tmbgriefergames.v1_8_9.util;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.Phase;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.v1_8_9.util.click.Click;
import tmb.randy.tmbgriefergames.v1_8_9.util.click.ClickManager;

public class AutoDecomp {

    private boolean autoDecompActive = false;
    private Item compItem;
    private int compSubID;
    private int counter;

    public void onKeyEvent(KeyEvent event) {
        if(Key.ESCAPE.isPressed() && autoDecompActive) {
            stopDecomp();
        } else if (VersionisedBridge.allKeysPressed(Addon.getSharedInstance().configuration().getAutoCrafterConfig().getAutoDecompHotkey().get())) {
            startDecomp();
            Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.autoDecomp.started"));
        }
    }

    public void onTickEvent(GameTickEvent event) {
        if (event.phase() == Phase.PRE && autoDecompActive
            && Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null) {
            counter++;

            if( counter >= 5) {
                decomp();
                counter = 0;
            }
        }
    }

    private void decomp() {
        if (Minecraft.getMinecraft().thePlayer.openContainer != null
            && Minecraft.getMinecraft().currentScreen instanceof GuiCrafting) {
            Container container = Minecraft.getMinecraft().thePlayer.openContainer;

            if (ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                if (compItem == null) {
                    if (Minecraft.getMinecraft().thePlayer.inventory.mainInventory[0] != null && Minecraft.getMinecraft().thePlayer.inventory.mainInventory[0].stackSize > 0) {
                        compItem = Minecraft.getMinecraft().thePlayer.inventory.mainInventory[0].getItem();
                        compSubID = Minecraft.getMinecraft().thePlayer.inventory.mainInventory[0].getMetadata();
                    }
                } else {
                    //Do Decomp

                    if (container.getSlot(0).getHasStack()) {
                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM,
                            new Click(container.windowId, 0, 0, 1));
                    } else if (doesCraftingFieldContainItems()) {
                        for (int i = 1; i < 10; i++) {
                            if (container.getSlot(i).getHasStack()) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM,
                                    new Click(container.windowId, i, 0, 1));
                            }
                        }
                    } else {
                        int[] compSteps = getCompCountForItem();
                        if (compSteps[0] > 0) {
                            dropUncompressedItems();
                        } else if (compSteps[1] > 0) {
                            int slot = getBestSlotForCompStep(1);
                            if (slot >= 10) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, slot, 0, 0));
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, 5, 0, 0));
                            }
                        } else if (compSteps[2] > 0) {
                            int slot = getBestSlotForCompStep(2);
                            if (slot >= 10) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, slot, 0, 0));
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, 5, 0, 0));
                            }
                        } else if (compSteps[3] > 0) {
                            int slot = getBestSlotForCompStep(3);
                            if (slot >= 10) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, slot, 0, 0));
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, 5, 0, 0));
                            }
                        } else if (compSteps[4] > 0) {
                            int slot = getBestSlotForCompStep(4);
                            if (slot >= 10) {
                                ClickManager.getSharedInstance().addClick(
                                    QueueType.MEDIUM, new Click(container.windowId, slot, 0, 0));
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, 5, 0, 0));
                            }
                        } else if (compSteps[5] > 0) {
                            int slot = getBestSlotForCompStep(5);
                            if (slot >= 10) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, slot, 0, 0));
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, 5, 0, 0));
                            }
                        } else if (compSteps[6] > 0) {
                            int slot = getBestSlotForCompStep(6);
                            if (slot >= 10) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, slot, 0, 0));
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, 5, 0, 0));
                            }
                        } else if (compSteps[7] > 0) {
                            int slot = getBestSlotForCompStep(7);
                            if (slot >= 10) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, slot, 0, 0));
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(container.windowId, 5, 0, 0));
                            }
                        }
                    }
                }
            }
        } else {
            if (VersionisedBridge.canSendCommand()) {
                VersionisedBridge.sendCommand("/craft");
            }
        }
    }

    public void startDecomp() {
        autoDecompActive = true;
    }

    public void stopDecomp() {
        autoDecompActive = false;
        compItem = null;
        compSubID = 0;
    }

    private int[] getCompCountForItem() {
        int[] output = new int[8];

        for (ItemStack itemStack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
            if (itemStack != null && itemStack.stackSize > 0) {
                if (itemStack.getItem().equals(compItem) && itemStack.getMetadata() == compSubID) {
                    if (itemStack.hasDisplayName()) {
                        if (itemStack.getDisplayName().endsWith(" VII")) {
                            output[7]++;
                        } else if (itemStack.getDisplayName().endsWith(" VI")) {
                            output[6]++;
                        } else if (itemStack.getDisplayName().endsWith(" V")) {
                            output[5]++;
                        } else if (itemStack.getDisplayName().endsWith(" IV")) {
                            output[4]++;
                        } else if (itemStack.getDisplayName().endsWith(" III")) {
                            output[3]++;
                        } else if (itemStack.getDisplayName().endsWith(" II")) {
                            output[2]++;
                        } else if (itemStack.getDisplayName().endsWith(" I")) {
                            output[1]++;
                        } else {
                            output[0]++;
                        }
                    } else {
                        output[0]++;
                    }
                }
            }
        }
        return output;
    }

    private boolean doesCraftingFieldContainItems() {
        if (Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerWorkbench) {
            for (int i = 1; i < 10; i++) {
                if (Minecraft.getMinecraft().thePlayer.openContainer.getSlot(i).getHasStack()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void dropUncompressedItems() {
        if (Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerWorkbench) {
            for (int i = 10; i < 46; i++) {
                if (Minecraft.getMinecraft().thePlayer.openContainer.getSlot(i).getHasStack()) {
                    if (Minecraft.getMinecraft().thePlayer.openContainer.getSlot(i).getStack().getItem().equals(compItem) && Minecraft.getMinecraft().thePlayer.openContainer.getSlot(i).getStack().getMetadata() == compSubID) {
                        if (!Minecraft.getMinecraft().thePlayer.openContainer.getSlot(i).getStack()
                            .hasDisplayName()) {
                            ClickManager.getSharedInstance().dropClick(i);
                        }
                    }
                }
            }
        }
    }

    private int getBestSlotForCompStep(int step) {
        String romanian = getRomanianNumberForInt(step);
        int lowestSlot = 0;
        ItemStack lowestItemStack = null;

        if (Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerWorkbench) {
            for (int i = 10; i < Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.size(); i++) {
                if (Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.get(i).getHasStack()) {
                    if (Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.get(i).getStack().getItem().equals(compItem) && Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.get(i).getStack().getMetadata() == compSubID) {
                        if (Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.get(i)
                            .getStack().hasDisplayName()) {
                            if (Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.get(i)
                                .getStack().getDisplayName().endsWith(" " + romanian)) {
                                if (lowestItemStack == null) {
                                    lowestItemStack = Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.get(
                                        i).getStack();
                                    lowestSlot = i;
                                } else if (lowestItemStack.stackSize
                                    > Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.get(
                                    i).getStack().stackSize) {
                                    lowestItemStack = Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.get(
                                        i).getStack();
                                    lowestSlot = i;
                                }
                            }
                        }
                    }
                }
            }
        }

        return lowestSlot;
    }

    private String getRomanianNumberForInt(int number) {
        switch (number) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            default:
                return "";
        }
    }
}