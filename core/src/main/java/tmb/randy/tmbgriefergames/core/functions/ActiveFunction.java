package tmb.randy.tmbgriefergames.core.functions;

import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import tmb.randy.tmbgriefergames.api.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.widgets.ActiveFunctionsWidget;

public abstract class ActiveFunction extends tmb.randy.tmbgriefergames.api.functions.ActiveFunction {

    public ActiveFunction(String identifier) {
        super(identifier);
    }

    @Override
    public boolean start(String[] arguments) {
        if (!CBtracker.isCommandAbleCB()) return false;
        boolean result = super.start(arguments);
        if (result) {
            if (!ActiveFunctionsWidget.sharedInstance.isEnabled() || !hasIcon())
                Addon.displayNotification(Addon.translate("functions.enable", Functions.valueOf(identifier).getLocalizedName()));
        }
        return result;
    }

    @Override
    public boolean stop() {
        boolean result = super.stop();
        if (result) {
            if (!ActiveFunctionsWidget.sharedInstance.isEnabled() || !hasIcon())
                Addon.displayNotification(Addon.translate("functions.disable", Functions.valueOf(identifier).getLocalizedName()));
        }
        return result;
    }

    @Override
    public void cbChangedEvent(CbChangedEvent event) {
        stop();
    }

    @Override
    public Icon getIcon() {
        ResourceLocation resourceLocation = ResourceLocation.create(Addon.getNamespace(), "textures/widgets/status/" + identifier + ".png");
        return resourceLocation.exists() ? Icon.texture(resourceLocation) : null;
    }
}
