package tmb.randy.tmbgriefergames.core.helper;

import net.labymod.api.Laby;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.scoreboard.Scoreboard;
import net.labymod.api.client.scoreboard.ScoreboardTeam;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.scoreboard.ScoreboardTeamUpdateEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.CBs;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import javax.inject.Singleton;

@Singleton
public class CBtracker {
    private static CBs currentCB = CBs.NONE;

    @Subscribe
    public void worldEnterEvent(ScoreboardTeamUpdateEvent event) {
        if(!Addon.isGG()) return;
        Scoreboard scoreboard = Laby.labyAPI().minecraft().getScoreboard();
            if(scoreboard != null) {
                for (ScoreboardTeam team : scoreboard.getTeams()) {
                    if(team.getTeamName().equals("server_value")) {
                        String CBString = ((TextComponent)team.getPrefix()).getText();

                        CBs newCB = CBs.NONE;

                        try {
                            newCB = CBs.valueOf(CBString.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            Addon.getSharedInstance().logger().warn(e.getMessage());
                        }

                        if(newCB != currentCB) {
                            currentCB = newCB;
                            Addon.getSharedInstance().logger().info("Joined " + newCB.getName());
                            Laby.fireEvent(new CbChangedEvent(newCB));
                        }
                    }
                }
            }
    }

    public static CBs getCurrentCB() {return currentCB;}
    public static boolean isNatureWorldCB() {return currentCB == CBs.NATURE || currentCB == CBs.EXTREME;}
    public static boolean isPlotworldCB() {
        return switch (currentCB) {
            case WASSER, LAVA, PORTAL, LOBBY, NONE -> false;
            default -> true;
        };
    }
    public static boolean isCommandAbleCB() {
        return switch (currentCB) {
            case PORTAL, LOBBY, NONE -> false;
            default -> true;
        };
    }

    public static boolean isPlotworldCB(CBs cb) {
        return switch (cb) {
            case WASSER, LAVA, PORTAL, LOBBY, NONE -> false;
            default -> true;
        };
    }
}
