package tmb.randy.tmbgriefergames.v1_12_2.util.click;

import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ClickType;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import java.util.LinkedList;

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
                Minecraft.getMinecraft().player);

            this.clickCooldownCounter = clickSpeed;
        }
        if (this.clickCooldownCounter > 0) {
            this.clickCooldownCounter--;
        }
    }

    private void dropClick(int slot)
    {
        this.clickQueue.addLast(new Click(Minecraft.getMinecraft().player.openContainer.windowId, slot, 0, ClickType.PICKUP));
        this.clickQueue.addLast(new Click(Minecraft.getMinecraft().player.openContainer.windowId, -999, 0, ClickType.PICKUP));
    }

    private void shiftClick(int slot) {
        this.clickQueue.add(new Click(Minecraft.getMinecraft().player.openContainer.windowId, slot, 0, ClickType.QUICK_MOVE));
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

