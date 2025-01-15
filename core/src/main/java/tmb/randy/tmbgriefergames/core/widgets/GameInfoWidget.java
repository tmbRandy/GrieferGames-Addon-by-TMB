package tmb.randy.tmbgriefergames.core.widgets;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.hud.hudwidget.text.Formatting;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.client.scoreboard.Scoreboard;
import net.labymod.api.client.scoreboard.ScoreboardTeam;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent.Side;
import net.labymod.api.event.client.scoreboard.ScoreboardTeamEntryAddEvent;
import net.labymod.api.event.client.scoreboard.ScoreboardTeamEntryRemoveEvent;
import net.labymod.api.event.client.scoreboard.ScoreboardTeamUpdateEvent;
import net.labymod.api.util.Color;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.widgets.GameInfoWidget.GameInfoWidgetConfig;

public class GameInfoWidget extends TextHudWidget<GameInfoWidgetConfig> {

    private final Icon widgetIcon = Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/gameinfo.png"));
    private final Icon serverIcon = Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/scoreboard/server.png"));
    private final Icon playersIcon = Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/scoreboard/players.png"));
    private final Icon moneyIcon = Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/scoreboard/money.png"));
    private final Icon bankIcon = Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/scoreboard/bank.png"));
    private final Icon hoursIcon = Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/scoreboard/hours.png"));
    private final Icon itemRemoverIcon = Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/scoreboard/itemremover.png"));
    private final Icon mobRemoverIcon = Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/scoreboard/mobremover.png"));

    private static final Pattern TIME_PATTERN = Pattern.compile("\\[MobRemover\\] Achtung! In (\\d+) Minute(n)? werden alle Tiere gel\\u00f6scht.");
    private static final Pattern REMOVED_PATTERN = Pattern.compile("\\[MobRemover\\] Es wurden .* Tiere entfernt.");

    private TextLine moneyLine;
    private TextLine bankLine;
    private TextLine cbLine;
    private TextLine playersLine;
    private TextLine playtimeLine;
    private TextLine itemRemoverLine;
    private TextLine mobRemoverLine;
    private static String moneyValue = "";
    private static String cbValue = "";
    private static String playersValue = "";
    private static String playtimeValue = "";

    private static Instant itemRemover = null;
    private static Instant mobRemover = null;

    private static long bankAmount = -1;

    public GameInfoWidget(HudWidgetCategory category) {
        super("gameinfo", GameInfoWidgetConfig.class);
        setIcon(widgetIcon);
        this.bindCategory(category);
    }

    public void load(GameInfoWidgetConfig config) {
        super.load(config);

        config.showBank.addChangeListener(() -> {
            bankLine.setState(config.getShowBank().get() ? State.VISIBLE : State.HIDDEN);
        });

        moneyLine = createLine(Component.icon(moneyIcon), 0, TextLine::new);
        bankLine = createLine(Component.icon(bankIcon), 0, TextLine::new);
        cbLine = createLine(Component.icon(serverIcon), 0, TextLine::new);
        playersLine = createLine(Component.icon(playersIcon), 0, TextLine::new);
        playtimeLine = createLine(Component.icon(hoursIcon), 0, TextLine::new);
        itemRemoverLine = createLine(Component.icon(itemRemoverIcon), 0, TextLine::new);
        mobRemoverLine = createLine(Component.icon(mobRemoverIcon), 0, TextLine::new);
        update();

        //Avoid tinting the icons and rendering brackets around them
        this.config.useGlobal().set(false);
        this.config.labelColor().set(Color.WHITE);
        this.config.formatting().set(Formatting.NONE);
    }

    private void update() {
        moneyLine.updateAndFlush(moneyValue);
        bankLine.updateAndFlush(config.getShowBank().get() ? formatAsCurrency(bankAmount) : "?");
        cbLine.updateAndFlush(cbValue);
        playersLine.updateAndFlush(playersValue);
        playtimeLine.updateAndFlush(playtimeValue);
        itemRemoverLine.updateAndFlush(getTimeString(itemRemover));
        mobRemoverLine.updateAndFlush(getTimeString(mobRemover));
    }


    @Subscribe
    public void tickEvent(GameTickEvent event) {
        update();
    }

    @Subscribe
    public void scoreboardAddTeam(ScoreboardTeamEntryAddEvent event) {
        readScoreboard();
        update();
    }

