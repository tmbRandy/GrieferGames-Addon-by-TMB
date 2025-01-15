package tmb.randy.tmbgriefergames.core.widgets;

import net.labymod.api.Laby;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.hud.binding.dropzone.NamedHudWidgetDropzones;
import net.labymod.api.client.gui.hud.hudwidget.HudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.SimpleHudWidget;
import net.labymod.api.client.gui.hud.position.HudSize;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.HopperState;
import tmb.randy.tmbgriefergames.core.util.HopperTracker;

public class HopperModeWidget extends SimpleHudWidget<HudWidgetConfig> {
    private static final Icon HOPPER_CONNECT_ICON =  Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/hopper_connect.png"));
    private static final Icon HOPPER_MULTI_CONNECT_ICON =  Icon.texture(ResourceLocation.create(Addon.getSharedInstance().addonInfo().getNamespace(), "textures/widgets/hopper_multi.png"));

    public HopperModeWidget(HudWidgetCategory category) {
        super("hopper_mode", HudWidgetConfig.class);
        bindDropzones(NamedHudWidgetDropzones.ACTION_BAR);
        this.bindCategory(category);
        this.setIcon(HOPPER_CONNECT_ICON);
    }

    public void render(Stack stack, MutableMouse mouse, float partialTicks, boolean isEditorContext, HudSize size) {
            if (stack != null) {
                if(HopperTracker.getCurrentHopperState() == HopperState.CONNECT || isEditorContext) {
                    HOPPER_CONNECT_ICON.render(stack, 0, 0, 32);
                    Laby.references().renderPipeline().resourceRenderer().render(stack);
                    size.setHeight((float)32);
                    size.setWidth((float)32);
                } else if (HopperTracker.getCurrentHopperState() == HopperState.MULTICONNECT) {
                    HOPPER_MULTI_CONNECT_ICON.render(stack, 0, 0, 32);
                    Laby.references().renderPipeline().resourceRenderer().render(stack);
                    size.setHeight((float)32);
                    size.setWidth((float) 32);
                }
            }
    }

    @Subscribe
    public void messageReceived(ChatReceiveEvent event) {
        if(this.isEnabled() && Addon.isGG()) {
            String message = event.chatMessage().getPlainText();
            if (message.equals("[Trichter] Das Multi-Verbinden wurde aktiviert. Klicke mit dem gewünschten Item auf den gewünschten Endpunkt.") ||
                message.equals("[Trichter] Das Verbinden wurde aktiviert. Klicke auf den gewünschten Endpunkt.") ||
                message.equals("[Trichter] Der Trichter wurde erfolgreich verbunden.") ||
                message.equals("[Trichter] Der Verbindungsmodus wurde beendet.") ||
                message.equals("[Trichter] Der Startpunkt ist zu weit entfernt. Bitte starte erneut.")) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public boolean isVisibleInGame() {
        return HopperTracker.getCurrentHopperState() != HopperState.NONE && Addon.isGG();
    }

}