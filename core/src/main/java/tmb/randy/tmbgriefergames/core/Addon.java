package tmb.randy.tmbgriefergames.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.screen.activity.types.IngameOverlayActivity;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.labymod.api.util.I18n;
import net.labymod.api.util.logging.Logging;
import org.jetbrains.annotations.NotNull;
import tmb.randy.tmbgriefergames.api.ITmbGGAddonAPI;
import tmb.randy.tmbgriefergames.api.TmbGGAddonAPI;
import tmb.randy.tmbgriefergames.api.enums.CBs;
import tmb.randy.tmbgriefergames.api.enums.FunctionState;
import tmb.randy.tmbgriefergames.api.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.api.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.api.functions.Function;
import tmb.randy.tmbgriefergames.core.activities.plotwheel.PlotWheelActivity;
import tmb.randy.tmbgriefergames.core.activities.plotwheel.PlotWheelPlot;
import tmb.randy.tmbgriefergames.core.commands.AutocraftV2Command;
import tmb.randy.tmbgriefergames.core.commands.AutocraftV3Command;
import tmb.randy.tmbgriefergames.core.commands.DKsCommand;
import tmb.randy.tmbgriefergames.core.commands.DescribedCommand;
import tmb.randy.tmbgriefergames.core.commands.EjectCommand;
import tmb.randy.tmbgriefergames.core.commands.FlowCommand;
import tmb.randy.tmbgriefergames.core.commands.PayAllCommand;
import tmb.randy.tmbgriefergames.core.commands.PlayerTracerCommand;
import tmb.randy.tmbgriefergames.core.config.Configuration;
import tmb.randy.tmbgriefergames.core.dev.DevBridge;
import tmb.randy.tmbgriefergames.core.flow.FlowManager;
import tmb.randy.tmbgriefergames.core.functions.AccountUnity;
import tmb.randy.tmbgriefergames.core.functions.AutoLoot;
import tmb.randy.tmbgriefergames.core.functions.ItemSaver;
import tmb.randy.tmbgriefergames.core.functions.PlotSwitch;
import tmb.randy.tmbgriefergames.core.functions.TooltipExtension;
import tmb.randy.tmbgriefergames.core.functions.chat.ChatCleaner;
import tmb.randy.tmbgriefergames.core.functions.chat.CooldownNotifier;
import tmb.randy.tmbgriefergames.core.functions.chat.MsgTabs;
import tmb.randy.tmbgriefergames.core.functions.chat.PaymentValidator;
import tmb.randy.tmbgriefergames.core.functions.chat.TypeCorrection;
import tmb.randy.tmbgriefergames.core.generated.DefaultReferenceStorage;
import tmb.randy.tmbgriefergames.core.helper.BaseHelper;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.helper.Commander;
import tmb.randy.tmbgriefergames.core.helper.HopperTracker;
import tmb.randy.tmbgriefergames.core.helper.ItemClearTimerListener;
import tmb.randy.tmbgriefergames.core.widgets.ActiveFunctionsWidget;
import tmb.randy.tmbgriefergames.core.widgets.AdventureWidget;
import tmb.randy.tmbgriefergames.core.widgets.GameInfoWidget;
import tmb.randy.tmbgriefergames.core.widgets.ItemClearWidget;
import tmb.randy.tmbgriefergames.core.widgets.NearbyWidget;
import tmb.randy.tmbgriefergames.core.widgets.PotionTimerWidget;

@AddonMain
public class Addon extends LabyAddon<Configuration> {
    private static final String ADDON_PREFIX = "§6[§5§l§oT§b§l§oM§5§l§oB§6] ";

    private static Addon INSTANCE;

    public static PlotWheelPlot queuedPlot = null;

    private IConnect connection;
    private final Set<DescribedCommand> commands = new HashSet<>();
    private final List<Function> functionList = new ArrayList<>(List.of(
        new PlotSwitch(),
        new ChatCleaner(),
        new CooldownNotifier(),
        new MsgTabs(),
        new PaymentValidator(),
        new TypeCorrection(),
        new ItemSaver(),
        new AccountUnity(),
        new TooltipExtension(),
        new AutoLoot()
    ));

