package tmb.randy.tmbgriefergames.v1_12_2.util.chat;

import net.labymod.api.client.component.Component;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;

public class PaymentValidator {

    private final static String FAKEMONEY_PREFIX = "§4§l[§c§l✖ " + I18n.translate("autocraft.chat.fakeMoney") + "§4§l]";
    final String VALID_PREFIX = "§2§l[§a§l✔ " + I18n.translate("tmbgriefergames.chat.realMoney") + "§2§l]";

    public void messageReceived(ChatReceiveEvent event) {
        if(!Addon.isGG() || !Addon.getSharedInstance().configuration().getChatConfig().getAntiFakeMoney().get()) {
            return;
        }

        String message = event.chatMessage().getPlainText();

        if(message.contains("hat dir") && message.contains("gegeben")) {
            if(message.matches(".* hat dir \\$\\d{1,3}(?:,\\d{3})*(\\.\\d{2})? gegeben\\.")) {
                if(!message.contains("[Greeting]") && !message.contains("»")) {
                    event.setMessage(event.message().append(Component.text(VALID_PREFIX)));
                    return;
                }
            }

            event.setMessage(event.message().append(Component.text(FAKEMONEY_PREFIX)));
        }
    }
}
