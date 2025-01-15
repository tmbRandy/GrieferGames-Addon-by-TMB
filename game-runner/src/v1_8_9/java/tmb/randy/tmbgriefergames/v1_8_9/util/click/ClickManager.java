package tmb.randy.tmbgriefergames.v1_8_9.util.click;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import tmb.randy.tmbgriefergames.core.enums.QueueType;

public class ClickManager {

    private static ClickManager sharedInstance;
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
        if(sharedInstance == null) {
            sharedInstance = new ClickManager();
        }
        return sharedInstance;
    }

    public void dropInventory() {
        Container container = Minecraft.getMinecraft().thePlayer.openContainer;
        processInventory(container, this::dropClick);
    }

    public void dropItemsFromInventory(Item item, int metadata, boolean skipFirst) {
        Container container = Minecraft.getMinecraft().thePlayer.openContainer;
        processInventory(container, i -> {
            if (container.getSlot(i).getStack().getItem().equals(item) && container.getSlot(i).getStack().getMetadata() == metadata) {
                if (!(skipFirst && i == 36)) {
                    dropClick(i);
                }
            }
        });
    }

    private void processInventory(Container container, java.util.function.IntConsumer action) {
        int size = container.inventorySlots.size();
        for (int i = 9; i < size; i++) {
            if (container.getSlot(i).getHasStack()) {
                action.accept(i);
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
        addDropClick(slot);
        addDropClick(-999);
    }

    private void addDropClick(int slot) {
        queues.get(QueueType.MEDIUM).add(new Click(Minecraft.getMinecraft().thePlayer.openContainer.windowId, slot, 0, 0));
    }
}