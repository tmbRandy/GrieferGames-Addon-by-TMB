package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import tmb.randy.tmbgriefergames.core.functions.PotionTimerMaster;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

public class PotionTimer extends PotionTimerMaster {

    @Override
    protected String getOpenMenuName() {
        Container cont = Helper.getPlayer().openContainer;
        return cont instanceof ContainerChest chest ? chest.getLowerChestInventory().getName() : null;
    }

    @Override
    protected String getHeldItemName() {
        return Helper.getPlayer().getHeldItem().getDisplayName();
    }
}