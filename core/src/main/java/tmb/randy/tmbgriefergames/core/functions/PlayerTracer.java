package tmb.randy.tmbgriefergames.core.functions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.scoreboard.Scoreboard;
import net.labymod.api.client.scoreboard.ScoreboardTeam;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.CBs;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.events.ToggleFunctionEvent;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.helper.I19n;

public class PlayerTracer extends ActiveFunction {
    public static final List<CBs> CBlist = Arrays.asList(CBs.NONE, CBs.CB1, CBs.CB22, CBs.CB21, CBs.CB20, CBs.CB7, CBs.NATURE, CBs.CB2, CBs.CB3, CBs.CB4, CBs.CB5, CBs.CB6, CBs.CB8, CBs.CB9, CBs.CB10, CBs.CB11, CBs.CB12, CBs.CB13, CBs.CB14, CBs.CB15, CBs.CB16, CBs.CB17, CBs.CB18, CBs.CB19, CBs.EXTREME, CBs.CBE);
    private int nextServer = 0;
    private String playerName;

    public PlayerTracer() {
        super(Functions.PLAYERTRACER);
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if(isEnabled() && event.state() == State.PRESS && (event.key() == Key.W || event.key() == Key.A || event.key() == Key.S || event.key() == Key.D)) {
            stop();
        }
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        String msg = event.chatMessage().getPlainText();
        if(msg.endsWith("Kicked whilst connecting to connector: Du hast dich zu schnell wieder eingeloggt. Versuche es später erneut.") && isEnabled()) {
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
        } else if(isEnabled() && ((msg.startsWith("[GrieferGames] Du wurdest automatisch auf ") && msg.endsWith(" verbunden.") || (msg.startsWith("[GrieferGames] Serverwechsel auf ") && msg.endsWith(" wurde gestartet..")))))  {
            event.setCancelled(true);
        } else if (msg.equals("[Switcher] Daten heruntergeladen!")) {
            if(!Addon.isGG() || !isEnabled()) return;

            if(CBtracker.isPlotworldCB())  {
                if(isPlayerOnCB()) {
                    Addon.getSharedInstance().displayNotification(I19n.translate("playerTracer.foundPlayer"));
                    stop();
                } else if(nextServer < CBlist.size()) {
                    nextServer++;
                    new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                if (isEnabled())
                                    Laby.labyAPI().minecraft().chatExecutor().chat("/switch " + CBlist.get(nextServer));
                            }
                        },
                        6000
                    );
                } else
                    stop();
            }
        }
    }

    @Override
    public void toggleFunctionEvent(ToggleFunctionEvent event) {
        if(event.function() == type)
            toggle(event.arguments());
    }

    @Override
    public boolean start(String[] arguments) {
        if(super.start(arguments)) {
            String[] args = arguments != null ? arguments : new String[0];

            switch (args.length) {
                case 0 -> {
                    Addon.getSharedInstance().displayNotification(I19n.translate("playerTracer.startedHopping"));
                    nextServer = 1;
                    Laby.labyAPI().minecraft().chatExecutor().chat("/switch " + CBlist.get(nextServer));
                    return true;
                }
                case 1 -> {
                    Addon.getSharedInstance().displayNotification(I19n.translate("playerTracer.lookingForPlayer", arguments[0]));
                    playerName = args[0].toLowerCase();
                    nextServer = 1;
                    Laby.labyAPI().minecraft().chatExecutor().chat("/switch " + CBlist.get(nextServer));
                    return true;
                }
                default -> Addon.getSharedInstance().displayNotification(I19n.translate("playerTracer.tooManyArguments"));
            }
        }

        return false;
    }

    @Override
    public boolean stop() {
        if(super.stop()) {
            playerName = null;
            nextServer = 0;
            return true;
        }
        return false;
    }

    // Override to avoid stop() while switching CBs
    @Override
    public void cbChangedEvent(CbChangedEvent event) {}

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
}
