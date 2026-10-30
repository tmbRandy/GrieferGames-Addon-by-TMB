package tmb.randy.tmbgriefergames.core.functions;

import java.util.List;
import java.util.function.Predicate;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.entity.player.Inventory;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.component.data.DataComponentKey;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.helper.BaseHelper;

/** Prevents using precious items (bonze/birth sword, SoS, birth bow) by switching away from them. */
public class ItemSaver extends Function {

    public enum ProtectionItems {
        BONZE_SWORD, BIRTH_SWORD, SOS, BIRTH_BOW
    }

    private static final DataComponentKey ENCHANTMENTS = DataComponentKey.simple("ench");

    public ItemSaver() {
        super(Functions.ITEMSAVER.name());
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
        ClientPlayer player = Laby.labyAPI().minecraft().getClientPlayer();
        if (!Addon.settings().getItemProtection().get() || event.action() != Action.CLICK || player == null)
            return;

        ItemStack stack = player.getMainHandItemStack();
        if (stack == null || !stack.hasDataComponentContainer() || !stack.getDataComponentContainer().has(ENCHANTMENTS))
            return;

        String enchantments = stack.getDataComponentContainer().get(ENCHANTMENTS).toString();
        String message = null;
        int slot = -1;

        if (event.button().isLeft() && (isItem(enchantments, ProtectionItems.BONZE_SWORD) || isItem(enchantments, ProtectionItems.BIRTH_SWORD))) {
            message = "itemSaver.item_saver_message_sword";
            slot = findHotbarSlotforItem();
        } else if (event.button().isRight() && isItem(enchantments, ProtectionItems.SOS)) {
            message = "itemSaver.item_saver_message_sos";
            slot = findHotbarSlotforBlock();
        } else if (event.button().isRight() && isItem(enchantments, ProtectionItems.BIRTH_BOW)) {
            message = "itemSaver.item_saver_message_birth_bow";
            slot = findHotbarSlotforItem();
        }

        if (message != null) {
            Addon.displayNotification("§4§l" + Addon.translate(message));
            Addon.getConnection().changeSlot(slot);
            event.setCancelled(true);
        }
    }

    private static boolean isItem(String enchantments, ProtectionItems item) {
        return enchantments.equals(getVersionizedNbtStringFor(item));
    }

    /** Hotbar slot to switch to when a block should be placed instead: empty, then item, then plain block. */
    public static int findHotbarSlotforBlock() {
        return findHotbarSlot(List.of(ItemStack::isAir, ItemStack::isItem, stack -> stack.isBlock() && !stack.hasDataComponentContainer()));
    }

    /** Hotbar slot to switch to when an item should be used instead: empty, then block, then plain item. */
    public static int findHotbarSlotforItem() {
        return findHotbarSlot(List.of(ItemStack::isAir, ItemStack::isBlock, stack -> stack.isItem() && !stack.hasDataComponentContainer()));
    }

    /** First hotbar slot matching the first possible filter, otherwise simply the next slot. */
    private static int findHotbarSlot(List<Predicate<ItemStack>> filtersByPriority) {
        Inventory inventory = Laby.labyAPI().minecraft().getClientPlayer().inventory();

        for (Predicate<ItemStack> filter : filtersByPriority) {
            for (int i = 0; i < BaseHelper.HOTBAR_SIZE; i++) {
                if (filter.test(inventory.itemStackAt(i)))
                    return i;
            }
        }
        return (inventory.getSelectedIndex() + 1) % BaseHelper.HOTBAR_SIZE;
    }
}
