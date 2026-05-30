package tmb.randy.tmbgriefergames.v1_8_9.functions;

import java.util.LinkedList;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.AutoCompMaster;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;
import tmb.randy.tmbgriefergames.v1_8_9.click.Click;
import tmb.randy.tmbgriefergames.v1_8_9.click.ClickManager;

public class AutoComp extends AutoCompMaster {

    private final LinkedList<Click> toSend = new LinkedList<>();

    @Override
    protected MenuContext getMenuContext() {
        if(!(Helper.getPlayer().openContainer instanceof ContainerChest chest))
            return null;

        ItemStack pageIndicator = chest.getSlot(49).getStack();
        int itemId = Helper.isStackEmpty(pageIndicator) ? -1 : Item.getIdFromItem(pageIndicator.getItem());
        String pageName = Helper.isStackEmpty(pageIndicator) ? "" : pageIndicator.getDisplayName();
        return new MenuContext(chest.getLowerChestInventory().getName(), itemId, pageName);
    }

    @Override
    protected void click(int slot) {
        this.toSend.addLast(new Click(Helper.getPlayer().openContainer.windowId, slot, 0, 1));
        ClickManager.getSharedInstance().queueClicks(QueueType.MEDIUM, this.toSend);
        this.toSend.clear();
    }
}
