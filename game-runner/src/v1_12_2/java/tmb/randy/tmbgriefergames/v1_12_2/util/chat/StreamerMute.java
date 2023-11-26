package tmb.randy.tmbgriefergames.v1_12_2.util.chat;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;

public class StreamerMute {

    public void messageReceived(ChatReceiveEvent event) {
        String message = event.chatMessage().getPlainText();

        if(message.startsWith("[Streamer]") && Addon.getSharedInstance().configuration().getChatConfig().getMuteStreamer().get()) {
            event.setCancelled(true);
        }
    }
}
