package tmb.randy.tmbgriefergames.core.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.inject.Singleton;
import net.labymod.api.Laby;
import net.labymod.api.client.component.serializer.plain.PlainTextComponentSerializer;
import net.labymod.api.client.scoreboard.DisplaySlot;
import net.labymod.api.client.scoreboard.Scoreboard;
import net.labymod.api.client.scoreboard.ScoreboardObjective;
import net.labymod.api.client.scoreboard.ScoreboardScore;
import net.labymod.api.client.scoreboard.ScoreboardTeam;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.scoreboard.ScoreboardTeamUpdateEvent;
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.api.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.Addon;

@Singleton
public class CBtracker {
    private static CBs currentCB = CBs.NONE;

    @Subscribe
    public void worldEnterEvent(ScoreboardTeamUpdateEvent event) {
        if(!Addon.isGG()) return;
        Scoreboard scoreboard = Laby.labyAPI().minecraft().getScoreboard();
        if(scoreboard == null) return;

        ScoreboardObjective sidebar = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if(sidebar == null) return;

        CBs newCB = findCB(scoreboard, sidebar);

        if(newCB != currentCB) {
            currentCB = newCB;
            Addon.log().info("Joined " + newCB.getName());
            Laby.fireEvent(new CbChangedEvent(newCB));
        }
    }

    private static CBs findCB(Scoreboard scoreboard, ScoreboardObjective sidebar) {
        List<ScoreboardScore> scores = new ArrayList<>(scoreboard.getScores(sidebar));
        scores.sort(Comparator.comparingInt(ScoreboardScore::getValue).reversed()
            .thenComparing(ScoreboardScore::getName));

        for (ScoreboardScore score : scores) {
            String cleaned = renderLine(scoreboard, score).replaceAll("§.", "").trim().toUpperCase();
            if(cleaned.isEmpty()) continue;

            try {
                return CBs.valueOf(cleaned);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return CBs.NONE;
    }

    private static String renderLine(Scoreboard scoreboard, ScoreboardScore score) {
        ScoreboardTeam team = scoreboard.teamByEntry(score.getName());
        if(team == null) return score.getName();

        return PlainTextComponentSerializer.plainText().serialize(team.getPrefix())
            + score.getName()
            + PlainTextComponentSerializer.plainText().serialize(team.getSuffix());
    }

    public static CBs getCurrentCB() {
        return currentCB;
    }

    public static boolean isNatureWorldCB() {
        return currentCB == CBs.NATURE || currentCB == CBs.EXTREME;
    }

    public static boolean isCommandAbleCB() {
        return switch (currentCB) {
            case PORTAL, LOBBY, NONE -> false;
            default -> true;
        };
    }

    public static boolean isPlotworldCB() {
        return isPlotworldCB(currentCB);
    }

    public static boolean isPlotworldCB(CBs cb) {
        return switch (cb) {
            case WASSER, LAVA, PORTAL, LOBBY, NONE -> false;
            default -> true;
        };
    }

    /** All plot world CBs a player can switch to, in enum order. */
    public static List<CBs> getSwitchablePlotworldCBs() {
        return Arrays.stream(CBs.values()).filter(cb -> cb != CBs.EVENT && isPlotworldCB(cb)).toList();
    }
}
