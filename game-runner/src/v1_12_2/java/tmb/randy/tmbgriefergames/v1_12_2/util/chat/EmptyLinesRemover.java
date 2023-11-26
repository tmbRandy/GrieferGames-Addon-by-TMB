package tmb.randy.tmbgriefergames.v1_12_2.util.chat;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;

public class EmptyLinesRemover {

    public void messageReceived(ChatReceiveEvent event) {
        if(!Addon.getSharedInstance().configuration().getChatConfig().getHideEmptyLines().get())
            return;

        String message = event.chatMessage().getPlainText();

        if(isStringEmpty(message) || message.equals("»") || message.length() == 0) {
            event.setCancelled(true);
        }
    }


    public static boolean isStringEmpty(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
