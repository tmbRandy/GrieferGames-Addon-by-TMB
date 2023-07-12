package tmb.randy.griefergames.core;

import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.models.addon.annotation.AddonMain;
import tmb.randy.griefergames.core.config.Configuration;
import tmb.randy.griefergames.core.util.ItemSaver;
import tmb.randy.griefergames.core.util.PlotSwitch;
import tmb.randy.griefergames.core.util.chat.ChatCleaner;
import tmb.randy.griefergames.core.util.chat.CooldownNotifier;
import tmb.randy.griefergames.core.util.chat.EmptyLinesRemover;
import tmb.randy.griefergames.core.util.chat.NewsBlocker;
import tmb.randy.griefergames.core.util.chat.PaymentValidator;
import tmb.randy.griefergames.core.util.TooltipExtension;
import tmb.randy.griefergames.core.util.chat.TypeCorrection;
import java.util.Objects;

@AddonMain
public class Addon extends LabyAddon<Configuration> {

  private static Addon SharedInstance;
  private final String ADDON_PREFIX = "§6[§5§l§oT§b§l§oM§5§l§oB§6] ";

  @Override
  protected void enable() {
    this.registerSettingCategory();

    this.registerListener(new TypeCorrection());
    this.registerListener(new TooltipExtension());
    this.registerListener(new NewsBlocker());
    this.registerListener(new PaymentValidator());
    this.registerListener(new ChatCleaner());
    this.registerListener(new EmptyLinesRemover());
    this.registerListener(new ItemSaver());
    this.registerListener(new CooldownNotifier());
    this.registerListener(new PlotSwitch());

    SharedInstance = this;

    this.logger().info("Enabled the Addon");
  }

    @Override
    protected Class<Configuration> configurationClass() {
    return Configuration.class;
  }

    public void displayNotification(String msg) {
        Laby.labyAPI().minecraft().chatExecutor().displayClientMessage(ADDON_PREFIX + msg);
    }

    public static Addon getSharedInstance() {
    return SharedInstance;
  }

  public static boolean isGG() {
    if(!Laby.labyAPI().serverController().isConnected()) {
      return false;
    }

    return Objects.requireNonNull(Laby.labyAPI().serverController().getCurrentServerData()).getName().equals("GrieferGames");
  }
}
