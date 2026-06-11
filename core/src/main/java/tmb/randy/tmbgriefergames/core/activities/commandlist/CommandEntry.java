package tmb.randy.tmbgriefergames.core.activities.commandlist;

import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;

public class CommandEntry extends VerticalListWidget<ComponentWidget> {

  public CommandEntry(String title, String description) {
    this.addChild(ComponentWidget.text(title).addId("title"));
    this.addChild(ComponentWidget.text(description).addId("description"));
  }
}
