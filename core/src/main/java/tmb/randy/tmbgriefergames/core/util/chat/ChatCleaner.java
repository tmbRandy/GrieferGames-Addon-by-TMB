package tmb.randy.tmbgriefergames.core.util.chat;

import java.util.Arrays;
import java.util.List;
import net.labymod.api.Laby;
import net.labymod.api.client.component.event.ClickEvent;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;

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

    @Subscribe
    public void messageReceived(ChatReceiveEvent event) {
        String message = event.chatMessage().getPlainText();

        // "No friendhip requests" messages is sent before the GrieferGames Server Name is set. So it has to be handles before checking for GG.
        if(message.equals("[Freunde] Du hast aktuell keine Freundschaftsanfragen.") && Addon.getSharedInstance().configuration().getChatConfig().getCleanChat().get()) {
            event.setCancelled(true);
        }

        if(!Addon.isGG())
            return;

        if(message.equals("[Rezepte] Du hast nicht genug Material, um dieses Rezept herzustellen.")) {
            event.setCancelled(true);
        }

        if(plotEnterCoordinates(message) instanceof String coordinates)
            event.chatMessage().component().clickEvent(ClickEvent.runCommand("/p h " + coordinates));

        if(Addon.getSharedInstance().configuration().getChatConfig().getMuteCaseOpening().get() && !event.isCancelled()) {
            if(message.startsWith("[CaseOpening] Folgender Preis wurde gezogen: ") || (message.startsWith("[CaseOpening] Der Spieler ") && message.endsWith(" hat einen Hauptpreis gewonnen!"))) {
                event.setCancelled(true);
            }
        }

        if(Addon.getSharedInstance().configuration().getChatConfig().getMuteLuckyBlocks().get() && !event.isCancelled()) {
            if(!message.contains(Laby.labyAPI().getName()) && (message.startsWith("[LuckyBlock] ") || (message.startsWith("[TEAM] Admin ┃ ") && message.endsWith(" » Hey Leute, was geht?")))) {
                event.setCancelled(true);
            }
        }

        if(Addon.getSharedInstance().configuration().getChatConfig().getCleanChat().get() && !event.isCancelled()) {
            for (String str : cleanupMessages) {
                if(str.equals(message)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    public static String plotEnterCoordinates(String text) {
        String regex = "^\\[GrieferGames] \\[(-?\\d+);(-?\\d+)] \\w+ betrat dein Grundstück\\.$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1) + ";" + matcher.group(2);
        }

        return null;
    }
}
