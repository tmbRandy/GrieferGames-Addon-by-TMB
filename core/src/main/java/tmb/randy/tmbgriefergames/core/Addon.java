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
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.models.addon.annotation.AddonMain;
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
import tmb.randy.tmbgriefergames.core.enums.Functions;
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

  private static Addon SharedInstance;
  private GameInfoWidget gameInfoWidget;
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

    private static final int commandCountdownLimit = 80;
    private static int commandCountdown = 0;
    public static PlotWheelPlot queuedPlot = null;

    @Override
  protected void enable() {
        SharedInstance = this;
        connection = ((DefaultReferenceStorage) this.referenceStorageAccessor()).iConnect();
        connection.loadFunctions();
        registerSettingCategory();

        registerListener(new Listener());
    registerListener(connection);
    registerListener(new CBtracker());
    registerListener(new HopperTracker());
    registerListener(new ItemClearTimerListener());
    registerListener(this);

      registerCommand(new DKsCommand());
        registerCommand(new PayAllCommand());
        registerCommand(new PlayerTracerCommand());
        registerCommand(new AutocraftV2Command());
        registerCommand(new AutocraftV3Command());
        registerCommand(new EjectCommand());

      HudWidgetCategory category = new HudWidgetCategory(getNamespace());
      labyAPI().hudWidgetRegistry().categoryRegistry().register(category);

      gameInfoWidget = new GameInfoWidget(category);

        labyAPI().hudWidgetRegistry().register(gameInfoWidget);
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

    public void displayNotification(String msg) {
        String ADDON_PREFIX = "§6[§5§l§oT§b§l§oM§5§l§oB§6] ";
        Laby.labyAPI().minecraft().chatExecutor().displayClientMessage(ADDON_PREFIX + msg);
    }

    public static Addon getSharedInstance() {return SharedInstance;}

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

    public GameInfoWidget getGameInfoWidget() {
        return gameInfoWidget;
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
        if(event.state() == State.PRESS && event.key() == configuration().getPlotSwitchSubConfig().getPlotWheelHotkey().get() && !isChatGuiOpen() && CBtracker.isCommandAbleCB() && isGG())
            Laby.labyAPI().minecraft().minecraftWindow().displayScreen(new PlotWheelActivity());
    }

    @Subscribe
    public void tick(GameTickEvent event) {
        if(isGG())
            commandCountdown();
    }

    @Subscribe
    public void cbChanged(CbChangedEvent event) {
        if(event.CB() == CBs.LOBBY && Addon.getSharedInstance().configuration().getSkipHub().get() && isGG())
            Addon.sendCommand("/portal");
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

    public static boolean canSendCommand() { return commandCountdown <= 0; }

    public static void sendCommand(String command) {
        if(canSendCommand()) {
            Laby.references().chatExecutor().chat(command);
            commandCountdown = commandCountdownLimit;
        }
    }

    private static void commandCountdown() {
        if (commandCountdown > 0) {
            commandCountdown--;
        }
    }

    public Set<DescribedCommand> getCommands() {
        return commands;
    }

    public void registerCommand(DescribedCommand command) {
        commands.add(command);
        super.registerCommand(command);
    }

    static public String getNamespace() {
        return getSharedInstance().addonInfo().getNamespace();
    }

    public Function getFunction(Functions type) {
        for (Function function : functionList) {
            if (function.getType() == type) {
                return function;
            }
        }

        return null;
    }

    public ActiveFunction getActiveFunction(Functions type) {
        Function function = getFunction(type);
        return function instanceof ActiveFunction ? (ActiveFunction) function : null;
    }

    public static boolean isGUIOpen() {
        return !Laby.labyAPI().minecraft().isMouseLocked();
    }

    public boolean allKeysPressed(Key[] keys) {
        if(keys.length == 0)
            return false;

        if(isGUIOpen())
            return false;

        for (Key key : keys) {
            if(!key.isPressed())
                return false;
        }
        return true;
    }

    public boolean isChatGuiClosed() {
        for (IngameOverlayActivity activity : Laby.labyAPI().ingameOverlay().getActivities()) {
            if(activity.isAcceptingInput()) {
                return false;
            }
        }

        return true;
    }

    public void openChat() {
        Laby.labyAPI().minecraft().openChat("");
    }

    public IConnect getConnection() {
        return connection;
    }

    public void addFunction(Function function) {
        functionList.add(function);
    }

    public List<Function> getFunctions() {
        return functionList;
    }

    public List<ActiveFunction> getActiveFunctions() {
        List<ActiveFunction> output = new ArrayList<>();

        for (Function function : functionList) {
            if(function instanceof ActiveFunction activeFunction) {
                if(activeFunction.isEnabled()) {
                    output.add(activeFunction);
                }
            }
        }
        return output;
    }
}