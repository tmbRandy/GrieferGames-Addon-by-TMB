package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.EnumHand;
import tmb.randy.tmbgriefergames.core.functions.PotionTimerMaster;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

public class PotionTimer extends PotionTimerMaster {

    @Override
    protected String getOpenMenuName() {
        Container cont = Helper.getPlayer().openContainer;
        return cont instanceof ContainerChest chest ? chest.getLowerChestInventory().getName() : null;
    }

    @Override
    protected String getHeldItemName() {
        return Helper.getPlayer().getHeldItem(EnumHand.MAIN_HAND).getDisplayName();
    }
}