package tmb.randy.tmbgriefergames.core.util;

import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.scoreboard.Scoreboard;
import net.labymod.api.client.scoreboard.ScoreboardTeam;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.CBtracker;
import tmb.randy.tmbgriefergames.core.enums.CBs;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PlayerTracer {
    public static final List<CBs> CBlist = Arrays.asList(CBs.NONE, CBs.CB1, CBs.CB22, CBs.CB21, CBs.CB20, CBs.CB7, CBs.NATURE, CBs.CB2, CBs.CB3, CBs.CB4, CBs.CB5, CBs.CB6, CBs.CB8, CBs.CB9, CBs.CB10, CBs.CB11, CBs.CB12, CBs.CB13, CBs.CB14, CBs.CB15, CBs.CB16, CBs.CB17, CBs.CB18, CBs.CB19, CBs.EXTREME, CBs.CBE);
    private int nextServer = 0;
    private String playerName;
    private boolean tracerActive;

    @Subscribe
    public void onKey(KeyEvent event) {
        if(!Addon.isGG())
            return;

        if(tracerActive && event.state() == State.PRESS && (event.key() == Key.W || event.key() == Key.A || event.key() == Key.S || event.key() == Key.D)) {
            stopTracer();
        }
    }

    @Subscribe
    public void messageReceived(ChatReceiveEvent event) {
        if(!Addon.isGG())
            return;

        String msg = event.chatMessage().getPlainText();
        if(msg.endsWith("Kicked whilst connecting to connector: Du hast dich zu schnell wieder eingeloggt. Versuche es später erneut.") && tracerActive) {
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
        } else if(tracerActive && ((msg.startsWith("[GrieferGames] Du wurdest automatisch auf ") && msg.endsWith(" verbunden.") || (msg.startsWith("[GrieferGames] Serverwechsel auf ") && msg.endsWith(" wurde gestartet..")))))  {
            event.setCancelled(true);
        } else if (msg.equals("[Switcher] Daten heruntergeladen!")) {
            if(!Addon.isGG() || !tracerActive) return;

            if(CBtracker.isPlotworldCB())  {
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
                        6000
                    );
                } else {
                    stopTracer();
                }
            }
        }
    }

    public void startTracer(String name) {
        tracerActive = true;

        if(name != null) {
            playerName = name.toLowerCase();
        }
        nextServer = 1;
        Laby.labyAPI().minecraft().chatExecutor().chat("/switch " + CBlist.get(nextServer));
    }

    public void stopTracer() {
        tracerActive = false;

        playerName = null;
        nextServer = 0;
        Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.playerTracer.tracingEnded"));
    }

    private boolean isPlayerOnCB() {
        if(playerName == null) {
            return false;
        }

        Scoreboard scoreboard = Laby.labyAPI().minecraft().getScoreboard();

        if(scoreboard != null) {
            for (ScoreboardTeam team : scoreboard.getTeams()) {
                Collection<String> members = team.getEntries();

                for (String member : members) {
                    if(member.equalsIgnoreCase(playerName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isTracerDisabled() {
        return !tracerActive;
    }
}
