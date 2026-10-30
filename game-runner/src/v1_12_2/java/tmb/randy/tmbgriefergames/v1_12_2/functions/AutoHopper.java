package tmb.randy.tmbgriefergames.v1_12_2.functions;

import java.util.Timer;
import java.util.TimerTask;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.config.HopperSubConfig;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.HopperFinalAction;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.menu.HopperMenu;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

/** Applies the saved hopper settings as soon as the hopper menu opens. */
public class AutoHopper extends Function {
    private static final long SNEAK_RELEASE_DELAY_MS = 10;

    private boolean reachedPlotBorder = false;
    private boolean sneakToggled = false;

    public AutoHopper() {
        super(Functions.AUTOHOPPER.name());
    }

    @Override
    public void mouseButtonEvent(MouseButtonEvent event) {
        if (!event.button().isRight() || event.action() != Action.CLICK || !config().getAutoSneak().get() || !Helper.isBlock(Helper.getBlockPosLookingAt(), Const.Id.HOPPER))
            return;

        // Sneaking while clicking a hopper opens the GrieferGames settings instead of the hopper inventory
        Keyboard.enableRepeatEvents(true);
        KeyBinding.setKeyBindState(Helper.mc().gameSettings.keyBindSneak.getKeyCode(), true);
        if (!Helper.getPlayer().onGround)
            KeyBinding.setKeyBindState(Helper.mc().gameSettings.keyBindJump.getKeyCode(), true);

        Helper.clickBlockLookingAt();
        sneakToggled = true;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                releaseSneak();
            }
        }, SNEAK_RELEASE_DELAY_MS);
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if (event.chatMessage().getPlainText().endsWith(Const.Chat.PLOT_BORDER_REACHED))
            reachedPlotBorder = true;
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if (!config().getEnabled().get())
            return;

        String title = Helper.getChestTitle();
        if (title == null) {
            reachedPlotBorder = false;
            return;
        }

        if (title.equals(HopperMenu.TITLE))
            releaseSneak();

        if (!Helper.isClickQueueEmpty())
            return;

        if (title.equals(HopperMenu.TITLE)) {
            if (!applyFilter() && !applyRadius() && !applyStackSize())
                applyFinalAction();
        } else if (title.equals(HopperMenu.MultiConnections.TITLE) && config().getFinalAction().get() == HopperFinalAction.MULTICONNECTION) {
            Helper.click(HopperMenu.MultiConnections.ADD_CONNECTION);
        }
    }

    private boolean applyFilter() {
        ItemStack filter = Helper.getContainerStack(HopperMenu.FILTER);
        ItemStack source = Helper.getContainerStack(HopperMenu.FILTER_SOURCE);
        if (!config().getFilterItem().get() || filter == null || source == null || Helper.isSameItem(filter, source))
            return false;

        Helper.shiftClick(HopperMenu.FILTER_SOURCE);
        return true;
    }

    private boolean applyRadius() {
        int radius = config().getRadius().get();
        if (radius < 0 || reachedPlotBorder)
            return false;

        if (radius == 0) {
            if (!Helper.isItem(Helper.getContainerStack(HopperMenu.RADIUS_DECREASE), Const.Id.SKULL))
                return false;

            Helper.shiftClick(HopperMenu.RADIUS_DECREASE);
            return true;
        }

        int currentRadius = Helper.getCount(Helper.getContainerStack(HopperMenu.RADIUS));
        if (radius == currentRadius)
            return false;

        Helper.shiftClick(radius > currentRadius ? HopperMenu.RADIUS_INCREASE : HopperMenu.RADIUS_DECREASE);
        return true;
    }

    private boolean applyStackSize() {
        int wantedSize = switch (config().getStackSize().get()) {
            case SINGLEITEM -> 1;
            case TWELVE -> 12;
            case FULLSTACK -> 64;
            case NONE -> -1;
        };

        ItemStack stackSize = Helper.getContainerStack(HopperMenu.STACK_SIZE);
        if (wantedSize < 0 || stackSize == null || Helper.getCount(stackSize) == wantedSize)
            return false;

        Helper.shiftClick(HopperMenu.STACK_SIZE);
        return true;
    }

    private void applyFinalAction() {
        if (Helper.getContainerStack(HopperMenu.STACK_SIZE) == null)
            return;

        switch (config().getFinalAction().get()) {
            case CLOSE -> Helper.getPlayer().closeScreen();
            case CONNECT -> Helper.click(HopperMenu.CONNECT);
            case MULTICONNECTION -> Helper.click(HopperMenu.MULTI_CONNECT);
            case NONE -> {}
        }
    }

    private void releaseSneak() {
        if (!sneakToggled)
            return;

        KeyBinding.setKeyBindState(Helper.mc().gameSettings.keyBindSneak.getKeyCode(), false);
        KeyBinding.setKeyBindState(Helper.mc().gameSettings.keyBindJump.getKeyCode(), false);
        Keyboard.enableRepeatEvents(false);
        sneakToggled = false;
    }

    private static HopperSubConfig config() {
        return Addon.settings().getHopperSubConfig();
    }
}
