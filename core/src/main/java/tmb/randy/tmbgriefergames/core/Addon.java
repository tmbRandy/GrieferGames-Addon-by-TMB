package tmb.randy.tmbgriefergames.core;

import java.util.ArrayList;
import java.util.Arrays;
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
import tmb.randy.tmbgriefergames.core.activities.plotwheel.PlotWheelActivity;
import tmb.randy.tmbgriefergames.core.activities.plotwheel.PlotWheelPlot;
import tmb.randy.tmbgriefergames.core.commands.AutocraftV2Command;
import tmb.randy.tmbgriefergames.core.commands.AutocraftV3Command;
import tmb.randy.tmbgriefergames.core.commands.DKsCommand;
import tmb.randy.tmbgriefergames.core.commands.DescribedCommand;
import tmb.randy.tmbgriefergames.core.commands.EjectCommand;
import tmb.randy.tmbgriefergames.core.commands.PayAllCommand;
import tmb.randy.tmbgriefergames.core.commands.PlayerTracerCommand;
import tmb.randy.tmbgriefergames.core.config.Configuration;
import tmb.randy.tmbgriefergames.core.enums.CBs;
import tmb.randy.tmbgriefergames.core.enums.FunctionState;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.functions.AccountUnity;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.functions.ItemSaver;
import tmb.randy.tmbgriefergames.core.functions.PlayerTracer;
import tmb.randy.tmbgriefergames.core.functions.PlotSwitch;
import tmb.randy.tmbgriefergames.core.functions.TooltipExtension;
import tmb.randy.tmbgriefergames.core.functions.chat.ChatCleaner;
import tmb.randy.tmbgriefergames.core.functions.chat.CooldownNotifier;
import tmb.randy.tmbgriefergames.core.functions.chat.MsgTabs;
import tmb.randy.tmbgriefergames.core.functions.chat.PaymentValidator;
import tmb.randy.tmbgriefergames.core.functions.chat.TypeCorrection;
import tmb.randy.tmbgriefergames.core.generated.DefaultReferenceStorage;
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

  private static Addon INSTANCE;
  private IConnect connection;

    private final List<Function> functionList = new ArrayList<>(Arrays.asList(
      new PlayerTracer(),
      new PlotSwitch(),
      new ChatCleaner(),
      new CooldownNotifier(),
      new MsgTabs(),
      new PaymentValidator(),
      new TypeCorrection(),
      new ItemSaver(),
      new AccountUnity(),
      new TooltipExtension()
      ));

    private final Set<DescribedCommand> commands = new HashSet<>();

    public static PlotWheelPlot queuedPlot = null;

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
    registerListener(this);

      registerCommand(new DKsCommand());
        registerCommand(new PayAllCommand());
        registerCommand(new PlayerTracerCommand());
        registerCommand(new AutocraftV2Command());
        registerCommand(new AutocraftV3Command());
        registerCommand(new EjectCommand());

      HudWidgetCategory category = new HudWidgetCategory(getNamespace());
      labyAPI().hudWidgetRegistry().categoryRegistry().register(category);

        labyAPI().hudWidgetRegistry().register(new GameInfoWidget(category));
      labyAPI().hudWidgetRegistry().register(new PotionTimerWidget(category));
      labyAPI().hudWidgetRegistry().register(new ItemClearWidget(category));
      labyAPI().hudWidgetRegistry().register(new NearbyWidget(category));
        labyAPI().hudWidgetRegistry().register(new AdventureWidget(category));
        labyAPI().hudWidgetRegistry().register(new ActiveFunctionsWidget(category));

    this.logger().info("Enabled the Addon");
  }

    @Override
    protected Class<Configuration> configurationClass() {
    return Configuration.class;
  }

  public static Configuration settings() {
        return INSTANCE.configuration();
  }

    public static void displayNotification(String msg) {
        String ADDON_PREFIX = "§6[§5§l§oT§b§l§oM§5§l§oB§6] ";
        Laby.labyAPI().minecraft().chatExecutor().displayClientMessage(ADDON_PREFIX + msg);
    }

    public static Logging log() {
        return INSTANCE.logger();
    }

    public static Set<DescribedCommand> getCommands() {
        return INSTANCE.commands;
    }

  public static boolean isGG() {
    if(!Laby.labyAPI().serverController().isConnected() ||
        Laby.references().serverController().getCurrentServerData() == null ||
        !Laby.labyAPI().minecraft().isIngame() ||
        Laby.labyAPI().minecraft().getClientPlayer() == null ||
        Laby.labyAPI().minecraft().clientWorld() == null) {
      return false;
     }

    return Objects.requireNonNull(Laby.references().serverController().getCurrentServerData()).address().getHost().toLowerCase().contains("griefergames");
  }

    public static boolean isChatGuiOpen() {
      if(!Laby.labyAPI().minecraft().isMouseLocked())
          return true;

        for (IngameOverlayActivity activity : Laby.labyAPI().ingameOverlay().getActivities()) {
            if(activity.isAcceptingInput())
                return true;
        }

        return false;
    }

    @Subscribe
    public void keyInput(KeyEvent event) {
        if(event.state() == State.PRESS && event.key() == settings().getPlotSwitchSubConfig().getPlotWheelHotkey().get() && !isChatGuiOpen() && CBtracker.isCommandAbleCB() && isGG())
            Laby.labyAPI().minecraft().minecraftWindow().displayScreen(new PlotWheelActivity());
    }

    @Subscribe
    public void cbChanged(CbChangedEvent event) {
        if(event.CB() == CBs.LOBBY && INSTANCE.settings().getSkipHub().get() != CBs.NONE && INSTANCE.settings().getSkipHub().get() != CBs.LOBBY && isGG())
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if(INSTANCE.settings().getSkipHub().get() == CBs.PORTAL)
                            Commander.queue("/portal");
                        else if(INSTANCE.settings().getSkipHub().get() != CBs.NONE)
                            Commander.queue("/switch " + INSTANCE.settings().getSkipHub().get());
                    }
                }, 800
            );
    }

    @Subscribe
    public void messageReceived(ChatReceiveEvent event) {
        if(isGG() && event.chatMessage().getPlainText().equals("[Switcher] Daten heruntergeladen!")) {
            if(queuedPlot != null) {
                if(CBtracker.isPlotworldCB(CBtracker.getCurrentCB())) {
                    new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                if(isGG() && (queuedPlot.cb() == CBs.NONE || CBtracker.getCurrentCB() == queuedPlot.cb())) {
                                    Laby.references().chatExecutor().chat(queuedPlot.command());
                                    queuedPlot = null;
                                }
                            }
                        }, 500
                    );
                }
            }
        }
    }

    public void registerCommand(DescribedCommand command) {
        commands.add(command);
        super.registerCommand(command);
    }

    public static String getNamespace() {
        return INSTANCE.addonInfo().getNamespace();
    }

    public static Function getFunction(String identifier) {
        for (Function function : INSTANCE.functionList) {
            if (function.getIdentifier().equals(identifier)) {
                return function;
            }
        }

        return null;
    }

    public static ActiveFunction getActiveFunction(String identifier) {
        Function function = getFunction(identifier);
        return function instanceof ActiveFunction ? (ActiveFunction) function : null;
    }

    public static void toggleActiveFunction(String identifier, FunctionState state) {
        toggleActiveFunction(identifier, state, null);
    }

    public static void toggleActiveFunction(String identifier, FunctionState state, String[] params) {
        if(getActiveFunction(identifier) instanceof ActiveFunction af) {
            switch (state) {
                case START -> af.start(params);
                case STOP -> af.stop();
                case TOGGLE -> af.toggle(params);
            }
        }
    }

    public static boolean isGUIOpen() {
        return !Laby.labyAPI().minecraft().isMouseLocked();
    }

    public static boolean allKeysPressed(Key[] keys) {
        if(keys.length == 0)
            return false;

        if(isGUIOpen())
            return false;

        for (Key key : keys) {
            if(!Laby.labyAPI().minecraft().isKeyPressed(key))
                return false;
        }
        return true;
    }

    public static boolean isChatGuiClosed() {
        for (IngameOverlayActivity activity : Laby.labyAPI().ingameOverlay().getActivities()) {
            if(activity.isAcceptingInput()) {
                return false;
            }
        }

        return true;
    }

    public static void openChat() {
        Laby.labyAPI().minecraft().openChat("");
    }

    public static IConnect getConnection() {
        return INSTANCE.connection;
    }

    public static void registerFunction(Function function) {
        INSTANCE.functionList.add(function);
    }

    public static List<Function> getFunctions() {
        return INSTANCE.functionList;
    }

    public static List<ActiveFunction> getActiveFunctions() {

        List<ActiveFunction> output = new ArrayList<>();

        for (Function function : INSTANCE.functionList) {
            if(function instanceof ActiveFunction activeFunction) {
                if(activeFunction.isEnabled()) {
                    output.add(activeFunction);
                }
            }
        }
        return output;
    }

    public static @NotNull String translate(@NotNull String key, Object... args) {
        return I18n.translate(getNamespace() + "." + key, args);
    }
}