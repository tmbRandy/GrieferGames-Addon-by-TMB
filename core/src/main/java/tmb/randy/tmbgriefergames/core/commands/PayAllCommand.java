package tmb.randy.tmbgriefergames.core.commands;

import net.labymod.api.Laby;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.scoreboard.Scoreboard;
import net.labymod.api.client.scoreboard.ScoreboardTeam;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.helper.I19n;

public class PayAllCommand extends DescribedCommand {
    public PayAllCommand() {
        super("pay");
    }

    @Override
    public boolean execute(String prefix, String[] arguments) {
        if(!Addon.isGG())
            return false;

        if(arguments.length == 2) {
            if((arguments[0].equals("**") || arguments[0].equals("/")) && Float.parseFloat(arguments[1]) >= 1F) {
                String playersString = getPlayersString();
                int players = Integer.parseInt(playersString);
                if(players > 1) {
                    float totalAmount = Float.parseFloat(arguments[1]);
                    int perPlayer = (int) (totalAmount / (float) players);
                    int realTotalAmount = players * perPlayer;

                    if(perPlayer < 1) {
                        Addon.getSharedInstance().displayNotification(I19n.translate("payAll.amountTooLow"));
                        return false;
                    }

                    Laby.labyAPI().minecraft().chatExecutor().chat("/pay * " + perPlayer);
                    Addon.getSharedInstance().displayNotification(I19n.translate("payAll.response", players, perPlayer, realTotalAmount));
                }
                return true;
            }
        }

        return false;
    }

    private String getPlayersString() {
        Scoreboard scoreboard = Laby.labyAPI().minecraft().getScoreboard();

        if(scoreboard == null)
            return "";

        for (ScoreboardTeam team : scoreboard.getTeams()) {
            if(team.getTeamName().equals("online_value")) {
                String onlineString = ((TextComponent)team.getPrefix()).getText();
                return onlineString.split("/")[0];
            }
        }
        return "";
    }
}

