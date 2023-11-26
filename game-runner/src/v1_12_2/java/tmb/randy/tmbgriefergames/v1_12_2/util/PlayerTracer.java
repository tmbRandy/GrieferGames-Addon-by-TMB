package tmb.randy.tmbgriefergames.v1_12_2.util;

import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.scoreboard.ScoreboardTeam;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PlayerTracer {
    public static final List<String> CBlist = Arrays.asList("None", "CB1", "CB22", "CB21", "CB20", "CB7", "Nature", "CB2", "CB3", "CB4", "CB5", "CB6", "CB8", "CB9", "CB10", "CB11", "CB12", "CB13", "CB14", "CB15", "CB16", "CB17", "CB18", "CB19", "Extreme", "CBEvil");
    private int nextServer = 0;
    private String playerName;

    public void onKey(KeyEvent event) {
        if(isTracerActive() && event.state() == State.PRESS && (event.key() == Key.W || event.key() == Key.A || event.key() == Key.S || event.key() == Key.D)) {
            stopTracer();
        }
    }

    public void messageReceived(ChatReceiveEvent event) {
        String msg = event.chatMessage().getPlainText();
        if(msg.endsWith("Kicked whilst connecting to connector: Du hast dich zu schnell wieder eingeloggt. Versuche es später erneut.") && isTracerActive()) {
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Laby.labyAPI().minecraft().chatExecutor().chat("/switch " + CBlist.get(nextServer));
                    }
                },
                1000
            );
            event.setCancelled(true);
        } else if(isTracerActive() && ((msg.startsWith("[GrieferGames] Du wurdest automatisch auf ") && msg.endsWith(" verbunden.") || (msg.startsWith("[GrieferGames] Serverwechsel auf ") && msg.endsWith(" wurde gestartet..")))))  {
            event.setCancelled(true);
        }
    }
    public void cbChanged() {
        if(CBTracker.currentCB != null && !CBTracker.currentCB.equals("Portal") && !CBTracker.currentCB.equals("Lobby") && isTracerActive())  {
            if(isPlayerOnCB()) {
                Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.playerTracer.foundPlayer"));
                stopTracer();
            } else if(nextServer < CBlist.size()) {
                nextServer++;
                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Laby.labyAPI().minecraft().chatExecutor().chat("/switch " + CBlist.get(nextServer));
                        }
                    },
                    4500
                );
            } else {
                stopTracer();
            }
        }
    }

    public void startTracer(String name) {
        if(name != null) {
            playerName = name.toLowerCase();
        }
        nextServer = 1;
        Laby.labyAPI().minecraft().chatExecutor().chat("/switch " + CBlist.get(nextServer));
    }

    public void stopTracer() {
        playerName = null;
        nextServer = 0;
        Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.playerTracer.tracingEnded"));
    }

    private boolean isPlayerOnCB() {
        if(playerName == null) {
            return false;
        }

        for (ScoreboardTeam team : Laby.labyAPI().minecraft().getScoreboard().getTeams()) {
            Collection<String> members = team.getEntries();
            for (String member : members) {
                if(member.equalsIgnoreCase(playerName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isTracerActive() {
        return (nextServer != 0);
    }
}
