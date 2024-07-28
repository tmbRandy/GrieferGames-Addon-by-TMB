package tmb.randy.tmbgriefergames.v1_8_9.util;

import net.labymod.accountmanager.storage.account.Account;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
// Unfortunately there is no LabyAPI for this so we must use the Core package :/
import net.labymod.core.main.LabyMod;

public class AccountUnity {

    public void messageReceived(ChatReceiveEvent event) {
        for (Account account : LabyMod.getInstance().getAccountManager().getAccounts()) {
            String name = account.getUsername();
            if(event.chatMessage().getPlainText().endsWith(name + " möchte sich zu dir teleportieren.") || event.chatMessage().getPlainText().endsWith(name + " möchte, dass du dich zu der Person teleportierst.")) {
                VersionisedBridge.sendCommand("/tpaccept");
            }
        }
    }
}
