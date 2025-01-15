package tmb.randy.tmbgriefergames.core.util;

import java.util.Objects;
import net.labymod.api.Laby;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.component.data.DataComponentKey;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.util.I18n;
import tmb.randy.tmbgriefergames.core.Addon;

public class ItemSaver {

    public enum ProtectionItems {
        BONZE_SWORD, BIRTH_SWORD, SOS, BIRTH_BOW
    }

    public static String getVersionizedNbtStringFor(ProtectionItems item) {
        return switch (Laby.labyAPI().minecraft().getVersion()) {
            case "1.8.9" -> switch (item) {
                case BONZE_SWORD -> "[0:{lvl:21s,id:16s},1:{lvl:3s,id:34s},2:{lvl:2s,id:20s},3:{lvl:5s,id:61s},4:{lvl:21s,id:21s}]";
                case BIRTH_SWORD -> "[0:{lvl:21s,id:16s},1:{lvl:2s,id:20s},2:{lvl:5s,id:61s},3:{lvl:21s,id:21s}]";
                case SOS -> "[0:{lvl:-6s,id:18s},1:{lvl:-6s,id:21s},2:{lvl:-6s,id:7s}]";
                case BIRTH_BOW -> "[0:{lvl:22s,id:48s},1:{lvl:4s,id:49s},2:{lvl:1s,id:51s},3:{lvl:4s,id:19s},4:{lvl:22s,id:21s}]";
            };
            case "1.12.2" -> switch (item) {
                case BONZE_SWORD -> "[{lvl:21s,id:16s},{lvl:3s,id:34s},{lvl:2s,id:20s},{lvl:5s,id:61s},{lvl:21s,id:21s}]";
                case BIRTH_SWORD -> "[{lvl:21s,id:16s},{lvl:2s,id:20s},{lvl:5s,id:61s},{lvl:21s,id:21s}]";
                case SOS -> "[{lvl:-6s,id:18s},{lvl:-6s,id:21s},{lvl:-6s,id:7s}]";
                case BIRTH_BOW -> "[{lvl:22s,id:48s},{lvl:4s,id:49s},{lvl:1s,id:51s},{lvl:4s,id:19s},{lvl:22s,id:21s}]";
            };
            default -> "";
        };
    }


    @Subscribe
    public void mouseButtonEvent(MouseButtonEvent event) {
        if(!Addon.isGG())
            return;

        if(Addon.getSharedInstance().configuration().getItemProtection().get()) {
            if(Laby.labyAPI().minecraft().getClientPlayer() != null) {
                if(Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).getMainHandItemStack() != null) {
                    ItemStack stack = Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).getMainHandItemStack();
                    if(stack.hasDataComponentContainer()) {
                        if(stack.getDataComponentContainer().has(DataComponentKey.simple("ench"))) {
                            String enchantments = stack.getDataComponentContainer().get(DataComponentKey.simple("ench")).toString();

                            if(event.action() == Action.CLICK) {
                                if((enchantments.equals(getVersionizedNbtStringFor(ProtectionItems.BONZE_SWORD)) || enchantments.equals(getVersionizedNbtStringFor(ProtectionItems.BIRTH_SWORD))) && event.button().isLeft()) {
                                    Addon.getSharedInstance().displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_sword"));
                                    Addon.getSharedInstance().getBridge().changeSlot(findHotbarSlotforItem());
                                    event.setCancelled(true);
                                } else if(enchantments.equals(getVersionizedNbtStringFor(ProtectionItems.SOS)) && event.button().isRight()) {
                                    Addon.getSharedInstance().displayNotification("§4§l" + I18n.translate("tmbgriefergames.itemSaver.item_saver_message_sos"));
                                    Addon.getSharedInstance().getBridge().changeSlot(findHotbarSlotforBlock());
                                    event.setCancelled(true);
                                } else if(enchantments.equals(getVersionizedNbtStringFor(ProtectionItems.BIRTH_BOW)) && event.button().isRight()) {
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
            ItemStack stack = Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).inventory().itemStackAt(i);

            if (stack.isAir()) {
                return i;
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).inventory().itemStackAt(i);

            if(stack.isItem()) {
                return i;
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).inventory().itemStackAt(i);

            if(stack.isBlock() && !stack.hasDataComponentContainer()) {
                return i;
            }
        }

        return Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).inventory().getSelectedIndex() == 8 ? 0 : Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).inventory().getSelectedIndex() + 1;
    }

    public static int findHotbarSlotforItem() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).inventory().itemStackAt(i);

            if (stack.isAir()) {
                return i;
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).inventory().itemStackAt(i);

            if(stack.isBlock()) {
                return i;
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).inventory().itemStackAt(i);

            if(stack.isItem() && !stack.hasDataComponentContainer()) {
                return i;
            }
        }

        return Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).inventory().getSelectedIndex() == 8 ? 0 : Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).inventory().getSelectedIndex() + 1;
    }



}

