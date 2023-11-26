package tmb.randy.tmbgriefergames.v1_12_2.util;

import net.labymod.api.Laby;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.scoreboard.Scoreboard;
import net.labymod.api.client.scoreboard.ScoreboardTeam;
import net.labymod.api.event.client.world.WorldLoadEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import java.util.Arrays;
import java.util.List;

public class CBTracker {

    public static final List<String> CBs = Arrays.asList(
        "Lobby",
        "Portal",
        "Extreme",
        "CBE",
        "Nature",
        "CB1",
        "CB2",
        "CB3",
        "CB4",
        "CB5",
        "CB6",
        "CB7",
        "CB8",
        "CB9",
        "CB10",
        "CB11",
        "CB12",
        "CB13",
        "CB14",
        "CB15",
        "CB16",
        "CB17",
        "CB18",
        "CB19",
        "CB20",
        "CB21",
        "CB22",
        "Lava",
        "Wasser",
        "Event"
    );

    public static String currentCB = "";

    public void worldLoadEvent(WorldLoadEvent event) {
        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    Scoreboard scoreboard = Laby.labyAPI().minecraft().getScoreboard();
                    for (ScoreboardTeam team : scoreboard.getTeams()) {
                        if(team.getTeamName().equals("server_value")) {
                            String CBString = ((TextComponent)team.getPrefix()).getText();
                            if(!CBString.equals(currentCB) && CBs.contains(CBString)) {
                                ItemClearTimerListener.resetItemRemover();
                                currentCB = CBString;
                                Addon.getSharedInstance().getBridge().cbChanged();

                                if(currentCB.equals("Lobby") && Addon.getSharedInstance().configuration().getSkipHub().get()) {
                                    Laby.labyAPI().minecraft().chatExecutor().chat("/portal");
                                }
                            }
                        }
                    }
                }
            }, 600
        );
    }
}