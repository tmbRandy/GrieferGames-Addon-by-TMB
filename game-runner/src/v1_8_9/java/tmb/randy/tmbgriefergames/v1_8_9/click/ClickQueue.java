package tmb.randy.tmbgriefergames.v1_8_9.click;

import java.util.LinkedList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

public class ClickQueue {
    private final LinkedList<Click> clickQueue = new LinkedList<>();
    private int clickCooldownCounter = 0;
    private final int clickSpeed;

    public ClickQueue(QueueType type) {
        clickSpeed = switch (type) {
            case FAST -> 1;
            case MEDIUM -> 2;
            case SLOW -> 3;
        };
    }

    public void tick() {
        while (!this.clickQueue.isEmpty() && this.clickCooldownCounter <= 0) {

            if (!isValidScreen()) {
                break;
            }

            Click currClick = this.clickQueue.pop();

            Minecraft.getMinecraft().playerController.windowClick(currClick.windowID(), currClick.slot(), currClick.data(), currClick.action(), Helper.getPlayer());

            this.clickCooldownCounter = clickSpeed;
        }
        if (this.clickCooldownCounter > 0) {
            this.clickCooldownCounter--;
        }
    }

    private boolean isValidScreen() {
        return Minecraft.getMinecraft().currentScreen instanceof GuiChest
            || Minecraft.getMinecraft().currentScreen instanceof GuiCrafting
            || Minecraft.getMinecraft().currentScreen instanceof GuiInventory;
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