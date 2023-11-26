package tmb.randy.tmbgriefergames.v1_12_2.util;

import net.labymod.api.Laby;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import tmb.randy.tmbgriefergames.core.Addon;

public class ItemSaver {

    private static final String NBTTagStringBonze = "[{lvl:21s,id:16s},{lvl:3s,id:34s},{lvl:2s,id:20s},{lvl:5s,id:61s},{lvl:21s,id:21s}]";
    private static final String NBTTagStringBirthSword = "[{lvl:21s,id:16s},{lvl:2s,id:20s},{lvl:5s,id:61s},{lvl:21s,id:21s}]";
    private static final String NBTTagStringSoS = "[{lvl:-6s,id:18s},{lvl:-6s,id:21s},{lvl:-6s,id:7s}]";
    public static final String NBTTagStringBirthBow = "[{lvl:22s,id:48s},{lvl:4s,id:49s},{lvl:1s,id:51s},{lvl:4s,id:19s},{lvl:22s,id:21s}]";


    public void mouseInput(MouseButtonEvent event) {
        if(!Addon.getSharedInstance().configuration().getItemProtection().get())
            return;

        if(Laby.labyAPI().minecraft().getClientPlayer() != null) {
            if(Laby.labyAPI().minecraft().getClientPlayer().getMainHandItemStack() != null) {
                ItemStack stack = Laby.labyAPI().minecraft().getClientPlayer().getMainHandItemStack();

                if(stack.hasNBTTag()) {
                    if(stack.getNBTTag().contains("ench")) {
                        String enchantments = stack.getNBTTag().get("ench").toString();

                        //Unfortunately there is a bug within LabyMod which doesn't cancel the event in MouseButtonEvent after performing event.setCancelled(true) for some players. So switching the slot to save the item is a workaround. As LabyMod support couldn't reproduce and fix the bug this is the only option to save the item.
                        if(event.action() == Action.CLICK) {
                            if((enchantments.equals(NBTTagStringBonze) || enchantments.equals(NBTTagStringBirthSword)) && event.button().isLeft()) {
                                Addon.getSharedInstance().displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_sword"));
                                changeSlot(findHotbarSlotforItem());
                                event.setCancelled(true);
                            } else if(enchantments.equals(NBTTagStringSoS) && event.button().isRight()) {
                                Addon.getSharedInstance().displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_sos"));
                                changeSlot(findHotbarSlotforBlock());
                                event.setCancelled(true);
                            } else if(enchantments.equals(NBTTagStringBirthBow) && event.button().isRight()) {
                                Addon.getSharedInstance().displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_birth_bow"));
                                changeSlot(findHotbarSlotforItem());
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

            if(stack.isBlock() && !stack.hasNBTTag()) {
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

            if(stack.isItem() && !stack.hasNBTTag()) {
                return i;
            }
        }

        return Laby.labyAPI().minecraft().getClientPlayer().inventory().getSelectedIndex() == 8 ? 0 : Laby.labyAPI().minecraft().getClientPlayer().inventory().getSelectedIndex() + 1;
    }

    public void changeSlot(int slot) {
        Minecraft.getMinecraft().player.inventory.currentItem = slot;
    }
}
