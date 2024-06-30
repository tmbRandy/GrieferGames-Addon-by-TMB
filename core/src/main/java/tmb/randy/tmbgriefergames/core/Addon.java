package tmb.randy.tmbgriefergames.core;

import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.gui.screen.activity.types.IngameOverlayActivity;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.models.addon.annotation.AddonMain;
import tmb.randy.tmbgriefergames.core.commands.AutocraftCommand;
import tmb.randy.tmbgriefergames.core.commands.DKsCommand;
import tmb.randy.tmbgriefergames.core.commands.EjectCommand;
import tmb.randy.tmbgriefergames.core.commands.PayAllCommand;
import tmb.randy.tmbgriefergames.core.commands.PlayerTracerCommand;
import tmb.randy.tmbgriefergames.core.config.Configuration;
import tmb.randy.tmbgriefergames.core.generated.DefaultReferenceStorage;
import tmb.randy.tmbgriefergames.core.widgets.FlyTimerWidget;
import tmb.randy.tmbgriefergames.core.widgets.GameInfoWidget;
import tmb.randy.tmbgriefergames.core.widgets.ItemClearWidget;
import tmb.randy.tmbgriefergames.core.widgets.NearbyWidget;
import java.util.Objects;

@AddonMain
public class Addon extends LabyAddon<Configuration> {

    private IBridge bridge;
  private static Addon SharedInstance;
    private GameInfoWidget gameInfoWidget;
  private final String ADDON_PREFIX = "§6[§5§l§oT§b§l§oM§5§l§oB§6] ";

  @Override
  protected void enable() {
    this.registerSettingCategory();
      SharedInstance = this;
      bridge = getReferenceStorage().iBridge();
    this.registerListener(bridge);

      this.registerCommand(new DKsCommand());
      this.registerCommand(new PayAllCommand());
      this.registerCommand(new PlayerTracerCommand());
      this.registerCommand(new AutocraftCommand());
      this.registerCommand(new EjectCommand());

      gameInfoWidget = new GameInfoWidget();

      labyAPI().hudWidgetRegistry().register(new FlyTimerWidget());
      labyAPI().hudWidgetRegistry().register(new ItemClearWidget());
      labyAPI().hudWidgetRegistry().register(gameInfoWidget);
      labyAPI().hudWidgetRegistry().register(new NearbyWidget());

    this.logger().info("Enabled the Addon");
  }

    @Override
    protected Class<Configuration> configurationClass() {
    return Configuration.class;
  }

    public void displayNotification(String msg) {
        Laby.labyAPI().minecraft().chatExecutor().displayClientMessage(ADDON_PREFIX + msg);
    }

    public static Addon getSharedInstance() {return SharedInstance;}

    public DefaultReferenceStorage getReferenceStorage() {return (this.referenceStorageAccessor()); }

  public static boolean isGG() {
    if(!Laby.labyAPI().serverController().isConnected()) {
      return false;
    }

    return Objects.requireNonNull(Laby.labyAPI().serverController().getCurrentServerData()).address().getHost().toLowerCase().contains("griefergames");
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
}
