package tmb.randy.tmbgriefergames.core.functions;

import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.core.events.ToggleFunctionEvent;
import tmb.randy.tmbgriefergames.core.helper.CBtracker;
import tmb.randy.tmbgriefergames.core.helper.I19n;
import tmb.randy.tmbgriefergames.core.widgets.ActiveFunctionsWidget;

abstract public class ActiveFunction extends Function {
    private boolean enabled;

    public ActiveFunction(Functions type) {
        super(type);
    }

    public boolean start() {
        return start(new String[0]);
    }

    public boolean start(String[] arguments) {
        if(enabled) return false;
        if(!CBtracker.isCommandAbleCB()) return false;

        enabled = true;

        if(!ActiveFunctionsWidget.sharedInstance.isEnabled() || !hasIcon())
            Addon.getSharedInstance().displayNotification(I19n.translate("functions.enable", type.getLocalizedName()));

        return true;
    }

    public boolean stop() {
        if(enabled) {
            enabled = false;

            if(!ActiveFunctionsWidget.sharedInstance.isEnabled() || !hasIcon())
                Addon.getSharedInstance().displayNotification(I19n.translate("functions.disable", type.getLocalizedName()));
            return true;
        }

        return false;
    }

    public void toggle() {
        toggle(null);
    }

    public void toggle(String[] arguments) {
        if(enabled)
            stop();
        else
            start(arguments);
    }

    public Icon getIcon() {
        ResourceLocation resourceLocation = ResourceLocation.create(Addon.getNamespace(), "textures/widgets/status/" + type.name() + ".png");
        return resourceLocation.exists() ? Icon.texture(resourceLocation) : null;
    }

    public boolean hasIcon() {
        return getIcon() != null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void serverDisconnectEvent(ServerDisconnectEvent event) {
        stop();
    }

    @Override
    public void toggleFunctionEvent(ToggleFunctionEvent event) {
        if(event.function() == type) {
            switch (event.state()) {
                case START -> start();
                case STOP -> stop();
                case TOGGLE -> toggle();
            }
        }
    }

    @Override
    public void cbChangedEvent(CbChangedEvent event) {
        stop();
    }
}