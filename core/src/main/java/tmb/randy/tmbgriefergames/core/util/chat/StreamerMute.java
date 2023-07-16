package tmb.randy.tmbgriefergames.core.util.chat;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;

public class StreamerMute {
    @Subscribe
    public void muteStreamer(ChatReceiveEvent event) {
        String message = event.chatMessage().getPlainText();

        if(message.startsWith("[Streamer]") && Addon.getSharedInstance().configuration().getChatConfig().getMuteStreamer().get()) {
            event.setCancelled(true);
        }
    }
}
