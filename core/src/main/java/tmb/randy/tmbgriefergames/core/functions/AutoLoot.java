package tmb.randy.tmbgriefergames.core.functions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.labymod.api.Laby;
import net.labymod.api.client.component.serializer.plain.PlainTextComponentSerializer;
import net.labymod.api.client.network.ClientPacketListener;
import net.labymod.api.client.network.NetworkPlayerInfo;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.core.commands.PlayerTracerCommand;
import tmb.randy.tmbgriefergames.core.flow.FlowManager;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.helper.BaseHelper;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.helper.Commander;
import tmb.randy.tmbgriefergames.core.helper.FileManager;

/** Collects the free booster, free chests and reminds of the free skull, depending on the player's rank. */
public class AutoLoot extends Function {
    private static final DateTimeFormatter CHAT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm:ss");
    private static final long DELAY_AFTER_JOIN_MS = 5000;

    private static final String FREE_BOOSTER = "freeBooster";
    private static final String FREE_CHEST = "freeChest";
    private static final String FREE_SKULL = "freeSkull";

    public AutoLoot() {
        super(Functions.AUTOLOOT.name());
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if (!Addon.settings().getAutoLoot().get())
            return;

        String message = event.chatMessage().getPlainText();

        if (message.equals(Const.Chat.SWITCHER_DATA_DOWNLOADED)) {
            if (CBtracker.getCurrentCB() != CBs.NONE && !FlowManager.get().isRunning(PlayerTracerCommand.FLOW))
                scheduleLootCheck(getOwnRank());
        } else if (isBetween(message, Const.Chat.CASE_OPENING_TIMER_START, Const.Chat.CASE_OPENING_TIMER_END)) {
            event.setCancelled(true);
            saveChatDate(FREE_CHEST, message, Const.Chat.CASE_OPENING_TIMER_START, Const.Chat.CASE_OPENING_TIMER_END);
        } else if (isBetween(message, Const.Chat.FREE_BOOSTER_TIMER_START, Const.Chat.FREE_BOOSTER_TIMER_END)) {
            event.setCancelled(true);
            saveChatDate(FREE_BOOSTER, message, Const.Chat.FREE_BOOSTER_TIMER_START, Const.Chat.FREE_BOOSTER_TIMER_END);
        } else if (message.equals(Const.Chat.CASE_OPENING_RECEIVED)) {
            saveNextDate(FREE_CHEST, getFreeChestPeriod(getOwnRank()));
        } else if (isBetween(message, Const.Chat.FREE_BOOSTER_RECEIVED_START, Const.Chat.FREE_BOOSTER_RECEIVED_END)) {
            saveNextDate(FREE_BOOSTER, getFreeChestPeriod(getOwnRank()));
        } else if (isBetween(message, Const.Chat.KOPF_START, Const.Chat.KOPF_END)) {
            saveNextDate(FREE_SKULL, getFreeSkullPeriod(getOwnRank()));
        }
    }

    private void scheduleLootCheck(String rank) {
        boolean chestAvailable = getFreeChestPeriod(rank) > -1;
        boolean skullAvailable = getFreeSkullPeriod(rank) > -1;
        if (!chestAvailable && !skullAvailable)
            return;

        BaseHelper.schedule(() -> {
            if (!CBtracker.isPlotworldCB())
                return;

            if (chestAvailable) {
                if (isDue(FREE_BOOSTER, true))
                    Commander.queue(Const.Cmd.GRIEFERBOOST);
                if (isDue(FREE_CHEST, true))
                    Commander.queue(Const.Cmd.FREEKISTE);
            }

            if (skullAvailable && isDue(FREE_SKULL, false))
                Addon.displayNotification(Addon.translate("autoLoot.skullAvailable"));
        }, DELAY_AFTER_JOIN_MS);
    }

    /** True if the saved date has passed. Without a saved date {@code dueIfUnknown} is returned. */
    private static boolean isDue(String key, boolean dueIfUnknown) {
        if (!(FileManager.getPlayerValue(key) instanceof String date) || date.isEmpty())
            return dueIfUnknown;

        return LocalDateTime.parse(date).isBefore(LocalDateTime.now());
    }

    private static void saveChatDate(String key, String message, String prefix, String suffix) {
        String date = message.substring(prefix.length(), message.length() - suffix.length());
        FileManager.setPlayerValue(key, LocalDateTime.parse(date, CHAT_DATE_FORMAT).toString());
    }

    private static void saveNextDate(String key, int periodDays) {
        if (periodDays > -1)
            FileManager.setPlayerValue(key, LocalDateTime.now().plusDays(periodDays).toString());
    }

    private static boolean isBetween(String message, String prefix, String suffix) {
        return message.startsWith(prefix) && message.endsWith(suffix);
    }

    private static String getOwnRank() {
        return getPlayerRank(Laby.labyAPI().getName());
    }

    /** The rank shown in front of the player's name in the tab list ("Rang ┃ Name"). */
    public static String getPlayerRank(String name) {
        ClientPacketListener connection = Laby.labyAPI().minecraft().getClientPacketListener();
        NetworkPlayerInfo info = connection == null ? null : connection.getNetworkPlayerInfo(name);
        if (info == null || info.displayName() == null)
            return "";

        String[] parts = PlainTextComponentSerializer.plainText().serialize(info.displayName()).split("┃");
        return parts.length > 1 ? parts[0].trim() : "";
    }

    private static int getFreeSkullPeriod(String rank) {
        return switch (rank) {
            case "Spieler", "Premium", "Ultra", "Legende" -> -1;
            case "Titan" -> 14;
            default -> 7;
        };
    }

    private static int getFreeChestPeriod(String rank) {
        return switch (rank) {
            case "Spieler", "Premium", "Ultra", "Legende", "Titan" -> -1;
            default -> 14;
        };
    }
}
