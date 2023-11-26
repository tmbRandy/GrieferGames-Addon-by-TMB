package tmb.randy.tmbgriefergames.v1_8_9.util.click;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import tmb.randy.tmbgriefergames.core.enums.QueueType;

public class ClickManager {

    private static ClickManager SharedInstance;
    private final Map<QueueType, ClickQueue> queues = new HashMap<>();

    private ClickManager() {
        queues.put(QueueType.SLOW, new ClickQueue(QueueType.SLOW));
        queues.put(QueueType.MEDIUM, new ClickQueue(QueueType.MEDIUM));
        queues.put(QueueType.FAST, new ClickQueue(QueueType.FAST));
    }

    public void tick(GameTickEvent event) {
        for (Map.Entry<QueueType, ClickQueue> entry : queues.entrySet()) {
            entry.getValue().tick(event);
        }
    }

    public static ClickManager getSharedInstance() {
        if(SharedInstance == null) {
            SharedInstance = new ClickManager();
        }
        return SharedInstance;
    }

    public void rightClick(World world) {
        Minecraft.getMinecraft().thePlayer.getHeldItem().useItemRightClick(world, Minecraft.getMinecraft().thePlayer);
    }

    public void dropInventory() {
        Container container = Minecraft.getMinecraft().thePlayer.openContainer;
        int size = container.inventorySlots.size();
        for(int i = 9; i < size; i++) {
            if(container.getSlot(i).getHasStack()) {
                dropClick(i);
            }

        }
    }

    public void queueClicks(QueueType queueType, LinkedList<Click> queue)
    {
        queues.get(queueType).queueClicks(queue);
    }

    public void clearQueue(QueueType queue) {
        queues.get(queue).clearQueue();
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
        queues.get(QueueType.MEDIUM).add(new Click(Minecraft.getMinecraft().thePlayer.openContainer.windowId, slot, 0, 0));
        queues.get(QueueType.MEDIUM).add(new Click(Minecraft.getMinecraft().thePlayer.openContainer.windowId, -999, 0, 0));
    }
}
