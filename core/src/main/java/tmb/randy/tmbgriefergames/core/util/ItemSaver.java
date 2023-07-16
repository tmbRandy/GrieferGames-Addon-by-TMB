package tmb.randy.tmbgriefergames.core.util;

import net.labymod.api.Laby;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;

public class ItemSaver {

    private static final String NBTTagStringBonze = "[{id:\"sharpness\",lvl:21s},{id:\"unbreaking\",lvl:3s},{id:\"fire_aspect\",lvl:2s},{id:\"luck_of_the_sea\",lvl:5s},{id:\"looting\",lvl:21s}]";
    private static final String NBTTagStringBirthSword = "[{id:\"sharpness\",lvl:21s},{id:\"fire_aspect\",lvl:2s},{id:\"luck_of_the_sea\",lvl:5s},{id:\"looting\",lvl:21s}]";
    private static final String NBTTagStringSoS = "[{id:\"bane_of_arthropods\",lvl:-6s},{id:\"looting\",lvl:-6s},{id:\"thorns\",lvl:-6s}]";
    private static final String NBTTagStringBirthBow = "[{id:\"power\",lvl:22s},{id:\"punch\",lvl:4s},{id:\"infinity\",lvl:1s},{id:\"knockback\",lvl:4s},{id:\"looting\",lvl:22s}]";

    @Subscribe (Priority.FIRST)
    public void mouseInputEvent(MouseButtonEvent event) {
        if(!Addon.isGG() || !Addon.getSharedInstance().configuration().getItemProtection().get()) {
            return;
        }

        if(Laby.labyAPI().minecraft().getClientPlayer() != null) {
            if(Laby.labyAPI().minecraft().getClientPlayer().getMainHandItemStack() != null) {
                ItemStack stack = Laby.labyAPI().minecraft().getClientPlayer().getMainHandItemStack();

                if(stack.hasNBTTag()) {
                    if(stack.getNBTTag().contains("Enchantments")) {
                        String enchantments = stack.getNBTTag().get("Enchantments").toString();

                        if(event.action() == Action.CLICK) {
                            if((enchantments.equals(NBTTagStringBonze) || enchantments.equals(NBTTagStringBirthSword)) && event.button().isLeft()) {
                                event.setCancelled(true);
                                Addon.getSharedInstance().displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_sword"));
                            } else if(enchantments.equals(NBTTagStringSoS) && event.button().isRight()) {
                                event.setCancelled(true);
                                Addon.getSharedInstance().displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_sos"));
                            } else if(enchantments.equals(NBTTagStringBirthBow) && event.button().isRight()) {
                                event.setCancelled(true);
                                Addon.getSharedInstance().displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_birth_bow"));
                            }
                        }
                    }
                }
            }
        }
    }

}
