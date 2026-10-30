package tmb.randy.tmbgriefergames.core.widgets;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.serializer.plain.PlainTextComponentSerializer;
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
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent.Side;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.render.overlay.IngameOverlayElementRenderEvent;
import net.labymod.api.event.client.render.overlay.IngameOverlayElementRenderEvent.OverlayElementType;
import net.labymod.api.event.client.scoreboard.ScoreboardTeamEntryAddEvent;
import net.labymod.api.event.client.scoreboard.ScoreboardTeamEntryRemoveEvent;
import net.labymod.api.event.client.scoreboard.ScoreboardTeamUpdateEvent;
import net.labymod.api.util.Color;
import tmb.randy.tmbgriefergames.api.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.api.events.MoneyBalanceChangedEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.widgets.GameInfoWidget.GameInfoWidgetConfig;

/** Replaces the GrieferGames scoreboard with a widget (money, bank, CB, players, playtime, item/mob remover). */
public class GameInfoWidget extends TextHudWidget<GameInfoWidgetConfig> {
    private static final Pattern MOB_REMOVER_WARNING = Pattern.compile("\\[MobRemover\\] Achtung! In (\\d+) Minute(n)? werden alle Tiere gel\\u00f6scht.");
    private static final Pattern MOB_REMOVER_DONE = Pattern.compile("\\[MobRemover\\] Es wurden .* Tiere entfernt.");
    private static final Pattern ITEM_REMOVER_SECONDS = Pattern.compile("until\":(\\d+)");
    private static final Pattern BANK_AMOUNT = Pattern.compile("amount\":\"(\\d+)\"");
    private static final Duration MOB_REMOVER_INTERVAL = Duration.ofMinutes(15);

    private static String moneyValue = "";
    private static String cbValue = "";
    private static String playersValue = "";
    private static String playtimeValue = "";
    private static long bankAmount = -1;
    private static Instant itemRemover = null;
    private static Instant mobRemover = null;

    private TextLine moneyLine;
    private TextLine bankLine;
    private TextLine cbLine;
    private TextLine playersLine;
    private TextLine playtimeLine;
    private TextLine itemRemoverLine;
    private TextLine mobRemoverLine;

    public GameInfoWidget(HudWidgetCategory category) {
        super("gameinfo", GameInfoWidgetConfig.class);
        setIcon(icon("gameinfo"));
        this.bindCategory(category);
    }

    private static Icon icon(String name) {
        return Icon.texture(ResourceLocation.create(Addon.getNamespace(), "textures/widgets/" + name + ".png"));
    }

    private TextLine createIconLine(String scoreboardIcon) {
        return createLine(Component.icon(icon("scoreboard/" + scoreboardIcon)), 0, TextLine::new);
    }

    @Override
    public void load(GameInfoWidgetConfig config) {
        super.load(config);

        config.showBank.addChangeListener(() -> bankLine.setState(config.getShowBank().get() ? State.VISIBLE : State.HIDDEN));

        moneyLine = createIconLine("money");
        bankLine = createIconLine("bank");
        cbLine = createIconLine("server");
        playersLine = createIconLine("players");
        playtimeLine = createIconLine("hours");
        itemRemoverLine = createIconLine("itemremover");
        mobRemoverLine = createIconLine("mobremover");
        update();

        // Avoid tinting the icons and rendering brackets around them
        this.config.useGlobal().set(false);
        this.config.labelColor().set(Color.WHITE);
        this.config.formatting().set(Formatting.NONE);
    }