    @Override
    protected void enable() {
        INSTANCE = this;
        connection = ((DefaultReferenceStorage) this.referenceStorageAccessor()).iConnect();
        connection.loadFunctions();
        registerSettingCategory();

        registerListener(new Listener());
        registerListener(connection);
        registerListener(new CBtracker());
        registerListener(new HopperTracker());
        registerListener(new ItemClearTimerListener());
        registerListener(Commander.INSTANCE());
        registerListener(FlowManager.get());
        if (DevBridge.isEnabled())
            registerListener(new DevBridge());
        registerListener(this);

        registerCommand(new DKsCommand());
        registerCommand(new PayAllCommand());
        registerCommand(new PlayerTracerCommand());
        registerCommand(new AutocraftV2Command());
        registerCommand(new AutocraftV3Command());
        registerCommand(new EjectCommand());
        registerCommand(new FlowCommand());

        HudWidgetCategory category = new HudWidgetCategory(getNamespace());
        labyAPI().hudWidgetRegistry().categoryRegistry().register(category);
        labyAPI().hudWidgetRegistry().register(new GameInfoWidget(category));
        labyAPI().hudWidgetRegistry().register(new PotionTimerWidget(category));
        labyAPI().hudWidgetRegistry().register(new ItemClearWidget(category));
        labyAPI().hudWidgetRegistry().register(new NearbyWidget(category));
        labyAPI().hudWidgetRegistry().register(new AdventureWidget(category));
        labyAPI().hudWidgetRegistry().register(new ActiveFunctionsWidget(category));

        TmbGGAddonAPI.setProvider(new ITmbGGAddonAPI() {
            @Override
            public void registerFunction(Function function) { Addon.registerFunction(function); }
            @Override
            public Function getFunction(String identifier) { return Addon.getFunction(identifier); }
            @Override
            public ActiveFunction getActiveFunction(String identifier) { return Addon.getActiveFunction(identifier); }
            @Override
            public List<Function> getFunctions() { return Addon.getFunctions(); }
            @Override
            public List<ActiveFunction> getActiveFunctions() { return Addon.getActiveFunctions(); }
        });

        this.logger().info("Enabled the Addon");
    }

    @Override
    protected Class<Configuration> configurationClass() {
        return Configuration.class;
    }

    public void registerCommand(DescribedCommand command) {
        commands.add(command);
        super.registerCommand(command);
    }

    // ---------------------------------------------------------------- Events

    @Subscribe
    public void keyInput(KeyEvent event) {
        if (event.state() == State.PRESS && event.key() == settings().getPlotSwitchSubConfig().getPlotWheelHotkey().get() && !isChatGuiOpen() && CBtracker.isCommandAbleCB() && isGG())
            Laby.labyAPI().minecraft().minecraftWindow().displayScreen(new PlotWheelActivity());
    }

    @Subscribe
    public void cbChanged(CbChangedEvent event) {
        CBs skipHub = settings().getSkipHub().get();
        if (event.CB() != CBs.LOBBY || skipHub == CBs.NONE || skipHub == CBs.LOBBY || !isGG())
            return;

        BaseHelper.schedule(() -> {
            CBs target = settings().getSkipHub().get();
            if (target == CBs.PORTAL)
                Commander.queue(Const.Cmd.PORTAL);
            else if (target != CBs.NONE)
                Commander.queue(Const.Cmd.SWITCH + target);
        }, 800);
    }

    @Subscribe
    public void messageReceived(ChatReceiveEvent event) {
        if (queuedPlot == null || !isGG() || !event.chatMessage().getPlainText().equals(Const.Chat.SWITCHER_DATA_DOWNLOADED) || !CBtracker.isPlotworldCB(CBtracker.getCurrentCB()))
            return;

        BaseHelper.schedule(() -> {
            if (queuedPlot != null && isGG() && (queuedPlot.cb() == CBs.NONE || CBtracker.getCurrentCB() == queuedPlot.cb())) {
                Laby.references().chatExecutor().chat(queuedPlot.command());
                queuedPlot = null;
            }
        }, 500);
    }

