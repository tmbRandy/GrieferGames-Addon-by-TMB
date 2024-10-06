package tmb.randy.tmbgriefergames.core.util;

import net.labymod.api.Laby;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.component.data.DataComponentKey;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;

public class ItemSaver {

    private static final String NBTTagStringBonze = "[{lvl:21s,id:16s},{lvl:3s,id:34s},{lvl:2s,id:20s},{lvl:5s,id:61s},{lvl:21s,id:21s}]";
    private static final String NBTTagStringBirthSword = "[{lvl:21s,id:16s},{lvl:2s,id:20s},{lvl:5s,id:61s},{lvl:21s,id:21s}]";
    private static final String NBTTagStringSoS = "[{lvl:-6s,id:18s},{lvl:-6s,id:21s},{lvl:-6s,id:7s}]";
    public static final String NBTTagStringBirthBow = "[{lvl:22s,id:48s},{lvl:4s,id:49s},{lvl:1s,id:51s},{lvl:4s,id:19s},{lvl:22s,id:21s}]";

    @Subscribe
    public void mouseButtonEvent(MouseButtonEvent event) {
        if(!Addon.isGG())
            return;

        if(Addon.getSharedInstance().configuration().getItemProtection().get()) {
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

