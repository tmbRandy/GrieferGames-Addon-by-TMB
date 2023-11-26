package tmb.randy.tmbgriefergames.v1_8_9.util;

import net.labymod.accountmanager.storage.account.Account;
import net.labymod.api.Laby;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.core.main.LabyMod;
import tmb.randy.tmbgriefergames.core.Addon;

public class AccountUnity {

    public void messageReceived(ChatReceiveEvent event) {

        for (Account account : LabyMod.getInstance().getAccountManager().getAccounts()) {
            String name = account.getUsername();
            if(event.chatMessage().getPlainText().endsWith(name + " möchte sich zu dir teleportieren.") || event.chatMessage().getPlainText().endsWith(name + " möchte, dass du dich zu der Person teleportierst.")) {
                Laby.labyAPI().minecraft().chatExecutor().chat("/tpaccept");
            }
        }
    }
}
