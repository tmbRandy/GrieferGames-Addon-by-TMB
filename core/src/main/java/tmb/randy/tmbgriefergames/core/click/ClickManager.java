package tmb.randy.tmbgriefergames.core.click;

import java.util.EnumMap;
import java.util.Map;
import tmb.randy.tmbgriefergames.core.click.Click.Mode;
import tmb.randy.tmbgriefergames.core.enums.QueueType;

/**
 * Version independent click scheduling. Every version only has to tell the manager which
 * window is open, whether clicking is allowed right now and how a click is sent.
 */
public abstract class ClickManager {
    /** Slot id Minecraft uses for clicks outside of the inventory window (drops the held stack). */
    public static final int OUTSIDE_SLOT = -999;

    private static ClickManager sharedInstance;

    private final Map<QueueType, ClickQueue> queues = new EnumMap<>(QueueType.class);

    protected ClickManager() {
        for (QueueType type : QueueType.values())
            queues.put(type, new ClickQueue(type));

        sharedInstance = this;
    }

    public static ClickManager getSharedInstance() {
        return sharedInstance;
    }

    protected abstract int getWindowId();

    protected abstract boolean isClickableScreen();

    protected abstract void sendClick(Click click);

    public void tick() {
        for (ClickQueue queue : queues.values())
            queue.tick(this);
    }

    public void addClick(QueueType queue, Click click) {
        queues.get(queue).add(click);
    }

    public void click(QueueType queue, int slot) {
        addClick(queue, new Click(getWindowId(), slot, Mode.PICKUP));
    }

    public void shiftClick(QueueType queue, int slot) {
        addClick(queue, new Click(getWindowId(), slot, Mode.QUICK_MOVE));
    }

    /** Picks up the stack in the given slot and drops it outside of the window. */
    public void dropClick(int slot) {
        click(QueueType.MEDIUM, slot);
        click(QueueType.MEDIUM, OUTSIDE_SLOT);
    }

    public boolean isClickQueueEmpty(QueueType queue) {
        return queues.get(queue).isEmpty();
    }

    public void clearAllQueues() {
        for (ClickQueue queue : queues.values())
            queue.clear();
    }
}
