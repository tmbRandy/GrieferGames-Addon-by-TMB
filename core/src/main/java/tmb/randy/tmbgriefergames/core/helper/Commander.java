package tmb.randy.tmbgriefergames.core.helper;

import java.util.LinkedList;
import java.util.Queue;
import javax.inject.Singleton;
import net.labymod.api.Laby;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;

@Singleton
public class Commander {

    private static final Commander INSTANCE = new Commander();
    private static final int COOLDOWN = 40;

    private final Queue<String> queue = new LinkedList<>();
    private int counter = 0;
    private boolean dispatching = false;

    public static Commander INSTANCE() {
        return INSTANCE;
    }

    public static void queue(String command) {
        INSTANCE.queue.offer(command);
    }

    @Subscribe
    public void onGameTick(GameTickEvent event) {
        if(!Addon.isGG())
            return;

        if (counter > 0) {
            counter--;
            return;
        }

        if (!queue.isEmpty()) {
            String command = queue.poll();
            dispatching = true;
            Laby.labyAPI().minecraft().chatExecutor().chat(command);
            dispatching = false;
            counter = COOLDOWN;
        }
    }

    @Subscribe
    public void onChatMessageSend(ChatMessageSendEvent event) {

        if(!Addon.isGG())
            return;

        String message = event.getMessage();
        if (!message.startsWith("/")) {
            return;
        }

        if (dispatching)
            return;

        queue.offer(message);
        event.setCancelled(true);
    }


    @Subscribe
    public void cbChangedEvent(CbChangedEvent event) {
        if(!Addon.isGG())
            return;

        counter = 0;
        dispatching = false;
        queue.clear();
    }

    public static boolean canSend() {
        return INSTANCE().counter == 0 && INSTANCE().queue.isEmpty();
    }

}
