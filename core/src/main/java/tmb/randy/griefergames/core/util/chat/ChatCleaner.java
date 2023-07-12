package tmb.randy.griefergames.core.util.chat;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.griefergames.core.Addon;
import tmb.randy.griefergames.core.config.Configuration;
import java.util.Arrays;
import java.util.List;

public class ChatCleaner {

    private final List<String> cleanupMessages = Arrays.asList(
        "[GrieferGames] Download: https://mysterymod.net/download/",
        "[GrieferGames] Wir sind optimiert für MysteryMod. Lade Dir gerne die Mod runter!",
        "[GrieferGames] Du bist im Portalraum. Wähle deinen Citybuild aus.",
        "[Switcher] Lade Daten herunter!",
        "[Switcher] Daten heruntergeladen!",
        "[GrieferGames] Deine Daten wurden vollständig heruntergeladen.",
        "[GrieferGames] Bitte warte 12 Sekunden zwischen jedem Teleport.",
        "[GGAuth] Du wurdest erfolgreich verifiziert.",
        "Already connecting to this server!",
        "[GrieferGames] Du wurdest zum Grundstück teleportiert."
    );

    @Subscribe
    public void messageReceived(ChatReceiveEvent event) {
        final Configuration configuration = Addon.getSharedInstance().configuration();


        if(!configuration.getChatConfig().getCleanChat().get()) {
            return;
        }

        // "No friendhip requests" messages is sent before the GrieferGames Server Name is set. So it has to be handles before checking for GG.
        String message = event.chatMessage().getPlainText();
        if(message.equals("[Freunde] Du hast aktuell keine Freundschaftsanfragen.")) {
            event.setCancelled(true);
        }

        if(!Addon.isGG()) {
            return;
        }

        for (String str : cleanupMessages) {
            if(str.equals(message)) {
                event.setCancelled(true);
                break;
            }
        }

    }
}
