package tmb.randy.tmbgriefergames.core.functions.chat;

import net.labymod.api.client.component.Component;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.helper.I19n;

public class PaymentValidator extends Function {

    public PaymentValidator() {
        super(Functions.PAYMENTVALIDATOR);
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if(!Addon.getSharedInstance().configuration().getChatConfig().getAntiFakeMoney().get()) return;

        String message = event.chatMessage().getPlainText();

        if(message.contains("hat dir") && message.contains("gegeben")) {
            if(message.matches(".* hat dir \\$\\d{1,3}(?:,\\d{3})*(\\.\\d{2})? gegeben\\.")) {
                if(!message.contains("[Greeting]") && !message.contains("»") && !message.contains("Plot-Chat")) {
                    event.setMessage(event.message().append(Component.text("§2§l[§a§l✔ " + I19n.translate("chat.realMoney") + "§2§l]")));
                    return;
                }
            }

            event.setMessage(event.message().append(Component.text("§4§l[§c§l✖ " + I19n.translate("chat.fakeMoney") + "§4§l]")));
        }
    }
}