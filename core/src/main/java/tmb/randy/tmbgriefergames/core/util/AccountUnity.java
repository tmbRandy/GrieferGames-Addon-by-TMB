package tmb.randy.tmbgriefergames.core.util;

import net.labymod.api.Laby;
import net.labymod.api.account.Account;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;

public class AccountUnity {

    @Subscribe
    public void messageReceived(ChatReceiveEvent event) {
        if(!Addon.isGG())
            return;

        for (Account account : Laby.references().accountService().getAccounts()) {
            String name = account.getUsername();
            if(event.chatMessage().getPlainText().endsWith(name + " möchte sich zu dir teleportieren.") || event.chatMessage().getPlainText().endsWith(name + " möchte, dass du dich zu der Person teleportierst.")) {
                Laby.references().chatExecutor().chat("/tpaccept");
            }
        }
    }
}