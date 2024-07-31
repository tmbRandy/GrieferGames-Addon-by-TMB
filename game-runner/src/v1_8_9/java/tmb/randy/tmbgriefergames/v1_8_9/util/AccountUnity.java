package tmb.randy.tmbgriefergames.v1_8_9.util;

import net.labymod.api.Laby;
import net.labymod.api.account.Account;
import net.labymod.api.event.client.chat.ChatReceiveEvent;

public class AccountUnity {

    public void messageReceived(ChatReceiveEvent event) {
        for (Account account : Laby.references().accountService().getAccounts()) {
            String name = account.getUsername();
            if(event.chatMessage().getPlainText().endsWith(name + " möchte sich zu dir teleportieren.") || event.chatMessage().getPlainText().endsWith(name + " möchte, dass du dich zu der Person teleportierst.")) {
                VersionisedBridge.sendCommand("/tpaccept");

            }
        }
    }
}
