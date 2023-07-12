package tmb.randy.griefergames.core.util.chat;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.griefergames.core.Addon;

public class EmptyLinesRemover {

    @Subscribe
    public void removeEmptyLines(ChatReceiveEvent event) {
        if(!Addon.isGG() || !Addon.getSharedInstance().configuration().getChatConfig().getHideEmptyLines().get()) {
            return;
        }

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
