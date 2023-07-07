package tmbrandy.griefergames.core.util;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmbrandy.griefergames.core.Addon;

public class NewsBlocker {

  private boolean isBlocking = false;
  @Subscribe
  public void blockNews(ChatReceiveEvent event) {
    if(!Addon.isGG()) {
      return;
    }

    String message = event.chatMessage().getPlainText();

    if(isBlocking) {
      event.setCancelled(true);
    }

    if(message.equals("------------ [ News ] ------------")) {
      isBlocking = !isBlocking;
    } else {
      Addon.getSharedInstance().logger().info("Message: " + message);
    }

    if(isBlocking) {
      event.setCancelled(true);
    }
  }

}
