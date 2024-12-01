package tmb.randy.tmbgriefergames.core;

import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.screen.activity.types.IngameOverlayActivity;
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
import tmb.randy.tmbgriefergames.core.commands.EjectCommand;
import tmb.randy.tmbgriefergames.core.commands.PayAllCommand;
import tmb.randy.tmbgriefergames.core.commands.PlayerTracerCommand;
import tmb.randy.tmbgriefergames.core.config.Configuration;
import tmb.randy.tmbgriefergames.core.enums.CBs;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.generated.DefaultReferenceStorage;
import tmb.randy.tmbgriefergames.core.util.AccountUnity;
import tmb.randy.tmbgriefergames.core.util.HopperTracker;
import tmb.randy.tmbgriefergames.core.util.ItemClearTimerListener;
import tmb.randy.tmbgriefergames.core.util.ItemSaver;
import tmb.randy.tmbgriefergames.core.util.PlayerTracer;
import tmb.randy.tmbgriefergames.core.util.PlotSwitch;
import tmb.randy.tmbgriefergames.core.util.TooltipExtension;
import tmb.randy.tmbgriefergames.core.util.chat.ChatCleaner;
import tmb.randy.tmbgriefergames.core.util.chat.CooldownNotifier;
import tmb.randy.tmbgriefergames.core.util.chat.EmptyLinesRemover;
import tmb.randy.tmbgriefergames.core.util.chat.MsgTabs;
import tmb.randy.tmbgriefergames.core.util.chat.NewsBlocker;
import tmb.randy.tmbgriefergames.core.util.chat.PaymentValidator;
import tmb.randy.tmbgriefergames.core.util.chat.StreamerMute;
import tmb.randy.tmbgriefergames.core.util.chat.TypeCorrection;
import tmb.randy.tmbgriefergames.core.widgets.AdventureWidget;
import tmb.randy.tmbgriefergames.core.widgets.FlyTimerWidget;
import tmb.randy.tmbgriefergames.core.widgets.GameInfoWidget;
import tmb.randy.tmbgriefergames.core.widgets.HopperModeWidget;
import tmb.randy.tmbgriefergames.core.widgets.ItemClearWidget;
import tmb.randy.tmbgriefergames.core.widgets.NearbyWidget;
import java.util.Objects;

@AddonMain
public class Addon extends LabyAddon<Configuration> {

    private IBridge bridge;
  private static Addon SharedInstance;
  private GameInfoWidget gameInfoWidget;
  private final CBtracker cbtracker = new CBtracker();
  private final PlayerTracer playerTracer = new PlayerTracer();
  private final HopperTracker hopperTracker = new HopperTracker();
  private final PlotSwitch plotSwitch = new PlotSwitch();

  private final ChatCleaner chatCleaner = new ChatCleaner();
  private final CooldownNotifier cooldownNotifier = new CooldownNotifier();
  private final EmptyLinesRemover emptyLinesRemover = new EmptyLinesRemover();
  private final MsgTabs msgTabs = new MsgTabs();
  private final NewsBlocker newsBlocker = new NewsBlocker();
  private final PaymentValidator paymentValidator = new PaymentValidator();
  private final StreamerMute streamerMute = new StreamerMute();
  private final TypeCorrection typeCorrection = new TypeCorrection();
  private final ItemSaver itemSaver = new ItemSaver();
  private final AccountUnity accountUnity = new AccountUnity();
  private final ItemClearTimerListener itemClearTimerListener = new ItemClearTimerListener();
  private final TooltipExtension tooltipExtension = new TooltipExtension();

    private static final int commandCountdownLimit = 80;
    private static int commandCountdown = 0;
    public static PlotWheelPlot queuedPlot = null;

    @Override
  protected void enable() {
    this.registerSettingCategory();
      SharedInstance = this;
      bridge = getReferenceStorage().iBridge();
    this.registerListener(bridge);
    this.registerListener(cbtracker);
    this.registerListener(playerTracer);
    this.registerListener(chatCleaner);
    this.registerListener(cooldownNotifier);
    this.registerListener(emptyLinesRemover);
    this.registerListener(msgTabs);
    this.registerListener(newsBlocker);
    this.registerListener(paymentValidator);
    this.registerListener(streamerMute);
    this.registerListener(typeCorrection);
    this.registerListener(hopperTracker);
    this.registerListener(plotSwitch);
    this.registerListener(itemSaver);
    this.registerListener(accountUnity);
    this.registerListener(itemClearTimerListener);
    this.registerListener(tooltipExtension);
    this.registerListener(this);

      this.registerCommand(new DKsCommand());
      this.registerCommand(new PayAllCommand());
      this.registerCommand(new PlayerTracerCommand());
      this.registerCommand(new AutocraftV2Command());
      this.registerCommand(new AutocraftV3Command());
      this.registerCommand(new EjectCommand());

      HudWidgetCategory category = new HudWidgetCategory("tmbgriefergames");
      labyAPI().hudWidgetRegistry().categoryRegistry().register(category);

      gameInfoWidget = new GameInfoWidget(category);

      labyAPI().hudWidgetRegistry().register(new FlyTimerWidget(category));
      labyAPI().hudWidgetRegistry().register(new ItemClearWidget(category));
      labyAPI().hudWidgetRegistry().register(gameInfoWidget);
      labyAPI().hudWidgetRegistry().register(new NearbyWidget(category));
      labyAPI().hudWidgetRegistry().register(new HopperModeWidget(category));
      labyAPI().hudWidgetRegistry().register(new AdventureWidget(category));

        //PlotWheelActivity activity = new PlotWheelActivity();
        //labyAPI().navigationService().register(new PlotWheelNavigationElement(activity));

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

    public DefaultReferenceStorage getReferenceStorage() {return (this.referenceStorageAccessor()); }

  public static boolean isGG() {
    if(!Laby.labyAPI().serverController().isConnected() || Laby.references().serverController().getCurrentServerData() == null) {
      return false;
     }

    return Objects.requireNonNull(Laby.references().serverController().getCurrentServerData()).address().getHost().toLowerCase().contains("griefergames");
  }

    public IBridge getBridge() {
        return bridge;
    }

    public GameInfoWidget getGameInfoWidget() {
        return gameInfoWidget;
    }

    public static boolean isChatGuiOpen() {
      if(!Laby.labyAPI().minecraft().isMouseLocked())
          return true;

        for (IngameOverlayActivity activity : Laby.labyAPI().ingameOverlay().getActivities()) {
            if(activity.isAcceptingInput()) {
                return true;
            }
        }

        return false;
    }

    public PlayerTracer getPlayerTracer() {return playerTracer;}

    @Subscribe
    public void keyInput(KeyEvent event) {
        if(event.state() == State.PRESS && event.key() == configuration().getPlotSwitchSubConfig().getPlotWheelHotkey().get() && !isChatGuiOpen() && CBtracker.isCommandAbleCB() && isGG()) {
            // Unfortunately the player has to close the PlotWheel manually as Laby doesn't provide Activity::close  :/
            Laby.labyAPI().minecraft().minecraftWindow().displayScreen(new PlotWheelActivity());
        }
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
}
