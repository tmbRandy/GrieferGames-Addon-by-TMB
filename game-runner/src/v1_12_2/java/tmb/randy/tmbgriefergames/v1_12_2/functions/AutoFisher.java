package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.api.events.FishEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.config.AutoFisherSubConfig;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.v1_12_2.Helper;

/** Catches fish automatically, drops rubbish and optionally stores the catch in the ender chest. */
public class AutoFisher extends ActiveFunction {
    private static final long RECAST_DELAY_MS = 1000;
    private static final long ROD_SWITCH_DELAY_MS = 200;
    private static final long ITEM_REMOVER_RETRY_MS = 4000;

    private boolean fished = false;
    private boolean waitForItemRemover = false;

    public AutoFisher() {
        super(Functions.AUTOFISHER.name());
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if (!config().getEnabled().get() || fished || !Helper.isItem(Helper.getHeldItem(), Const.Id.FISHING_ROD))
            return;

        EntityFishHook hook = Helper.getFishHook();
        if (hook != null && hook.motionY < Helper.FISH_BITE_MOTION_Y && hook.isInWater() && hook.motionX == 0 && hook.motionZ == 0)
            catchFish();
    }

    @Override
    public void fishEvent(FishEvent event) {
        if (Helper.getFishHook() != null)
            catchFish();
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        String message = event.chatMessage().getPlainText();

        // Pull the hook in before the item remover runs, otherwise it would be removed as well
        if (message.endsWith(Const.Chat.AUTOFISHER_ITEMS_WARNING)) {
            pullInBeforeItemRemover();
            Helper.schedule(this::pullInBeforeItemRemover, ITEM_REMOVER_RETRY_MS);
        } else if (message.endsWith(Const.Chat.AUTOFISHER_ITEMS_REMOVED) && waitForItemRemover) {
            waitForItemRemover = false;
            Helper.sendUseItemPacket();
        }
    }

    private void pullInBeforeItemRemover() {
        if (Helper.getPlayer() != null && Helper.getFishHook() != null) {
            Helper.sendUseItemPacket();
            waitForItemRemover = true;
        }
    }

    private void catchFish() {
        Helper.sendUseItemPacket();

        if (!fished) {
            fished = true;
            Helper.schedule(() -> fished = false, RECAST_DELAY_MS);
        }

        if (Helper.isEmpty(Helper.getHeldItem()))
            return;

        Helper.schedule(() -> {
            if (Helper.hasDurabilityLeft(Helper.getHeldItem()) && !waitForItemRemover) {
                Helper.sendUseItemPacket();
                dropRubbish();
            } else {
                selectNextRod();
            }
        }, RECAST_DELAY_MS);
    }

    private void selectNextRod() {
        for (int i = 0; i < Helper.HOTBAR_SIZE; i++) {
            ItemStack stack = Helper.getInventoryStack(i);
            if (Helper.isItem(stack, Const.Id.FISHING_ROD) && Helper.hasDurabilityLeft(stack)) {
                Helper.setHotbarSlot(i);
                Helper.schedule(() -> {
                    Helper.sendUseItemPacket();
                    fished = false;
                }, ROD_SWITCH_DELAY_MS);
                return;
            }
        }
        fished = false;
    }

    private void dropRubbish() {
        if (!Helper.isContainerOpen()) {
            Helper.dropItems(this::isRubbish, false);
        } else if (Const.Item.ENDERTRUHE.equals(Helper.getChestDisplayName()) && config().getAllowEC().get()) {
            Helper.dropItems(this::isRubbish, false);
            storeCatchInEnderChest();
        }
    }

    private void storeCatchInEnderChest() {
        int freeSlots = 0;
        for (int i = 0; i < Helper.getChestSize(); i++) {
            if (Helper.getContainerStack(i) == null)
                freeSlots++;
        }

        for (int slot = Helper.getChestSize(); slot < Helper.getContainerSize() && freeSlots > 0; slot++) {
            ItemStack stack = Helper.getContainerStack(slot);
            if (stack != null && !Helper.isItem(stack, Const.Id.FISHING_ROD) && !isRubbish(stack)) {
                Helper.shiftClick(slot);
                freeSlots--;
            }
        }
    }

    private boolean isRubbish(ItemStack stack) {
        if (Helper.isEmpty(stack) || stack.hasDisplayName())
            return false;

        AutoFisherSubConfig config = config();
        String id = Helper.getRegistryName(stack);
        return switch (id) {
            case Const.Id.FISHING_ROD -> config.getAutoDropRods().get();
            case Const.Id.BOW -> config.getAutoDropBows().get();
            case Const.Id.ENCHANTED_BOOK -> config.getAutoDropBooks().get();
            case Const.Id.SADDLE -> config.getAutoDropSaddle().get();
            case Const.Id.LILY_PAD -> config.getAutoDropLilypads().get();
            case Const.Id.FISH, Const.Id.COOKED_FISH -> config.getAutoDropFish().get() && stack.getMetadata() <= 1;
            default -> false;
        };
    }

    private static AutoFisherSubConfig config() {
        return Addon.settings().getAutoFisherSubConfig();
    }
}
