package tmb.randy.tmbgriefergames.core.widgets;

import java.util.List;
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
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.HopperState;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.helper.HopperTracker;
import javax.inject.Singleton;

@Singleton
public class ActiveFunctionsWidget extends SimpleHudWidget<HudWidgetConfig> {
    private static final int ICON_SIZE = 32;
    private static final int SPACE = 2;
    private static final Icon HOPPER_CONNECT_ICON =  Icon.texture(ResourceLocation.create(Addon.getNamespace(), "textures/widgets/status/hopper_connect.png"));
    private static final Icon HOPPER_MULTI_CONNECT_ICON =  Icon.texture(ResourceLocation.create(Addon.getNamespace(), "textures/widgets/status/hopper_multiconnect.png"));
    public static ActiveFunctionsWidget sharedInstance;

    public ActiveFunctionsWidget(HudWidgetCategory category) {
        super("activefunctions", HudWidgetConfig.class);
        sharedInstance = this;
        bindDropzones(NamedHudWidgetDropzones.ACTION_BAR);
        this.bindCategory(category);
        this.setIcon(Icon.texture(ResourceLocation.create(Addon.getNamespace(), "textures/widgets/status.png")));
    }

    public void render(Stack stack, MutableMouse mouse, float partialTicks, boolean isEditorContext, HudSize size) {
        if(stack != null) {
            List<ActiveFunction> active = isEditorContext ? getDemoFunctions() : Addon.getSharedInstance().getActiveFunctions();

            int xPos = 0;
            int yPos = 0;

            for (ActiveFunction function : active) {
                if(function.hasIcon()) {
                    function.getIcon().render(stack, xPos, yPos, ICON_SIZE);
                }

                if(anchor().isRight() || anchor.isLeft()) {
                    yPos += (ICON_SIZE + SPACE);
                } else {
                    xPos += (ICON_SIZE + SPACE);
                }
            }

            if(HopperTracker.getCurrentHopperState() == HopperState.CONNECT) {
                HOPPER_CONNECT_ICON.render(stack, xPos, yPos, ICON_SIZE);

                if(anchor().isRight() || anchor.isLeft()) {
                    yPos += (ICON_SIZE + SPACE);
                } else {
                    xPos += (ICON_SIZE + SPACE);
                }
            } else if(HopperTracker.getCurrentHopperState() == HopperState.MULTICONNECT) {
                HOPPER_MULTI_CONNECT_ICON.render(stack, xPos, yPos, ICON_SIZE);

                if(anchor().isRight() || anchor.isLeft()) {
                    yPos += (ICON_SIZE + SPACE);
                } else {
                    xPos += (ICON_SIZE + SPACE);
                }
            }

            Laby.references().renderPipeline().resourceRenderer().render(stack);
            size.setHeight((float)Math.max(ICON_SIZE, yPos));
            size.setWidth((float)Math.max(ICON_SIZE, xPos));
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
        return Addon.isGG();
    }

    private List<ActiveFunction> getDemoFunctions() {
        return List.of(
            Addon.getSharedInstance().getActiveFunction(Functions.PLAYERTRACER),
            Addon.getSharedInstance().getActiveFunction(Functions.COMP),
            Addon.getSharedInstance().getActiveFunction(Functions.CRAFTV3));
    }
}