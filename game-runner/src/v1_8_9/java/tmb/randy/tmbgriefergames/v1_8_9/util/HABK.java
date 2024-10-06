package tmb.randy.tmbgriefergames.v1_8_9.util;

import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import tmb.randy.tmbgriefergames.core.Addon;

import static tmb.randy.tmbgriefergames.core.util.ItemSaver.NBTTagStringBirthBow;
import static tmb.randy.tmbgriefergames.core.util.ItemSaver.findHotbarSlotforItem;

public class HABK {

    public void onMouseButtonEvent(MouseButtonEvent event) {
        if(Addon.getSharedInstance().configuration().getSwordsSubConfig().getHABKenabled().get() && event.button().isRight() && event.action() == Action.RELEASE) {
            int selectedSlot = Minecraft.getMinecraft().thePlayer.inventory.currentItem;

            ItemStack heldItemStack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(selectedSlot);
            ItemStack firstItemStack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(0);

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
                            Minecraft.getMinecraft().thePlayer.inventory.currentItem = 0;
                            new java.util.Timer().schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        Minecraft.getMinecraft().thePlayer.inventory.currentItem = selectedSlot;
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