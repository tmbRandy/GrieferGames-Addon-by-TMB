package tmb.randy.tmbgriefergames.core.functions;

import java.util.Objects;
import net.labymod.api.Laby;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.component.data.DataComponentKey;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;

public class ItemSaver extends Function {

    private static final long NOTIFICATION_COOLDOWN_MS = 750L;
    private static String lastNotificationKey = "";
    private static long lastNotificationTime = 0L;

    public ItemSaver() {
        super(Functions.ITEMSAVER.name());
    }

    public enum ProtectionItems {
        BONZE_SWORD, BIRTH_SWORD, SOS, BIRTH_BOW
    }

    public enum ProtectedAction {
        ATTACK, USE_ITEM, RELEASE_USE_ITEM
    }

    public static String getVersionizedNbtStringFor(ProtectionItems item) {
        return switch (Laby.labyAPI().minecraft().getVersion()) {
            case "1.8.9" -> switch (item) {
                case BONZE_SWORD -> "[0:{lvl:21s,id:16s},1:{lvl:3s,id:34s},2:{lvl:2s,id:20s},3:{lvl:5s,id:61s},4:{lvl:21s,id:21s}]";
                case BIRTH_SWORD -> "[0:{lvl:21s,id:16s},1:{lvl:2s,id:20s},2:{lvl:5s,id:61s},3:{lvl:21s,id:21s}]";
                case SOS -> "[0:{lvl:-6s,id:18s},1:{lvl:-6s,id:18s},2:{lvl:-6s,id:7s}]";
                case BIRTH_BOW -> "[0:{lvl:22s,id:48s},1:{lvl:4s,id:49s},2:{lvl:1s,id:51s},3:{lvl:4s,id:19s},4:{lvl:22s,id:21s}]";
            };
            case "1.12.2" -> switch (item) {
                case BONZE_SWORD -> "[{lvl:21s,id:16s},{lvl:3s,id:34s},{lvl:2s,id:20s},{lvl:5s,id:61s},{lvl:21s,id:21s}]";
                case BIRTH_SWORD -> "[{lvl:21s,id:16s},{lvl:2s,id:20s},{lvl:5s,id:61s},{lvl:21s,id:21s}]";
                case SOS -> "[{lvl:-6s,id:21s},{lvl:-6s,id:18s},{lvl:-6s,id:7s}]";
                case BIRTH_BOW -> "[{lvl:22s,id:48s},{lvl:4s,id:49s},{lvl:1s,id:51s},{lvl:4s,id:19s},{lvl:22s,id:21s}]";
            };
            default -> "";
        };
    }


    @Override
    public void mouseButtonEvent(MouseButtonEvent event) {
        if (event.action() == Action.CLICK && event.button().isLeft() && shouldBlockCurrentItemAction(ProtectedAction.ATTACK)) {
            event.setCancelled(true);
        } else if (event.action() == Action.CLICK && event.button().isRight() && shouldBlockCurrentItemAction(ProtectedAction.USE_ITEM)) {
            event.setCancelled(true);
        } else if (event.action() == Action.RELEASE && event.button().isRight() && shouldBlockCurrentItemAction(ProtectedAction.RELEASE_USE_ITEM)) {
            event.setCancelled(true);
        }
    }

    public static boolean shouldBlockCurrentItemAction(ProtectedAction action) {
        ProtectedItem protectedItem = getProtectedItemForAction(action);

        if (protectedItem == null)
            return false;

        displayProtectionNotification(protectedItem);
        return true;
    }

    private static ProtectedItem getProtectedItemForAction(ProtectedAction action) {
        if (!Addon.settings().getItemProtection().get())
            return null;

        String enchantments = getCurrentMainHandEnchantments();

        if (enchantments == null)
            return null;

        return switch (action) {
            case ATTACK -> {
                if (enchantments.equals(getVersionizedNbtStringFor(ProtectionItems.BONZE_SWORD)) || enchantments.equals(getVersionizedNbtStringFor(ProtectionItems.BIRTH_SWORD)))
                    yield ProtectedItem.SWORD;

                yield null;
            }
            case USE_ITEM -> {
                if (enchantments.equals(getVersionizedNbtStringFor(ProtectionItems.SOS)))
                    yield ProtectedItem.SOS;

                if (enchantments.equals(getVersionizedNbtStringFor(ProtectionItems.BIRTH_BOW)))
                    yield ProtectedItem.BIRTH_BOW;

                yield null;
            }
            case RELEASE_USE_ITEM -> enchantments.equals(getVersionizedNbtStringFor(ProtectionItems.BIRTH_BOW)) ? ProtectedItem.BIRTH_BOW : null;
        };
    }

    private static String getCurrentMainHandEnchantments() {
        if (Laby.labyAPI().minecraft().getClientPlayer() == null)
            return null;

        ItemStack stack = Objects.requireNonNull(Laby.labyAPI().minecraft().getClientPlayer()).getMainHandItemStack();

        if (stack == null || !stack.hasDataComponentContainer() || !stack.getDataComponentContainer().has(DataComponentKey.simple("ench")))
            return null;

        return stack.getDataComponentContainer().get(DataComponentKey.simple("ench")).toString();
    }

    private static void displayProtectionNotification(ProtectedItem protectedItem) {
        long now = System.currentTimeMillis();

        if (protectedItem.messageKey.equals(lastNotificationKey) && now - lastNotificationTime < NOTIFICATION_COOLDOWN_MS)
            return;

        lastNotificationKey = protectedItem.messageKey;
        lastNotificationTime = now;
        Addon.displayNotification("§4§l" + Addon.translate(protectedItem.messageKey));
    }

    private enum ProtectedItem {
        SWORD("itemSaver.item_saver_message_sword"),
        SOS("itemSaver.item_saver_message_sos"),
        BIRTH_BOW("itemSaver.item_saver_message_birth_bow");

        private final String messageKey;

        ProtectedItem(String messageKey) {
            this.messageKey = messageKey;
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

