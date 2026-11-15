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
        if (result)
            notifyStateChange("functions.enable");
        return result;
    }

    @Override
    public boolean stop() {
        boolean result = super.stop();
        if (result)
            notifyStateChange("functions.disable");
        return result;
    }

    /** Tells the player about the new state in the chat, unless the widget already shows it as an icon. */
    private void notifyStateChange(String translationKey) {
        if (!ActiveFunctionsWidget.sharedInstance.isEnabled() || !hasIcon())
            Addon.displayNotification(Addon.translate(translationKey, Functions.valueOf(identifier).getLocalizedName()));
    }

    @Override
    public void cbChangedEvent(CbChangedEvent event) {
        stop();
    }

    @Override
    public Icon getIcon() {
        ResourceLocation resourceLocation = Addon.widgetTexture("status/" + identifier);
        return resourceLocation.exists() ? Icon.texture(resourceLocation) : null;
    }
}