    // ---------------------------------------------------------------- Static access

    public static Configuration settings() {
        return INSTANCE.configuration();
    }

    public static Logging log() {
        return INSTANCE.logger();
    }

    public static IConnect getConnection() {
        return INSTANCE.connection;
    }

    public static String getNamespace() {
        return INSTANCE.addonInfo().getNamespace();
    }

    public static Set<DescribedCommand> getCommands() {
        return INSTANCE.commands;
    }

    public static void displayNotification(String msg) {
        Laby.labyAPI().minecraft().chatExecutor().displayClientMessage(ADDON_PREFIX + msg);
    }

    public static @NotNull String translate(@NotNull String key, Object... args) {
        return I18n.translate(getNamespace() + "." + key, args);
    }

    public static boolean isGG() {
        if (!Laby.labyAPI().serverController().isConnected()
            || Laby.references().serverController().getCurrentServerData() == null
            || !Laby.labyAPI().minecraft().isIngame()
            || Laby.labyAPI().minecraft().getClientPlayer() == null
            || Laby.labyAPI().minecraft().clientWorld() == null) {
            return false;
        }

        return Objects.requireNonNull(Laby.references().serverController().getCurrentServerData()).address().getHost().toLowerCase().contains("griefergames");
    }

    // ---------------------------------------------------------------- Functions

    public static void registerFunction(Function function) {
        INSTANCE.functionList.add(function);
    }

    public static List<Function> getFunctions() {
        return INSTANCE.functionList;
    }

    public static Function getFunction(String identifier) {
        for (Function function : INSTANCE.functionList) {
            if (function.getIdentifier().equals(identifier))
                return function;
        }
        return null;
    }

    public static ActiveFunction getActiveFunction(String identifier) {
        return getFunction(identifier) instanceof ActiveFunction activeFunction ? activeFunction : null;
    }

    /** All active functions that are currently enabled. */
    public static List<ActiveFunction> getActiveFunctions() {
        List<ActiveFunction> output = new ArrayList<>();
        for (Function function : INSTANCE.functionList) {
            if (function instanceof ActiveFunction activeFunction && activeFunction.isEnabled())
                output.add(activeFunction);
        }
        return output;
    }

    public static void toggleActiveFunction(String identifier, FunctionState state) {
        toggleActiveFunction(identifier, state, null);
    }

    public static void toggleActiveFunction(String identifier, FunctionState state, String[] params) {
        ActiveFunction function = getActiveFunction(identifier);
        if (function == null)
            return;

        switch (state) {
            case START -> function.start(params);
            case STOP -> function.stop();
            case TOGGLE -> function.toggle(params);
        }
    }

    // ---------------------------------------------------------------- Input / GUI state

    public static boolean isGUIOpen() {
        return !Laby.labyAPI().minecraft().isMouseLocked();
    }

    public static boolean isChatGuiOpen() {
        return isGUIOpen() || !isChatGuiClosed();
    }

    public static boolean isChatGuiClosed() {
        for (IngameOverlayActivity activity : Laby.labyAPI().ingameOverlay().getActivities()) {
            if (activity.isAcceptingInput())
                return false;
        }
        return true;
    }

    public static void openChat() {
        Laby.labyAPI().minecraft().openChat("");
    }

    public static boolean allKeysPressedAndGuiClosed(Key[] keys) {
        return !isGUIOpen() && allKeysPressed(keys);
    }

    public static boolean allKeysPressed(Key[] keys) {
        if (keys.length == 0)
            return false;

        for (Key key : keys) {
            if (!Laby.labyAPI().minecraft().isKeyPressed(key))
                return false;
        }
        return true;
    }
}
