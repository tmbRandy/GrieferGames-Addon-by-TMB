package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.core.functions.ItemSaver;
import tmb.randy.tmbgriefergames.core.functions.ItemSaver.ProtectionItems;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

/** Half automatic bow sword: after shooting the bow it switches to the sword in slot 1 and back. */
public class HABK extends Function {
    private static final long SWITCH_DELAY_MS = 5;

    public HABK() {
        super(Functions.HABK.name());
    }

    @Override
    public void mouseButtonEvent(MouseButtonEvent event) {
        if (!Addon.settings().getSwordsSubConfig().getHABKenabled().get() || !event.button().isRight() || event.action() != Action.RELEASE)
            return;

        int selectedSlot = Helper.getHotbarSlot();
        ItemStack heldItem = Helper.getInventoryStack(selectedSlot);

        if (ItemSaver.getVersionizedNbtStringFor(ProtectionItems.BIRTH_BOW).equals(Helper.getEnchantments(heldItem))) {
            Helper.setHotbarSlot(ItemSaver.findHotbarSlotforItem());
            Addon.displayNotification("§4§l" + Addon.translate("itemSaver.item_saver_message_birth_bow"));
            event.setCancelled(true);
        } else if (!Helper.isEmpty(heldItem) && heldItem.getItem() instanceof ItemBow && !Helper.isEmpty(Helper.getInventoryStack(0))
            && Helper.getInventoryStack(0).getItem() instanceof ItemSword) {
            Helper.schedule(() -> {
                Helper.setHotbarSlot(0);
                Helper.schedule(() -> Helper.setHotbarSlot(selectedSlot), Addon.settings().getSwordsSubConfig().getHABKcooldown().get());
            }, SWITCH_DELAY_MS);
        }
    }
}
