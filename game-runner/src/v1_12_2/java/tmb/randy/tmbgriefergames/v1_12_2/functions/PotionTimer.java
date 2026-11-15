package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.labymod.api.Laby;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.menu.PotionMenu;
import tmb.randy.tmbgriefergames.core.widgets.PotionTimerWidget;
import tmb.randy.tmbgriefergames.core.widgets.PotionTimerWidget.Timer;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

/** Starts the potion timer widget when the use of a fly or break potion is confirmed. */
public class PotionTimer extends Function {
    private ItemStack lastHoveredStack;

    public PotionTimer() {
        super(Functions.POTIONTIMER.name());
    }

    @Override
    public void itemStackTooltipEvent(ItemStackTooltipEvent event) {
        lastHoveredStack = event.itemStack();
    }

    @Override
    public void mouseButtonEvent(MouseButtonEvent event) {
        if (!Laby.labyAPI().minecraft().isIngame() || event.action() != Action.CLICK || !event.button().isLeft())
            return;

        if (!Helper.isChestOpen(PotionMenu.TITLE) || !isConfirmButton(lastHoveredStack) || Helper.isEmpty(Helper.getHeldItem()))
            return;

        switch (Helper.getHeldItem().getDisplayName()) {
            case Const.Item.FLUGTRANK -> PotionTimerWidget.startTimer(Timer.FLY);
            case Const.Item.ABBAUTRANK -> PotionTimerWidget.startTimer(Timer.BREAK);
        }
    }

    private static boolean isConfirmButton(ItemStack stack) {
        return stack != null && stack.getAsItem().getIdentifier().getPath().equals("dye")
            && stack.getDisplayName().toString().contains(PotionMenu.CONFIRM_KEYWORD);
    }
}
