package tmb.randy.tmbgriefergames.v1_8_9.util.click;

import java.util.LinkedList;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import tmb.randy.tmbgriefergames.core.enums.QueueType;

public class ClickQueue {
    private final LinkedList<Click> clickQueue = new LinkedList<>();
    private int clickCooldownCounter = 0;
    private int clickSpeed;

    private ClickQueue() {}

    public ClickQueue(QueueType type) {
        clickSpeed = switch (type) {
            case FAST -> 1;
            case MEDIUM -> 2;
            case SLOW -> 3;
        };
    }

    public void tick(GameTickEvent event) {
        while (!this.clickQueue.isEmpty() && this.clickCooldownCounter <= 0) {

            if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest
                || Minecraft.getMinecraft().currentScreen instanceof GuiCrafting
                || Minecraft.getMinecraft().currentScreen instanceof GuiInventory)) {
                break;
            }

            Click currClick = this.clickQueue.pop();

            Minecraft.getMinecraft().playerController.windowClick(currClick.windowID(),
                currClick.slot(), currClick.data(), currClick.action(),
                Minecraft.getMinecraft().thePlayer);

            this.clickCooldownCounter = clickSpeed;
        }
        if (this.clickCooldownCounter > 0) {
            this.clickCooldownCounter--;
        }
    }

    private void dropClick(int slot)
    {
        this.clickQueue.addLast(new Click(Minecraft.getMinecraft().thePlayer.openContainer.windowId, slot, 0, 0));
        this.clickQueue.addLast(new Click(Minecraft.getMinecraft().thePlayer.openContainer.windowId, -999, 0, 0));
    }

    private void shiftClick(int slot) {
        this.clickQueue.add(new Click(Minecraft.getMinecraft().thePlayer.openContainer.windowId, slot, 0, 1));
    }

    public void add(Click click) {
        this.clickQueue.add(click);
    }

    public void queueClicks(LinkedList<Click> queue)
    {
        this.clickQueue.addAll(queue);
    }

    public void clearQueue() {
        this.clickQueue.clear();
    }

    public boolean isClickQueueEmpty() {
        return clickQueue.isEmpty();
    }
}

