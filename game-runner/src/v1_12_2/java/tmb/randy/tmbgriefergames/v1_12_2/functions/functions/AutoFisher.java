package tmb.randy.tmbgriefergames.v1_12_2.functions.functions;

import java.util.Timer;
import java.util.TimerTask;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.config.AutoFisherSubConfig;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.v1_12_2.functions.Helper;
import tmb.randy.tmbgriefergames.v1_12_2.functions.click.Click;
import tmb.randy.tmbgriefergames.v1_12_2.functions.click.ClickManager;

public class AutoFisher extends ActiveFunction {

    private boolean fished = false;
    private boolean waitForFishingAfterRemover = false;

    public AutoFisher() {
        super(Functions.AUTOFISHER);
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if(!Addon.getSharedInstance().configuration().getAutoFisherSubConfig().getEnabled().get()) return;
        if(!Helper.getPlayer().getUniqueID().equals(Helper.getPlayer().getUniqueID())) return;
        if(!(Helper.getPlayer().getHeldItemMainhand().getItem() instanceof ItemFishingRod)) return;

        if(Helper.getPlayer().fishEntity == null) return;

        if(fished) return;
        EntityFishHook fishingHook = Helper.getPlayer().fishEntity;
        double x = fishingHook.motionX;
        double z = fishingHook.motionZ;
        double y = fishingHook.motionY;
        if(y < -0.05 && Helper.getPlayer().fishEntity.isInWater() && x == 0 && z == 0) {
            NetHandlerPlayClient nethandler = Minecraft.getMinecraft().getConnection();

            if(nethandler != null)
                nethandler.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            else
                ClickManager.getSharedInstance().rightClick();

            if(!fished) {
                fished = true;
                startTimer();
            }
            if(Helper.getPlayer().getHeldItemMainhand().isEmpty()) return;
            new Thread(() -> {
                try {
                    Thread.sleep(1000);

                    if(Helper.getPlayer().getHeldItemMainhand().getItemDamage() < Helper.getPlayer().getHeldItemMainhand().getMaxDamage() && !waitForFishingAfterRemover) {
                        if(nethandler != null) {
                            nethandler.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                            dropRubbishFishedItems();
                        } else
                            ClickManager.getSharedInstance().rightClick();
                    } else
                        selectSimilarItem();

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if(event.chatMessage().getPlainText().endsWith("Warnung! Die auf dem Boden liegenden Items werden in 20 Sekunden entfernt!")) {
            if(Helper.getPlayer().fishEntity != null) {
                NetHandlerPlayClient nethandler = Minecraft.getMinecraft().getConnection();
                if(nethandler != null) {
                    nethandler.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                } else {
                    ClickManager.getSharedInstance().rightClick();
                }
                waitForFishingAfterRemover = true;
            }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if(Helper.getPlayer().fishEntity != null) {
                        NetHandlerPlayClient nethandler = Minecraft.getMinecraft().getConnection();
                        if(nethandler != null) {
                            nethandler.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                        } else
                            ClickManager.getSharedInstance().rightClick();

                        waitForFishingAfterRemover = true;
                    }
                }
            }, 4000);

        } else if(event.chatMessage().getPlainText().endsWith("auf dem Boden liegende Items entfernt!")) {
            if(waitForFishingAfterRemover) {
                waitForFishingAfterRemover = false;
                NetHandlerPlayClient nethandler = Minecraft.getMinecraft().getConnection();
                if(nethandler != null) {
                    nethandler.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                } else {
                    ClickManager.getSharedInstance().rightClick();
                }
            }
        }
    }

    private void dropRubbishFishedItems() {
        if(Helper.getPlayer().openContainer instanceof ContainerPlayer container) {
            int size = container.inventorySlots.size();
            dropRubbish(container, 9, size);
        } else if(Helper.getPlayer().openContainer instanceof ContainerChest container) {
            IInventory lowerChestInventory = container.getLowerChestInventory();

            if (lowerChestInventory.getDisplayName().getUnformattedText().equals("Endertruhe") &&
                Addon.getSharedInstance().configuration().getAutoFisherSubConfig().getAllowEC().get()) {

                int ecSize = lowerChestInventory.getSizeInventory();
                int containerSize = container.inventorySlots.size();

                dropRubbish(container, ecSize, containerSize);

                int maxShifts = getEmptySlotsInInventory(lowerChestInventory);
                int executedShifts = 0;

                for (int i = ecSize; i < containerSize; i++) {
                    ItemStack stack = container.getSlot(i).getStack();
                    if (!stack.isEmpty() && stack.getItem() != Items.FISHING_ROD &&
                        !shouldDropItem(stack) && executedShifts < maxShifts) {
                        ClickManager.getSharedInstance().addClick(
                            QueueType.MEDIUM, new Click(container.windowId, i, 0, ClickType.QUICK_MOVE));
                        executedShifts++;
                    }
                }
            }
        }
    }

    private boolean shouldDropItem(ItemStack stack) {
        if (stack.isEmpty() || stack.hasDisplayName()) {
            return false;
        }

        Item item = stack.getItem();
        int meta = stack.getMetadata();
        AutoFisherSubConfig subConfig = Addon.getSharedInstance().configuration().getAutoFisherSubConfig();

        return (item == Items.FISHING_ROD && subConfig.getAutoDropRods().get()) ||
            (item == Items.BOW && subConfig.getAutoDropBows().get()) ||
            (item == Items.ENCHANTED_BOOK && subConfig.getAutoDropBooks().get()) ||
            (item == Items.SADDLE && subConfig.getAutoDropSaddle().get()) ||
            (Item.getIdFromItem(item) == 111 && subConfig.getAutoDropLilypads().get()) ||
            (item == Items.FISH && subConfig.getAutoDropFish().get() && (meta == 0 || meta == 1));
    }

    private void dropRubbish(Container container, int minSlot, int maxSlot) {
        for(int i = minSlot; i < maxSlot; i++) {
            if(container.getSlot(i).getHasStack()) {
                ItemStack stack = container.getSlot(i).getStack();
                if (shouldDropItem(stack)) {
                    ClickManager.getSharedInstance().dropClick(i);
                }
            }
        }
    }

    private int getEmptySlotsInInventory(IInventory inventory) {
        int count = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if(inventory.getStackInSlot(i).isEmpty()) count++;
        }

        return count;
    }

    private void startTimer() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                fished = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void selectSimilarItem() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            boolean rodFound = false;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);

                if (!stack.isEmpty() && stack.getItem().equals(Items.FISHING_ROD)) {
                    int itemDamage = stack.getItemDamage();
                    int maxDamage = stack.getMaxDamage();
                    if(itemDamage < maxDamage) {
                        Helper.getPlayer().inventory.currentItem = i;
                        rodFound = true;

                        new Thread(() -> {
                            try {
                                Thread.sleep(200);
                                NetHandlerPlayClient nethandler = Minecraft.getMinecraft().getConnection();
                                if(nethandler != null)
                                    nethandler.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                                else
                                    ClickManager.getSharedInstance().rightClick();

                                fished = false;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();

                        break;
                    }
                }
            }

            if (!rodFound) {
                fished = false;
            }
        }
    }
}