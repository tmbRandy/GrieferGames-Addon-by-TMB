package tmb.randy.tmbgriefergames.v1_12_2.util;

import static tmb.randy.tmbgriefergames.core.util.ItemSaver.findHotbarSlotforItem;

import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.util.ItemSaver;
import tmb.randy.tmbgriefergames.core.util.ItemSaver.ProtectionItems;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HABK {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void onMouseButtonEvent(MouseButtonEvent event) {
        if (isHABKEnabled() && event.button().isRight() && event.action() == Action.RELEASE) {
            int selectedSlot = Minecraft.getMinecraft().player.inventory.currentItem;

            ItemStack heldItemStack = Minecraft.getMinecraft().player.inventory.getStackInSlot(selectedSlot);
            ItemStack firstItemStack = Minecraft.getMinecraft().player.inventory.getStackInSlot(0);

            String enchantments = getEnchantments(heldItemStack);

            if(enchantments != null && enchantments.equals(ItemSaver.getVersionizedNbtStringFor(ProtectionItems.BIRTH_BOW))) {
                Addon.getSharedInstance().getBridge().changeSlot(findHotbarSlotforItem());
                displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_birth_bow"));
                event.setCancelled(true);
            } else if (heldItemStack != null && heldItemStack.getItem() instanceof ItemBow && firstItemStack.getItem() instanceof ItemSword) {

                scheduler.schedule(() -> {
                    Minecraft.getMinecraft().player.inventory.currentItem = 0;
                    scheduler.schedule(() -> Minecraft.getMinecraft().player.inventory.currentItem = selectedSlot,
                        Addon.getSharedInstance().configuration().getSwordsSubConfig().getHABKcooldown().get(), TimeUnit.MILLISECONDS);
                }, 5, TimeUnit.MILLISECONDS);
            }
        }
    }

    private boolean isHABKEnabled() {
        return Addon.getSharedInstance().configuration().getSwordsSubConfig().getHABKenabled().get();
    }

    private String getEnchantments(ItemStack itemStack) {
        if(itemStack != null && itemStack.getTagCompound() != null) {
            if(itemStack.getTagCompound().hasKey("ench")) {
                return itemStack.getTagCompound().getTag("ench").toString();
            }
        }
        return null;
    }

    private void displayNotification(String message) {
        Addon.getSharedInstance().displayNotification(message);
    }
}
