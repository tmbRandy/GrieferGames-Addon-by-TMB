package tmb.randy.tmbgriefergames.api.functions;

import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import tmb.randy.tmbgriefergames.api.events.CbChangedEvent;
import tmb.randy.tmbgriefergames.api.events.ToggleFunctionEvent;

public abstract class ActiveFunction extends Function {

    private boolean enabled = false;
    private final ResourceLocation iconLocation;

    public ActiveFunction(String identifier) {
        this(identifier, null);
    }

    public ActiveFunction(String identifier, ResourceLocation iconLocation) {
        super(identifier);
        this.iconLocation = iconLocation;
    }

    public boolean start() {
        return start(new String[0]);
    }

    public boolean start(String[] arguments) {
        if (enabled) return false;
        enabled = true;
        return true;
    }

    public boolean stop() {
        if (!enabled) return false;
        enabled = false;
        return true;
    }

    public void toggle() {
        toggle(null);
    }

    public void toggle(String[] arguments) {
        if (isEnabled()) stop();
        else start(arguments);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Icon getIcon() {
        return iconLocation != null ? Icon.texture(iconLocation) : null;
    }

    public boolean hasIcon() {
        return getIcon() != null;
    }

    @Override
    public void serverDisconnectEvent(ServerDisconnectEvent event) {
        stop();
    }

    @Override
    public void toggleFunctionEvent(ToggleFunctionEvent event) {
        if (event.function().equals(identifier)) {
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
