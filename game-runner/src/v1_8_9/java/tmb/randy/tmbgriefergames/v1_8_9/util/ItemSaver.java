package tmb.randy.tmbgriefergames.v1_8_9.util;

import net.labymod.api.Laby;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.component.data.DataComponentKey;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import tmb.randy.tmbgriefergames.core.Addon;

public class ItemSaver {

    private static final String NBTTagStringBonze = "[0:{lvl:21s,id:16s},1:{lvl:3s,id:34s},2:{lvl:2s,id:20s},3:{lvl:5s,id:61s},4:{lvl:21s,id:21s}]";
    private static final String NBTTagStringBirthSword = "[0:{lvl:21s,id:16s},1:{lvl:2s,id:20s},2:{lvl:5s,id:61s},3:{lvl:21s,id:21s}]";
    private static final String NBTTagStringSoS = "[0:{lvl:-6s,id:18s},1:{lvl:-6s,id:21s},2:{lvl:-6s,id:7s}]";
    public static final String NBTTagStringBirthBow = "[0:{lvl:22s,id:48s},1:{lvl:4s,id:49s},2:{lvl:1s,id:51s},3:{lvl:4s,id:19s},4:{lvl:22s,id:21s}]";


    public void mouseButtonEvent(MouseButtonEvent event) {

        if(Laby.labyAPI().minecraft().getClientPlayer() != null) {
            if(Laby.labyAPI().minecraft().getClientPlayer().getMainHandItemStack() != null) {
                ItemStack stack = Laby.labyAPI().minecraft().getClientPlayer().getMainHandItemStack();
                if(stack.hasDataComponentContainer()) {
                    if(stack.getDataComponentContainer().has(DataComponentKey.simple("ench"))) {
                        String enchantments = stack.getDataComponentContainer().get(DataComponentKey.simple("ench")).toString();

                        if(event.action() == Action.CLICK) {
                            if((enchantments.equals(NBTTagStringBonze) || enchantments.equals(NBTTagStringBirthSword)) && event.button().isLeft()) {
                                Addon.getSharedInstance().displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_sword"));
                                Addon.getSharedInstance().getBridge().changeSlot(findHotbarSlotforItem());
                                event.setCancelled(true);
                            } else if(enchantments.equals(NBTTagStringSoS) && event.button().isRight()) {
                                Addon.getSharedInstance().displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_sos"));
                                Addon.getSharedInstance().getBridge().changeSlot(findHotbarSlotforBlock());
                                event.setCancelled(true);
                            } else if(enchantments.equals(NBTTagStringBirthBow) && event.button().isRight()) {
                                Addon.getSharedInstance().displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_birth_bow"));
                                Addon.getSharedInstance().getBridge().changeSlot(findHotbarSlotforItem());
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    public static int findHotbarSlotforBlock() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Laby.labyAPI().minecraft().getClientPlayer().inventory().itemStackAt(i);

            if (stack.isAir()) {
                return i;
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = Laby.labyAPI().minecraft().getClientPlayer().inventory().itemStackAt(i);

            if(stack.isItem()) {
                return i;
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = Laby.labyAPI().minecraft().getClientPlayer().inventory().itemStackAt(i);

            if(stack.isBlock() && !stack.hasDataComponentContainer()) {
                return i;
            }
        }

        return Laby.labyAPI().minecraft().getClientPlayer().inventory().getSelectedIndex() == 8 ? 0 : Laby.labyAPI().minecraft().getClientPlayer().inventory().getSelectedIndex() + 1;
    }

    public static int findHotbarSlotforItem() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Laby.labyAPI().minecraft().getClientPlayer().inventory().itemStackAt(i);

            if (stack.isAir()) {
                return i;
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = Laby.labyAPI().minecraft().getClientPlayer().inventory().itemStackAt(i);

            if(stack.isBlock()) {
                return i;
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = Laby.labyAPI().minecraft().getClientPlayer().inventory().itemStackAt(i);

            if(stack.isItem() && !stack.hasDataComponentContainer()) {
                return i;
            }
        }

        return Laby.labyAPI().minecraft().getClientPlayer().inventory().getSelectedIndex() == 8 ? 0 : Laby.labyAPI().minecraft().getClientPlayer().inventory().getSelectedIndex() + 1;
    }

}
