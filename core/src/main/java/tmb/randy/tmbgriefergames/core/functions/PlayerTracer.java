package tmb.randy.tmbgriefergames.core.functions;

import java.util.List;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.scoreboard.Scoreboard;
import net.labymod.api.client.scoreboard.ScoreboardTeam;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.api.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.api.events.ToggleFunctionEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.helper.BaseHelper;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;

/** /fahndung: hops through all CBs, optionally until a given player is found. */
public class PlayerTracer extends ActiveFunction {
    public static final List<CBs> CBlist = List.of(CBs.NONE, CBs.CB1, CBs.CB22, CBs.CB21, CBs.CB20, CBs.CB7, CBs.NATURE, CBs.CB2, CBs.CB3, CBs.CB4, CBs.CB5, CBs.CB6, CBs.CB8, CBs.CB9, CBs.CB10, CBs.CB11, CBs.CB12, CBs.CB13, CBs.CB14, CBs.CB15, CBs.CB16, CBs.CB17, CBs.CB18, CBs.CB19, CBs.EXTREME, CBs.CBE);

    private static final String KICKED_TOO_FAST = "Kicked whilst connecting to connector: Du hast dich zu schnell wieder eingeloggt. Versuche es später erneut.";
    private static final long RETRY_DELAY_MS = 1000;
    private static final long SWITCH_DELAY_MS = 6000;

    private int nextServer = 0;
    private String playerName;

    public PlayerTracer() {
        super(Functions.PLAYERTRACER.name());
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (isEnabled() && event.state() == State.PRESS && (event.key() == Key.W || event.key() == Key.A || event.key() == Key.S || event.key() == Key.D))
            stop();
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if (!isEnabled())
            return;

        String message = event.chatMessage().getPlainText();
        if (message.endsWith(KICKED_TOO_FAST)) {
            event.setCancelled(true);
            BaseHelper.schedule(this::switchToNextServer, RETRY_DELAY_MS);
        } else if (isBetween(message, Const.Chat.AUTOSWITCH_START, Const.Chat.AUTOSWITCH_END) || isBetween(message, Const.Chat.SERVERSWITCH_START, Const.Chat.SERVERSWITCH_END)) {
            event.setCancelled(true);
        } else if (message.equals(Const.Chat.SWITCHER_DATA_DOWNLOADED) && CBtracker.isPlotworldCB()) {
            if (isPlayerOnCB()) {
                Addon.displayNotification(Addon.translate("playerTracer.foundPlayer"));
                stop();
            } else if (nextServer + 1 < CBlist.size()) {
                nextServer++;
                BaseHelper.schedule(() -> {
                    if (isEnabled())
                        switchToNextServer();
                }, SWITCH_DELAY_MS);
            } else {
                stop();
            }
        }
    }

    @Override
    public void toggleFunctionEvent(ToggleFunctionEvent event) {
        if (event.function().equals(identifier))
            toggle(event.arguments());
    }

    @Override
    public boolean start(String[] arguments) {
        String[] args = arguments != null ? arguments : new String[0];
        if (args.length > 1) {
            Addon.displayNotification(Addon.translate("playerTracer.tooManyArguments"));
            return false;
        }

        if (!super.start(arguments))
            return false;

        if (args.length == 1) {
            playerName = args[0].toLowerCase();
            Addon.displayNotification(Addon.translate("playerTracer.lookingForPlayer", args[0]));
        } else {
            Addon.displayNotification(Addon.translate("playerTracer.startedHopping"));
        }

        nextServer = 1;
        switchToNextServer();
        return true;
    }

    @Override
    public boolean stop() {
        if (!super.stop())
            return false;

        playerName = null;
        nextServer = 0;
        return true;
    }

    @Override
    public void cbChangedEvent(CbChangedEvent event) {
        // Stays enabled while switching CBs
    }

    private void switchToNextServer() {
        Laby.labyAPI().minecraft().chatExecutor().chat(Const.Cmd.SWITCH + CBlist.get(nextServer));
    }

    private static boolean isBetween(String message, String prefix, String suffix) {
        return message.startsWith(prefix) && message.endsWith(suffix);
    }

    private boolean isPlayerOnCB() {
        Scoreboard scoreboard = Laby.labyAPI().minecraft().getScoreboard();
        if (playerName == null || scoreboard == null)
            return false;

        for (ScoreboardTeam team : scoreboard.getTeams()) {
            for (String member : team.getEntries()) {
                if (member.equalsIgnoreCase(playerName))
                    return true;
            }
        }
        return false;
    }
}