    @Override
    public boolean isVisibleInGame() {
        return Addon.isGG();
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

    // ---------------------------------------------------------------- Events

    @Subscribe
    public void tickEvent(GameTickEvent event) {
        if (Addon.isGG())
            update();
    }

    @Subscribe
    public void scoreboardAddTeam(ScoreboardTeamEntryAddEvent event) {
        onScoreboardChanged();
    }

    @Subscribe
    public void scoreboardRemoveTeam(ScoreboardTeamEntryRemoveEvent event) {
        onScoreboardChanged();
    }

    @Subscribe
    public void scoreboardUpdateTeam(ScoreboardTeamUpdateEvent event) {
        onScoreboardChanged();
    }

    @Subscribe
    public void onScoreboardRender(IngameOverlayElementRenderEvent event) {
        if (Addon.isGG() && isEnabled() && event.elementType() == OverlayElementType.SCOREBOARD && isVisibleInGame() && event.phase() == Phase.PRE)
            event.setCancelled(true);
    }

    @Subscribe
    public void serverDisconnectEvent(ServerDisconnectEvent event) {
        bankAmount = -1;
    }

    @Subscribe
    public void cbChanged(CbChangedEvent event) {
        if (Addon.isGG())
            mobRemover = null;
    }

    @Subscribe
    public void networkPayloadEvent(NetworkPayloadEvent event) {
        if (!Addon.isGG() || event.side() != Side.RECEIVE)
            return;

        String payload = new String(event.getPayload(), StandardCharsets.UTF_8);
        if (payload.contains("countdown_create")) {
            Matcher matcher = ITEM_REMOVER_SECONDS.matcher(payload);
            itemRemover = Instant.now().plusSeconds(matcher.find() ? Long.parseLong(matcher.group(1)) : -1);
        } else if (payload.contains("bank")) {
            Matcher matcher = BANK_AMOUNT.matcher(payload.replaceAll("[^\\x20-\\x7E]", ""));
            bankAmount = matcher.find() ? Long.parseLong(matcher.group(1)) : -1;
        }
    }

    @Subscribe
    public void onMessageReceiveEvent(ChatReceiveEvent event) {
        if (!Addon.isGG())
            return;

        String message = event.chatMessage().getPlainText();
        Matcher warning = MOB_REMOVER_WARNING.matcher(message);
        if (warning.matches())
            mobRemover = Instant.now().plus(Duration.ofMinutes(Integer.parseInt(warning.group(1))));
        else if (MOB_REMOVER_DONE.matcher(message).matches())
            mobRemover = Instant.now().plus(MOB_REMOVER_INTERVAL);
    }

    // ---------------------------------------------------------------- Scoreboard

    private void onScoreboardChanged() {
        if (!Addon.isGG())
            return;

        readScoreboard();
        update();
    }

    private void readScoreboard() {
        Scoreboard scoreboard = Laby.labyAPI().minecraft().getScoreboard();
        if (scoreboard == null)
            return;

        for (ScoreboardTeam team : scoreboard.getTeams()) {
            String value = PlainTextComponentSerializer.plainText().serialize(team.getPrefix());
            switch (team.getTeamName()) {
                case "money_value" -> {
                    fireMoneyChange(moneyValue, value);
                    moneyValue = value;
                }
                case "online_value" -> playersValue = value;
                case "playtime_value" -> playtimeValue = value;
                case "server_value" -> cbValue = value;
            }
        }
    }

    private static void fireMoneyChange(String oldValue, String newValue) {
        try {
            double oldBalance = parseMoney(oldValue);
            double newBalance = parseMoney(newValue);
            if (newBalance != oldBalance)
                Laby.fireEvent(new MoneyBalanceChangedEvent(oldBalance, newBalance));
        } catch (NumberFormatException e) {
            // The scoreboard is not fully loaded yet
        }
    }

    // ---------------------------------------------------------------- Formatting

    private static double parseMoney(String input) {
        return Double.parseDouble(input.replace("$", "").replace(".", "").replace(",", "."));
    }

    public static String formatAsCurrency(long value) {
        if (value < 0)
            return "?";

        return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.GERMAN)).format(value) + "$";
    }

    public static String getTimeString(Instant remover) {
        if (remover == null)
            return "?";

        long remainingSeconds = Duration.between(Instant.now(), remover).toSeconds();
        if (remainingSeconds <= 0)
            return "0:00";

        return String.format("%d:%02d", remainingSeconds / 60, remainingSeconds % 60);
    }

    public static class GameInfoWidgetConfig extends TextHudWidgetConfig {

        @SwitchSetting
        private final ConfigProperty<Boolean> showBank = new ConfigProperty<>(true);

        public ConfigProperty<Boolean> getShowBank() {
            return showBank;
        }
    }
}
