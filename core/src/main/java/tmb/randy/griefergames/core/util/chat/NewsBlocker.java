package tmb.randy.griefergames.core.util.chat;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.griefergames.core.Addon;

public class NewsBlocker {

  private boolean isBlocking = false;
  @Subscribe
  public void blockNews(ChatReceiveEvent event) {
    if(!Addon.isGG() || !Addon.getSharedInstance().configuration().getChatConfig().getHideNews().get()) {
      return;
    }

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
