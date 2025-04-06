package tmb.randy.tmbgriefergames.v1_8_9.functions;

import static tmb.randy.tmbgriefergames.core.functions.ItemSaver.findHotbarSlotforItem;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
import tmb.randy.tmbgriefergames.core.helper.I19n;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

public class HABK extends Function {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public HABK() {
        super(Functions.HABK);
    }

    @Override
    public void mouseButtonEvent(MouseButtonEvent event) {
        if (Addon.getSharedInstance().configuration().getSwordsSubConfig().getHABKenabled().get() && event.button().isRight() && event.action() == Action.RELEASE) {
            int selectedSlot = Helper.getPlayer().inventory.currentItem;

            ItemStack heldItemStack = Helper.getPlayer().inventory.getStackInSlot(selectedSlot);
            ItemStack firstItemStack = Helper.getPlayer().inventory.getStackInSlot(0);

            String enchantments = getEnchantments(heldItemStack);

            if(enchantments != null && enchantments.equals(ItemSaver.getVersionizedNbtStringFor(ProtectionItems.BIRTH_BOW))) {
                Addon.getSharedInstance().getConnection().changeSlot(findHotbarSlotforItem());
                Addon.getSharedInstance().displayNotification("§4§l" + I19n.translate("itemSaver.item_saver_message_birth_bow"));
                event.setCancelled(true);
            } else if (heldItemStack != null && heldItemStack.getItem() instanceof ItemBow && firstItemStack.getItem() instanceof ItemSword) {

                scheduler.schedule(() -> {
                    Helper.getPlayer().inventory.currentItem = 0;
                    scheduler.schedule(() -> Helper.getPlayer().inventory.currentItem = selectedSlot,
                        Addon.getSharedInstance().configuration().getSwordsSubConfig().getHABKcooldown().get(), TimeUnit.MILLISECONDS);
                }, 5, TimeUnit.MILLISECONDS);
            }
        }
    }

    private String getEnchantments(ItemStack itemStack) {
        if(itemStack != null && itemStack.getTagCompound() != null) {
            if(itemStack.getTagCompound().hasKey("ench")) {
                return itemStack.getTagCompound().getTag("ench").toString();
            }
        }
        return null;
    }
}