    @Subscribe
    public void scoreboardRemoveTeam(ScoreboardTeamEntryRemoveEvent event) {
        readScoreboard();
        update();
    }

    @Subscribe
    public void scoreboardUpdateTeam(ScoreboardTeamUpdateEvent event) {
        readScoreboard();
        update();
    }

    private void readScoreboard() {
        Scoreboard scoreboard = Laby.labyAPI().minecraft().getScoreboard();

        if (scoreboard == null) {
            return;
        }

        for (ScoreboardTeam team : scoreboard.getTeams()) {
            if (team.getTeamName().equals("money_value")) {
                moneyValue = ((TextComponent) team.getPrefix()).getText();
            } else if (team.getTeamName().equals("online_value")) {
                playersValue = ((TextComponent) team.getPrefix()).getText();
            } else if (team.getTeamName().equals("playtime_value")) {
                playtimeValue = ((TextComponent) team.getPrefix()).getText();
            } else if (team.getTeamName().equals("server_value")) {
                cbValue = ((TextComponent) team.getPrefix()).getText();
            }
        }
    }

    @Subscribe
    public void networkPayloadEvent(NetworkPayloadEvent event) {
        if (!Addon.isGG()) {
            return;
        }

        if (event.side() == Side.RECEIVE) {
            byte[] packetBuffer = event.getPayload().clone();
            String payloadString = new String(packetBuffer, StandardCharsets.UTF_8);

            if (payloadString.contains("countdown_create")) {
                int itemRemoverSec = extractItemRemoverValue(payloadString);
                itemRemover = Instant.now().plus(Duration.ofSeconds(itemRemoverSec));
            } else if (payloadString.contains("bank")) {
                bankAmount = extractBankValue(payloadString);
            }
        }
    }

    public static String formatAsCurrency(long value) {
        if (value < 0) {
            return "?";
        }

        NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.GERMAN);

        DecimalFormat decimalFormat = (DecimalFormat) currencyFormatter;
        decimalFormat.applyPattern("#,##0.00");

        return decimalFormat.format(value) + "$";
    }

    @Override
    public boolean isVisibleInGame() {
        return Addon.isGG();
    }

    private int extractItemRemoverValue(String input) {
        String pattern = "until\":(\\d+)";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(input);

        if (matcher.find()) {
            String untilValueStr = matcher.group(1);
            return Integer.parseInt(untilValueStr);
        }

        return -1;
    }

    private long extractBankValue(String input) {
        String sanitizedInput = input.replaceAll("[^\\x20-\\x7E]", "");

        String pattern = "amount\":\"(\\d+)\"";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(sanitizedInput);

        if (matcher.find()) {
            String amountValueStr = matcher.group(1);
            return Integer.parseInt(amountValueStr);
        }

        return -1;
    }


    @Subscribe
    public void onMessageReceiveEvent(ChatReceiveEvent event) {
        if (!Laby.labyAPI().minecraft().isIngame()
            || Laby.labyAPI().minecraft().getClientPlayer() == null) {
            return;
        }

        String message = event.chatMessage().getPlainText();
        Instant now = Instant.now();

        Matcher timeMatcher = TIME_PATTERN.matcher(message);
        if (timeMatcher.matches()) {
            int minutes = Integer.parseInt(timeMatcher.group(1));
            mobRemover = now.plus(Duration.ofMinutes(minutes));
        } else if (REMOVED_PATTERN.matcher(message).matches()) {
            mobRemover = now.plus(Duration.ofMinutes(15));
        }
    }

    public static String getTimeString(Instant remover) {

        long remainingMs =
            remover == null ? -1000000L : Duration.between(Instant.now(), remover).toMillis();

        if (remainingMs == -1000000L) {
            return "?";
        }
        if (remainingMs <= 0) {
            return "0:00";
        }

        long totalSeconds = remainingMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    @Subscribe
    void cbChanged(CbChangedEvent event) {
        mobRemover = null;
    }

    public static class GameInfoWidgetConfig extends TextHudWidgetConfig {

        @SwitchSetting
        private final ConfigProperty<Boolean> showBank = new ConfigProperty<>(true);

        public ConfigProperty<Boolean> getShowBank() {return showBank;}
    }
}