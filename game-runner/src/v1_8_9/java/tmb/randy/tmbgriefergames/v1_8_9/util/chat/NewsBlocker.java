package tmb.randy.tmbgriefergames.v1_8_9.util.chat;

import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;

public class NewsBlocker {

  private boolean isBlocking = false;
  public void messageReceived(ChatReceiveEvent event) {
    if(!Addon.getSharedInstance().configuration().getChatConfig().getHideNews().get())
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
