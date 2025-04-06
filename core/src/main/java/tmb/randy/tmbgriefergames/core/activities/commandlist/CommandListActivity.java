package tmb.randy.tmbgriefergames.core.activities.commandlist;

import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.activity.types.SimpleActivity;
import net.labymod.api.client.gui.screen.widget.widgets.layout.ScrollWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.commands.DescribedCommand;

@AutoActivity
@Link("commandlistactivity.lss")
public class CommandListActivity extends SimpleActivity {

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
    VerticalListWidget<CommandEntry> list = new VerticalListWidget<>();
    list.addId("list");

    ScrollWidget scrollWidget = new ScrollWidget(list);
    scrollWidget.addId("scroll");

    for (DescribedCommand command : Addon.getSharedInstance().getCommands()) {
        StringBuilder title = new StringBuilder("/" + command.getPrefix());

        for (String alias : command.getAliases()) {
          title.append(" /").append(alias);
        }

        list.addChild(new CommandEntry(title.toString(), command.getDescription()));
    }

    document.addChild(scrollWidget);
  }
}
