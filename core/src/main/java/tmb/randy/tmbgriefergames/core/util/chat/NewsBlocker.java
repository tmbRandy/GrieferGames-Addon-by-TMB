package tmb.randy.tmbgriefergames.core.util.chat;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;

public class NewsBlocker {

  private boolean isBlocking = false;

    @Subscribe
  public void messageReceived(ChatReceiveEvent event) {
    if(!Addon.getSharedInstance().configuration().getChatConfig().getHideNews().get() || !Addon.isGG())
      return;

    String message = event.chatMessage().getPlainText();

    if(isBlocking) {
      event.setCancelled(true);
    }

    if(message.equals("------------ [ News ] ------------")) {
      isBlocking = !isBlocking;
    }

    if(isBlocking) {
      event.setCancelled(true);
    }
  }

}
