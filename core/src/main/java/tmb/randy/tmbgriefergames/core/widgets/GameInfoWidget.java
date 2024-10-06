package tmb.randy.tmbgriefergames.core.widgets;

import net.labymod.api.Laby;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.client.scoreboard.Scoreboard;
import net.labymod.api.client.scoreboard.ScoreboardTeam;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;

public class GameInfoWidget extends TextHudWidget<TextHudWidgetConfig> {

    private TextLine moneyLine;
    private TextLine cbLine;
    private TextLine playersLine;
    private TextLine playtimeLine;
    private static String moneyValue = "";
    private static String cbValue = "";
    private static String playersValue = "";
    private static String playtimeValue = "";

    public GameInfoWidget(HudWidgetCategory category) {
        super("gameinfo");
        setIcon(Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/gameinfo.png")));
        this.bindCategory(category);
    }

    public void load(TextHudWidgetConfig config) {
        super.load(config);
        this.moneyLine = this.createLine(I18n.getTranslation("tmbgriefergames.gameInfo.money"), 0, TextLine::new);
        this.cbLine = this.createLine(I18n.getTranslation("tmbgriefergames.gameInfo.cb"), 0, TextLine::new);
        this.playersLine = this.createLine(I18n.getTranslation("tmbgriefergames.gameInfo.players"), 0, TextLine::new);
        this.playtimeLine = this.createLine(I18n.getTranslation("tmbgriefergames.gameInfo.playtime"), 0, TextLine::new);
        this.update();
    }

    private void update() {
        this.moneyLine.updateAndFlush(moneyValue);
        this.cbLine.updateAndFlush(cbValue);
        this.playersLine.updateAndFlush(playersValue);
        this.playtimeLine.updateAndFlush(playtimeValue);
    }

    public void onTick(boolean isEditorContext) {
        readScoreboard();
        this.update();
    }

    private void readScoreboard() {
        Scoreboard scoreboard = Laby.labyAPI().minecraft().getScoreboard();

        if(scoreboard == null)
            return;

        for (ScoreboardTeam team : scoreboard.getTeams()) {
            if(team.getTeamName().equals("money_value")) {
                moneyValue = ((TextComponent)team.getPrefix()).getText();
            } else if(team.getTeamName().equals("online_value")) {
                playersValue = ((TextComponent)team.getPrefix()).getText();
            } else if(team.getTeamName().equals("playtime_value")) {
                playtimeValue = ((TextComponent)team.getPrefix()).getText();
            } else if(team.getTeamName().equals("server_value")) {
                cbValue = ((TextComponent)team.getPrefix()).getText();
            }
        }
    }

    @Override
    public boolean isVisibleInGame() {
        return Addon.isGG();
    }
}