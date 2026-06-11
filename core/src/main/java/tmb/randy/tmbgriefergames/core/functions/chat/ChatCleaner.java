package tmb.randy.tmbgriefergames.core.functions.chat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import net.labymod.api.Laby;
import net.labymod.api.client.component.event.ClickEvent;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;

public class ChatCleaner extends Function {

    private boolean isBlocking = false;
    private static final java.util.regex.Pattern PLOT_COORDINATES_PATTERN = Pattern.compile("^\\[GrieferGames] \\[(-?\\d+);(-?\\d+)] \\w+ betrat dein Grundstück\\.$");

    private final Set<String> cleanupMessages = new HashSet<>(Arrays.asList(
        Const.Chat.GG_DOWNLOAD,
        Const.Chat.GG_MYSTERYMOD,
        Const.Chat.GG_PORTALRAUM,
        Const.Chat.SWITCHER_LOADING,
        Const.Chat.SWITCHER_DATA_DOWNLOADED,
        Const.Chat.GG_DATA_DOWNLOADED,
        Const.Chat.GG_TELEPORT_WAIT,
        Const.Chat.GGAUTH_VERIFIED,
        Const.Chat.ALREADY_CONNECTING,
        Const.Chat.TELEPORTED_TO_PLOT,
        Const.Chat.GG_DAYTIME_UPDATED,
        Const.Chat.GG_DAYTIME_RESTORED,
        Const.Chat.GG_JOIN_WAIT,
        Const.Chat.SERVER_STATUS,
        Const.Chat.ERGRIFFENE_MASSNAHMEN,
        Const.Chat.PORTALRAUM_CONNECTING
    ));

    public ChatCleaner() {
        super(Functions.CHATCLEANER.name());
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        String message = event.chatMessage().getPlainText();

        // Handle specific messages regardless of clean chat setting
        if (message.equals(Const.Chat.FREUNDE_KEINE_ANFRAGEN) &&
            Addon.settings().getChatConfig().getCleanChat().get()) {
            event.setCancelled(true);
            return;
        }

        if (message.equals(Const.Chat.REZEPTE_NOT_ENOUGH_MATERIAL)) {
            event.setCancelled(true);
            return;
        }

        // Add click event for plot coordinates
        String coordinates = plotEnterCoordinates(message);
        if (coordinates != null) {
            event.chatMessage().component().clickEvent(ClickEvent.runCommand(Const.Cmd.PLOT_HOME + coordinates));
        }

        // Case opening filter
        if (Addon.settings().getChatConfig().getMuteCaseOpening().get() &&
            (message.startsWith(Const.Chat.CASEOPENING_PRIZE_DRAWN) ||
                (message.startsWith(Const.Chat.CASEOPENING_PLAYER_WON_START) &&
                    message.endsWith(Const.Chat.CASEOPENING_PLAYER_WON_END)) ||
                message.equals(Const.Chat.CASEOPENING_MAIN_PRIZE))) {
            event.setCancelled(true);
            return;
        }

        // Lucky blocks filter
        if (Addon.settings().getChatConfig().getMuteLuckyBlocks().get() &&
            !message.contains(Laby.labyAPI().getName()) &&
            (message.startsWith(Const.Chat.LUCKYBLOCK_PREFIX) ||
                (message.startsWith(Const.Chat.TEAM_ADMIN_PREFIX) &&
                    message.endsWith(Const.Chat.TEAM_ADMIN_END)))) {
            event.setCancelled(true);
            return;
        }

        // Only process the rest if clean chat is enabled
        if (!Addon.settings().getChatConfig().getCleanChat().get()) {
            return;
        }

        // Standard cleanup messages
        if (cleanupMessages.contains(message)) {
            event.setCancelled(true);
            return;
        }

        // Server change messages
        if ((message.startsWith(Const.Chat.AUTOSWITCH_START) &&
            message.endsWith(Const.Chat.AUTOSWITCH_END)) ||
            (message.startsWith(Const.Chat.SERVERSWITCH_START) &&
                message.endsWith(Const.Chat.SERVERSWITCH_END))) {
            event.setCancelled(true);
            return;
        }

        // Streamer filter
        if (Addon.settings().getChatConfig().getMuteStreamer().get() &&
            message.contains(Const.Chat.STREAMER_TAG)) {
            event.setCancelled(true);
            return;
        }

        // Empty lines filter
        if (Addon.settings().getChatConfig().getHideEmptyLines().get() &&
            (message.isBlank() || message.equals("»"))) {
            event.setCancelled(true);
            return;
        }

        // News filter - toggle blocking state when seeing the delimiter
        if (Addon.settings().getChatConfig().getHideNews().get()) {

            if (isBlocking)
                event.setCancelled(true);

            if (message.equals(Const.Chat.NEWS_DELIMITER))
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