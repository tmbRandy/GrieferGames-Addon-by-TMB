package tmb.randy.tmbgriefergames.core.functions.chat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import net.labymod.api.Laby;
import net.labymod.api.client.component.event.ClickEvent;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;

public class ChatCleaner extends Function {

    private boolean isBlocking = false;
    private static final java.util.regex.Pattern PLOT_COORDINATES_PATTERN = Pattern.compile("^\\[GrieferGames] \\[(-?\\d+);(-?\\d+)] \\w+ betrat dein Grundstück\\.$");

    private final Set<String> cleanupMessages = new HashSet<>(Arrays.asList(
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
    ));

    public ChatCleaner() {
        super(Functions.CHATCLEANER);
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        String message = event.chatMessage().getPlainText();

        // Handle specific messages regardless of clean chat setting
        if (message.equals("[Freunde] Du hast aktuell keine Freundschaftsanfragen.") &&
            Addon.getSharedInstance().configuration().getChatConfig().getCleanChat().get()) {
            event.setCancelled(true);
            return;
        }

        if (message.equals("[Rezepte] Du hast nicht genug Material, um dieses Rezept herzustellen.")) {
            event.setCancelled(true);
            return;
        }

        // Add click event for plot coordinates
        String coordinates = plotEnterCoordinates(message);
        if (coordinates != null) {
            event.chatMessage().component().clickEvent(ClickEvent.runCommand("/p h " + coordinates));
        }

        // Case opening filter
        if (Addon.getSharedInstance().configuration().getChatConfig().getMuteCaseOpening().get() &&
            (message.startsWith("[CaseOpening] Folgender Preis wurde gezogen: ") ||
                (message.startsWith("[CaseOpening] Der Spieler ") &&
                    message.endsWith(" hat einen Hauptpreis gewonnen!")) ||
                message.equals("[CaseOpening] Ein Spieler hat einen Hauptpreis gewonnen!"))) {
            event.setCancelled(true);
            return;
        }

        // Lucky blocks filter
        if (Addon.getSharedInstance().configuration().getChatConfig().getMuteLuckyBlocks().get() &&
            !message.contains(Laby.labyAPI().getName()) &&
            (message.startsWith("[LuckyBlock] ") ||
                (message.startsWith("[TEAM] Admin ┃ ") &&
                    message.endsWith(" » Hey Leute, was geht?")))) {
            event.setCancelled(true);
            return;
        }

        // Only process the rest if clean chat is enabled
        if (!Addon.getSharedInstance().configuration().getChatConfig().getCleanChat().get()) {
            return;
        }

        // Standard cleanup messages
        if (cleanupMessages.contains(message)) {
            event.setCancelled(true);
            return;
        }

        // Server change messages
        if ((message.startsWith("[GrieferGames] Du wurdest automatisch auf ") &&
            message.endsWith(" verbunden.")) ||
            (message.startsWith("[GrieferGames] Serverwechsel auf ") &&
                message.endsWith(" wurde gestartet.."))) {
            event.setCancelled(true);
            return;
        }

        // Streamer filter
        if (Addon.getSharedInstance().configuration().getChatConfig().getMuteStreamer().get() &&
            message.contains("§8[§6Streamer§8]")) {
            event.setCancelled(true);
            return;
        }

        // Empty lines filter
        if (Addon.getSharedInstance().configuration().getChatConfig().getHideEmptyLines().get() &&
            (message.isBlank() || message.equals("»"))) {
            event.setCancelled(true);
            return;
        }

        // News filter - toggle blocking state when seeing the delimiter
        if (Addon.getSharedInstance().configuration().getChatConfig().getHideNews().get()) {

            if (isBlocking)
                event.setCancelled(true);

            if (message.equals("------------ [ News ] ------------"))
                isBlocking = !isBlocking;

            if (isBlocking)
                event.setCancelled(true);
        }
    }

    public static String plotEnterCoordinates(String text) {
        java.util.regex.Matcher matcher = PLOT_COORDINATES_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1) + ";" + matcher.group(2);
        }
        return null;
    }
}