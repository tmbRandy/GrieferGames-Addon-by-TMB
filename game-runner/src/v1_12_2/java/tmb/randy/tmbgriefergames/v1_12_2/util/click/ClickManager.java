package tmb.randy.tmbgriefergames.v1_12_2.util.click;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ClickManager {

    private static ClickManager SharedInstance;
    private final Map<QueueType, ClickQueue> queues = new HashMap<>();

    private ClickManager() {
        queues.put(QueueType.SLOW, new ClickQueue(QueueType.SLOW));
        queues.put(QueueType.MEDIUM, new ClickQueue(QueueType.MEDIUM));
        queues.put(QueueType.FAST, new ClickQueue(QueueType.FAST));
    }

    public void tick() {
        for (Map.Entry<QueueType, ClickQueue> entry : queues.entrySet()) {
            entry.getValue().tick();
        }
    }

    public static ClickManager getSharedInstance() {
        if(SharedInstance == null) {
            SharedInstance = new ClickManager();
        }
        return SharedInstance;
    }

    public void dropInventory() {
        Container container = Minecraft.getMinecraft().player.openContainer;
        int size = container.inventorySlots.size();
        for(int i = 9; i < size; i++) {
            if(container.getSlot(i).getHasStack()) {
                dropClick(i);
            }

        }
    }

    public void dropItemsFromInventory(Item item, int metadata, boolean skipFirst) {
        Container container = Minecraft.getMinecraft().player.openContainer;
        int size = container.inventorySlots.size();
        for(int i = 9; i < size; i++) {
            if(container.getSlot(i).getHasStack()) {
                if(container.getSlot(i).getStack().getItem().equals(item) && container.getSlot(i).getStack().getMetadata() == metadata) {
                    if(skipFirst && i == 36) {
                        continue;
                    }
                    dropClick(i);
                }
            }
        }
    }

    public void queueClicks(QueueType queueType, LinkedList<Click> queue)
    {
        queues.get(queueType).queueClicks(queue);
    }

    public void clearAllQueues() {
        for (Map.Entry<QueueType, ClickQueue> entry : queues.entrySet()) {
            entry.getValue().clearQueue();
        }
    }

    public boolean isClickQueueEmpty(QueueType queue) {
        return queues.get(queue).isClickQueueEmpty();
    }

    public void addClick(QueueType queue, Click click) {
        queues.get(queue).add(click);
    }

    public void dropClick(int slot)
    {
        queues.get(QueueType.MEDIUM).add(new Click(Minecraft.getMinecraft().player.openContainer.windowId, slot, 0, ClickType.PICKUP));
        queues.get(QueueType.MEDIUM).add(new Click(Minecraft.getMinecraft().player.openContainer.windowId, -999, 0, ClickType.PICKUP));
    }
}
