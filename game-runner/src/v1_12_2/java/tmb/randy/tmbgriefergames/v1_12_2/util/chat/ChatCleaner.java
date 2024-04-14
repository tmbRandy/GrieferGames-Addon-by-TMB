package tmb.randy.tmbgriefergames.v1_12_2.util.chat;

import java.util.Arrays;
import java.util.List;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.config.Configuration;

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
        "[GrieferGames] Du wurdest zum Grundstück teleportiert.",
        "[GrieferGames] Deine Tageszeit wurde vom Grundstück aktualisiert.",
        "[GrieferGames] Deine Tageszeit wurde wiederhergestellt.",
        "[GrieferGames] Bitte warte 15 Sekunden zwischen jedem Join-Versuch.",
        "------------ [ Server-Status ] ------------",
        "Ergriffene Maßnahmen:",
        "Versuche in den Portalraum zu verbinden."
    );

    public void messageReceived(ChatReceiveEvent event) {
        final Configuration configuration = Addon.getSharedInstance().configuration();
        String message = event.chatMessage().getPlainText();

        if(message.equals("[Rezepte] Du hast nicht genug Material, um dieses Rezept herzustellen.")) {
            event.setCancelled(true);
        }


        if(!configuration.getChatConfig().getCleanChat().get())
            return;


        // "No friendhip requests" messages is sent before the GrieferGames Server Name is set. So it has to be handles before checking for GG.
        if(message.equals("[Freunde] Du hast aktuell keine Freundschaftsanfragen.")) {
            event.setCancelled(true);
        }


        for (String str : cleanupMessages) {
            if(str.equals(message)) {
                event.setCancelled(true);
                break;
            }
        }

    }
}
