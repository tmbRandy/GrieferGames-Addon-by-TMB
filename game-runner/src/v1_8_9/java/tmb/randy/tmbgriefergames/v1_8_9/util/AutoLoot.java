package tmb.randy.tmbgriefergames.v1_8_9.util;

import net.labymod.api.Laby;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.CBtracker;
import tmb.randy.tmbgriefergames.core.FileManager;
import tmb.randy.tmbgriefergames.core.enums.CBs;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AutoLoot {

    public void chatMessageReceived(ChatReceiveEvent event) {
        String message = event.chatMessage().getPlainText();

        if(Addon.getSharedInstance().configuration().getAutoLoot().get()) {
            if(CBtracker.getCurrentCB() != CBs.NONE && message.equals("[Switcher] Daten heruntergeladen!") && Addon.getSharedInstance()
                .getPlayerTracer().isTracerDisabled()) {

                String rank = getPlayerRank(Laby.labyAPI().getName());
                int periodSkull = getTimePeriodForFreeSkull(rank);
                int periodChest = getTimePeriodForFreeChest(rank);

                if(periodChest == -1 && periodSkull == -1)
                    return;

                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if(!Addon.isGG())
                                return;

                            if(CBtracker.isPlotworldCB() && Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null) {

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
                                            Minecraft.getMinecraft().thePlayer.sendChatMessage("/grieferboost");
                                        }
                                    } else {
                                        Minecraft.getMinecraft().thePlayer.sendChatMessage("/grieferboost");
                                    }

                                    if(!freeChestString.isEmpty()) {
                                        LocalDateTime freeChestDate = LocalDateTime.parse(freeChestString);

                                        if(freeChestDate.isBefore(LocalDateTime.now())) {
                                            Minecraft.getMinecraft().thePlayer.sendChatMessage("/freekiste");
                                        }
                                    } else {
                                        Minecraft.getMinecraft().thePlayer.sendChatMessage("/freekiste");
                                    }
                                }


                                if(!freeSkullString.isEmpty() && periodSkull > -1) {
                                    LocalDateTime freeSkullDate = LocalDateTime.parse(freeSkullString);

                                    if(freeSkullDate.isBefore(LocalDateTime.now())) {
                                        Addon.getSharedInstance().displayNotification(I18n.translate("tmbgriefergames.autoLoot.skullAvailable"));
                                    }
                                }
                            }
                        }
                    }, 5000
                );
            }

            if(message.startsWith("[CaseOpening] Du kannst erst am ") && message.endsWith(" wieder Free-Kisten abholen.")) {
                event.setCancelled(true);
                String isolatedDate = message.replace("[CaseOpening] Du kannst erst am ", "").replace(" wieder Free-Kisten abholen.", "");
                FileManager.setPlayerValue("freeChest", stringToDate(isolatedDate).toString());
            } else if(message.startsWith("Du kannst erst am ") && message.endsWith(" wieder einen Free-Booster abholen.")) {
                event.setCancelled(true);
                String isolatedDate = message.replace("Du kannst erst am ", "").replace(" wieder einen Free-Booster abholen.", "");
                FileManager.setPlayerValue("freeBooster", stringToDate(isolatedDate).toString());
            } else if(message.equals("[CaseOpening] Du hast 2 Kisten erhalten.")) {
                String rank = getPlayerRank(Laby.labyAPI().getName());
                int periodChest = getTimePeriodForFreeChest(rank);
                if(periodChest > -1)
                    FileManager.setPlayerValue("freeChest", LocalDateTime.now().plusDays(periodChest).toString());
            } else if(message.startsWith("Du hast 1 ") && message.endsWith("-Booster erhalten. Danke für deine Unterstützung von GrieferGames!")) {
                String rank = getPlayerRank(Laby.labyAPI().getName());
                int periodChest = getTimePeriodForFreeChest(rank);
                if(periodChest > -1)
                    FileManager.setPlayerValue("freeBooster", LocalDateTime.now().plusDays(periodChest).toString());
            } else if(message.startsWith("[Kopf] Du hast einen ") && message.endsWith("-Kopf erhalten!")) {
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