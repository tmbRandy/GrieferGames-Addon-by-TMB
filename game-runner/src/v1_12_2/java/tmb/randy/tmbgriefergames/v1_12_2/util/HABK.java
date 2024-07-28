package tmb.randy.tmbgriefergames.v1_12_2.util;

import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import tmb.randy.tmbgriefergames.core.Addon;

import static tmb.randy.tmbgriefergames.v1_12_2.util.ItemSaver.NBTTagStringBirthBow;
import static tmb.randy.tmbgriefergames.v1_12_2.util.ItemSaver.findHotbarSlotforItem;

public class HABK {

    public void onMouseButtonEvent(MouseButtonEvent event) {
        if(Addon.getSharedInstance().configuration().getSwordsSubConfig().getHABKenabled().get() && event.button().isRight() && event.action() == Action.RELEASE) {
            int selectedSlot = Minecraft.getMinecraft().player.inventory.currentItem;

            ItemStack heldItemStack = Minecraft.getMinecraft().player.inventory.getStackInSlot(selectedSlot);
            ItemStack firstItemStack = Minecraft.getMinecraft().player.inventory.getStackInSlot(0);

            String enchantments = null;

            if(heldItemStack != null && heldItemStack.getTagCompound() != null) {
                if(heldItemStack.getTagCompound().hasKey("ench")) {
                    enchantments = heldItemStack.getTagCompound().getTag("ench").toString();
                }
            }

            if(enchantments != null && enchantments.equals(NBTTagStringBirthBow)) {
                Addon.getSharedInstance().getBridge().changeSlot(findHotbarSlotforItem());
                Addon.getSharedInstance().displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_birth_bow"));
                event.setCancelled(true);
            } else if (heldItemStack != null && heldItemStack.getItem() instanceof ItemBow && firstItemStack.getItem() instanceof ItemSword) {

                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Minecraft.getMinecraft().player.inventory.currentItem = 0;
                            new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        Minecraft.getMinecraft().player.inventory.currentItem = selectedSlot;
                                    }
                                },
                                Addon.getSharedInstance().configuration().getSwordsSubConfig().getHABKcooldown().get()
                            );
                        }
                    },
                    5
                );
            }
        }
    }
}

