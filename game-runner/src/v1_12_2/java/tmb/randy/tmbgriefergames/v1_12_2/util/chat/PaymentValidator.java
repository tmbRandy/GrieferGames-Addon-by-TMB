package tmb.randy.tmbgriefergames.v1_12_2.util.chat;

import net.labymod.api.client.component.Component;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;

public class PaymentValidator {

    public void messageReceived(ChatReceiveEvent event) {
        if(!Addon.getSharedInstance().configuration().getChatConfig().getAntiFakeMoney().get())
            return;

        String message = event.chatMessage().getPlainText();

        boolean isValid = false;

        if(message.contains("hat dir") && message.contains("gegeben")) {
            if(message.matches(".* hat dir \\$\\d{1,3}(?:,\\d{3})*(\\.\\d{2})? gegeben\\.")) {
                if(!message.contains("[") && !message.contains("]")) {
                    isValid = true;
                }
            }

            if(isValid) {
                final String VALID_PREFIX = "§2§l[§a§l✔ " + I18n.translate("tmbgriefergames.chat.realMoney") + "§2§l]";
                event.setMessage(event.message().append(Component.text(VALID_PREFIX)));
            } else {
                final String FAKEMONEY_PREFIX = "§4§l[§c§l✖ " + I18n.translate("tmbgriefergames.chat.fakeMoney") + "§4§l]";
                event.setMessage(event.message().append(Component.text(FAKEMONEY_PREFIX)));
            }
        }
    }
}
