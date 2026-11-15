package tmb.randy.tmbgriefergames.core.click;

import java.util.ArrayDeque;
import java.util.Deque;
import tmb.randy.tmbgriefergames.core.enums.QueueType;

final class ClickQueue {
    private final Deque<Click> clicks = new ArrayDeque<>();
    private final int ticksBetweenClicks;
    private int cooldown = 0;

    ClickQueue(QueueType type) {
        ticksBetweenClicks = switch (type) {
            case FAST -> 1;
            case MEDIUM -> 2;
            case SLOW -> 3;
        };
    }

    void tick(ClickManager manager) {
        if (cooldown <= 0 && !clicks.isEmpty() && manager.isClickableScreen()) {
            manager.sendClick(clicks.pop());
            cooldown = ticksBetweenClicks;
        }

        if (cooldown > 0)
            cooldown--;
    }

    void add(Click click) {
        clicks.add(click);
    }

    void clear() {
        clicks.clear();
    }

    boolean isEmpty() {
        return clicks.isEmpty();
    }
}
