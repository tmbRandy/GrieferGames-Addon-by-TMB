package tmbrandy.griefergames.core;

import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;
import tmbrandy.griefergames.core.commands.ExamplePingCommand;
import tmbrandy.griefergames.core.util.TypeCorrection;

@AddonMain
public class Addon extends LabyAddon<Configuration> {

  private static Addon SharedInstance;

  @Override
  protected void enable() {
    this.registerSettingCategory();

    this.registerListener(new TypeCorrection());
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
}
