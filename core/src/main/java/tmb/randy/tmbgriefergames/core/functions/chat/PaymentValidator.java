package tmb.randy.tmbgriefergames.core.functions.chat;

import net.labymod.api.client.component.Component;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;

public class PaymentValidator extends Function {

    public PaymentValidator() {
        super(Functions.PAYMENTVALIDATOR.name());
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if(!Addon.settings().getChatConfig().getAntiFakeMoney().get()) return;

        String message = event.chatMessage().getPlainText();

        if(message.contains("hat dir") && message.contains("gegeben")) {
            if(message.matches(".* hat dir \\$\\d{1,3}(?:,\\d{3})*(\\.\\d{2})? gegeben\\.")) {
                if(!message.contains("[Greeting]") && !message.contains("»") && !message.contains("Plot-Chat")) {
                    event.setMessage(event.message().append(Component.text("§2§l[§a§l✔ " + Addon.translate("chat.realMoney") + "§2§l]")));
                    return;
                }
            }

            event.setMessage(event.message().append(Component.text("§4§l[§c§l✖ " + Addon.translate("chat.fakeMoney") + "§4§l]")));
        }
    }
}