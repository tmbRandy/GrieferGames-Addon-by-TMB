package tmb.randy.tmbgriefergames.core.util.chat;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;

public class StreamerMute {

    @Subscribe
    public void messageReceived(ChatReceiveEvent event) {
        if(!Addon.isGG())
            return;

        String message = event.chatMessage().getPlainText();

        if(message.contains("§8[§6Streamer§8]") && Addon.getSharedInstance().configuration().getChatConfig().getMuteStreamer().get()) {
            event.setCancelled(true);
        }
    }
}
