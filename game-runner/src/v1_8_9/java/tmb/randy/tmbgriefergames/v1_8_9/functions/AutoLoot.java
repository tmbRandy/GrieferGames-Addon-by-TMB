package tmb.randy.tmbgriefergames.v1_8_9.functions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.labymod.api.Laby;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;
import tmb.randy.tmbgriefergames.api.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.core.enums.Functions;

import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.helper.Commander;
import tmb.randy.tmbgriefergames.core.helper.FileManager;

public class AutoLoot extends Function {

    public AutoLoot() {
        super(Functions.AUTOLOOT.name());
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        String message = event.chatMessage().getPlainText();

        if(Addon.settings().getAutoLoot().get()) {
            ActiveFunction playerTracer = Addon.getActiveFunction(Functions.PLAYERTRACER.name());
            if(CBtracker.getCurrentCB() != CBs.NONE && message.equals(Const.Chat.SWITCHER_DATA_DOWNLOADED) && playerTracer != null && !playerTracer.isEnabled()) {

                String rank = getPlayerRank(Laby.labyAPI().getName());
                int periodSkull = getTimePeriodForFreeSkull(rank);
                int periodChest = getTimePeriodForFreeChest(rank);

                if(periodChest == -1 && periodSkull == -1)
                    return;

                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if(CBtracker.isPlotworldCB()) {

                                Object freeBooster = FileManager.getPlayerValue("freeBooster");
                                String freeBoosterString = freeBooster instanceof String ? (String) freeBooster : "";

                                Object freeChest = FileManager.getPlayerValue("freeChest");
                                String freeChestString = freeChest instanceof String ? (String) freeChest : "";

                                Object freeSkull = FileManager.getPlayerValue("freeSkull");
                                String freeSkullString = freeSkull instanceof String ? (String) freeSkull : "";

                                if(periodChest > -1) {
                                    if(!freeBoosterString.isEmpty()) {
                                        LocalDateTime freeBoosterDate = LocalDateTime.parse(freeBoosterString);

                                        if(freeBoosterDate.isBefore(LocalDateTime.now())) {
                                            Commander.queue(Const.Cmd.GRIEFERBOOST);
                                        }
                                    } else {
                                        Commander.queue(Const.Cmd.GRIEFERBOOST);
                                    }

                                    if(!freeChestString.isEmpty()) {
                                        LocalDateTime freeChestDate = LocalDateTime.parse(freeChestString);

                                        if(freeChestDate.isBefore(LocalDateTime.now())) {
                                            Commander.queue(Const.Cmd.FREEKISTE);
                                        }
                                    } else {
                                        Commander.queue(Const.Cmd.FREEKISTE);
                                    }
                                }


                                if(!freeSkullString.isEmpty() && periodSkull > -1) {
                                    LocalDateTime freeSkullDate = LocalDateTime.parse(freeSkullString);

                                    if(freeSkullDate.isBefore(LocalDateTime.now())) {
                                        Addon.displayNotification(Addon.translate("autoLoot.skullAvailable"));
                                    }
                                }
                            }
                        }
                    }, 5000
                );
            }

            if(message.startsWith(Const.Chat.CASE_OPENING_TIMER_START) && message.endsWith(Const.Chat.CASE_OPENING_TIMER_END)) {
                event.setCancelled(true);
                String isolatedDate = message.replace(Const.Chat.CASE_OPENING_TIMER_START, "").replace(Const.Chat.CASE_OPENING_TIMER_END, "");
                FileManager.setPlayerValue("freeChest", stringToDate(isolatedDate).toString());
            } else if(message.startsWith(Const.Chat.FREE_BOOSTER_TIMER_START) && message.endsWith(Const.Chat.FREE_BOOSTER_TIMER_END)) {
                event.setCancelled(true);
                String isolatedDate = message.replace(Const.Chat.FREE_BOOSTER_TIMER_START, "").replace(Const.Chat.FREE_BOOSTER_TIMER_END, "");
                FileManager.setPlayerValue("freeBooster", stringToDate(isolatedDate).toString());
            } else if(message.equals(Const.Chat.CASE_OPENING_RECEIVED)) {
                String rank = getPlayerRank(Laby.labyAPI().getName());
                int periodChest = getTimePeriodForFreeChest(rank);
                if(periodChest > -1)
                    FileManager.setPlayerValue("freeChest", LocalDateTime.now().plusDays(periodChest).toString());
            } else if(message.startsWith(Const.Chat.FREE_BOOSTER_RECEIVED_START) && message.endsWith(Const.Chat.FREE_BOOSTER_RECEIVED_END)) {
                String rank = getPlayerRank(Laby.labyAPI().getName());
                int periodChest = getTimePeriodForFreeChest(rank);
                if(periodChest > -1)
                    FileManager.setPlayerValue("freeBooster", LocalDateTime.now().plusDays(periodChest).toString());
            } else if(message.startsWith(Const.Chat.KOPF_START) && message.endsWith(Const.Chat.KOPF_END)) {
                String rank = getPlayerRank(Laby.labyAPI().getName());
                int periodSkull = getTimePeriodForFreeSkull(rank);
                if(periodSkull > -1)
                    FileManager.setPlayerValue("freeSkull", LocalDateTime.now().plusDays(periodSkull).toString());
            }
        }
    }

    public static String getPlayerRank(String name) {
        if (Minecraft.getMinecraft().getNetHandler() == null)
            return "";

        NetworkPlayerInfo info = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(name);

        if (info == null)
            return "";

        IChatComponent component = info.getDisplayName();
        if (component != null) {
            String[] parts = component.getUnformattedText().split("┃");
            if (parts.length > 1) {
                return parts[0].trim();
            }
        }
        return "";
    }

    private LocalDateTime stringToDate(String string) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm:ss");
        return LocalDateTime.parse(string, formatter);
    }

    private int getTimePeriodForFreeSkull(String rank) {
        return switch (rank) {
            case "Spieler", "Premium", "Ultra", "Legende" -> -1;
            case "Titan" -> 14;
            default -> 7;
        };
    }

    private int getTimePeriodForFreeChest(String rank) {
        return switch (rank) {
            case "Spieler", "Premium", "Ultra", "Legende", "Titan" -> -1;
            default -> 14;
        };
    }
}