package org.example.core.listener;

import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import org.example.core.ExampleAddon;

public class ExampleGameTickListener {

  private final ExampleAddon addon;

  public ExampleGameTickListener(ExampleAddon addon) {
    this.addon = addon;
  }

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    if (event.phase() != Phase.PRE) {
      return;
    }

    this.addon.logger().info(this.addon.configuration().enabled().get() ? "enabled" : "disabled");
  }

  @Subscribe
  public void onClick(MouseButtonEvent event) {
    this.addon.logger().info("Maus: " + event.button().isLeft());
  }
}
