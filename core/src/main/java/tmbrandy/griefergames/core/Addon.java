package tmbrandy.griefergames.core;

import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;
import tmbrandy.griefergames.core.commands.ExamplePingCommand;
import tmbrandy.griefergames.core.util.NewsBlocker;
import tmbrandy.griefergames.core.util.TooltipExtension;
import tmbrandy.griefergames.core.util.TypeCorrection;
import java.util.Objects;

@AddonMain
public class Addon extends LabyAddon<Configuration> {

  private static Addon SharedInstance;

  @Override
  protected void enable() {
    this.registerSettingCategory();

    this.registerListener(new TypeCorrection());
    this.registerListener(new TooltipExtension());
    this.registerListener(new NewsBlocker());
    this.registerCommand(new ExamplePingCommand());

    SharedInstance = this;

    this.logger().info("Enabled the Addon");
  }

  @Override
  protected Class<Configuration> configurationClass() {
    return Configuration.class;
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
