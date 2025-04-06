package tmb.randy.tmbgriefergames.v1_12_2.functions.functions;

import net.labymod.api.Laby;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumHand;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.widgets.PotionTimerWidget;
import tmb.randy.tmbgriefergames.core.widgets.PotionTimerWidget.Timer;
import tmb.randy.tmbgriefergames.v1_12_2.functions.Helper;

public class PotionTimer extends Function {

    private ItemStack lastRenderedTooltipItemStack;

    public PotionTimer() {
        super(Functions.POTIONTIMER);
    }

    @Override
    public void itemStackTooltipEvent(ItemStackTooltipEvent event) {
        this.lastRenderedTooltipItemStack = event.itemStack();
    }

    @Override
    public void mouseButtonEvent(MouseButtonEvent event) {
        if (!Laby.labyAPI().minecraft().isIngame())
            return;

        if (event.action() == Action.CLICK && event.button().isLeft()) {
            Container cont = Helper.getPlayer().openContainer;
            if(cont != null) {
                if(cont instanceof ContainerChest chest) {
                    IInventory inv = chest.getLowerChestInventory();
                    if(inv.getName().equalsIgnoreCase("§6Möchtest du den Trank benutzen?")) {
                        if(lastRenderedTooltipItemStack != null) {
                            if(lastRenderedTooltipItemStack.getAsItem().getIdentifier().getPath().equals("dye") && lastRenderedTooltipItemStack.getDisplayName().toString().contains("Bestätigen")) {
                                switch (Helper.getPlayer().getHeldItem(EnumHand.MAIN_HAND).getDisplayName()) {
                                    case "§6Flugtrank" -> PotionTimerWidget.startTimer(Timer.FLY);
                                    case "§6Abbautrank" -> PotionTimerWidget.startTimer(Timer.BREAK);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}