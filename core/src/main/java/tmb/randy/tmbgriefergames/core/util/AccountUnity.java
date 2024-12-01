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

        String message = event.chatMessage().getPlainText();

        if(Addon.getSharedInstance().configuration().getAccountUnitySubConfig().getTpAccept().get() && (message.endsWith(" möchte sich zu dir teleportieren.") || message.endsWith(" möchte, dass du dich zu der Person teleportierst."))) {
            for (Account account : Laby.references().accountService().getAccounts()) {
                String name = account.getUsername();

                if (message.endsWith(name + " möchte sich zu dir teleportieren.") || message.endsWith(name + " möchte, dass du dich zu der Person teleportierst.")) {
                    Laby.references().chatExecutor().chat("/tpaccept");
                }
            }
        } else if(Addon.getSharedInstance().configuration().getAccountUnitySubConfig().getVoteBooster().get() && message.startsWith("[StartKick] Ersteller: ") || message.startsWith("[StartJail] Ersteller: ")) {
                String[] split = message.split(" ");
                String name = split[split.length - 1];

                for (Account account : Laby.references().accountService().getAccounts()) {
                    String accName = account.getUsername();
                    if(name.equals(accName)) {
                        Laby.labyAPI().minecraft().chatExecutor().chat("/ja");
                    }
                }
            }
    }
}