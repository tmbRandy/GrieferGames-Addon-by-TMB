package tmb.randy.tmbgriefergames.v1_12_2.util.click;

import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
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

    public void rightClick(World world, EnumHand hand) {
        Minecraft.getMinecraft().player.getHeldItemMainhand().useItemRightClick(world, Minecraft.getMinecraft().player, hand);
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
        queues.get(QueueType.MEDIUM).add(new Click(Minecraft.getMinecraft().player.openContainer.windowId, slot, 0, ClickType.PICKUP));
        queues.get(QueueType.MEDIUM).add(new Click(Minecraft.getMinecraft().player.openContainer.windowId, -999, 0, ClickType.PICKUP));
    }
}
