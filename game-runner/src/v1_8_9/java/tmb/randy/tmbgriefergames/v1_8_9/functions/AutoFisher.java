package tmb.randy.tmbgriefergames.v1_8_9.functions;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.config.AutoFisherSubConfig;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;
import tmb.randy.tmbgriefergames.v1_8_9.click.Click;
import tmb.randy.tmbgriefergames.v1_8_9.click.ClickManager;

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
        if(Helper.getPlayer().getHeldItem() == null) return;
        if(!(Helper.getPlayer().getHeldItem().getItem() instanceof ItemFishingRod)) return;

        EntityFishHook currentFishHook = findPlayerFishHook();
        if(currentFishHook == null) return;

        if(fished) return;

        double x = currentFishHook.motionX;
        double z = currentFishHook.motionZ;
        double y = currentFishHook.motionY;

        if(y < -0.001 && currentFishHook.isInWater() && x == 0 && z == 0) {
            NetHandlerPlayClient nethandler = Minecraft.getMinecraft().getNetHandler();
            if(nethandler != null) {
                ItemStack heldItem = Helper.getPlayer().getHeldItem();
                if (heldItem != null)
                    nethandler.addToSendQueue(new C08PacketPlayerBlockPlacement(heldItem));

            } else
                ClickManager.getSharedInstance().rightClick();

            if(!fished) {
                fished = true;
                startTimer();
            }
            if(Helper.getPlayer().getHeldItem() == null) return;
            new Thread(() -> {
                try {
                    Thread.sleep(1000);

                    if(Helper.getPlayer().getHeldItem().getItemDamage() < Helper.getPlayer().getHeldItem().getMaxDamage() && !waitForFishingAfterRemover) {
                        if(nethandler != null) {
                            ItemStack heldItem = Helper.getPlayer().getHeldItem();
                            if (heldItem != null)
                                nethandler.addToSendQueue(new C08PacketPlayerBlockPlacement(heldItem));

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
            EntityPlayerSP player = Helper.getPlayer();
            if(player != null && player.fishEntity != null) {
                NetHandlerPlayClient nethandler = Minecraft.getMinecraft().getNetHandler();
                if(nethandler != null) {
                    ItemStack heldItem = player.getCurrentEquippedItem();
                    nethandler.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, heldItem, 0, 0, 0));
                } else {
                    ClickManager.getSharedInstance().rightClick();
                }
                waitForFishingAfterRemover = true;
            }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    EntityPlayerSP player = Helper.getPlayer();
                    if(player != null && player.fishEntity != null) {
                        NetHandlerPlayClient nethandler = Minecraft.getMinecraft().getNetHandler();
                        if(nethandler != null) {
                            ItemStack heldItem = player.getCurrentEquippedItem();
                            nethandler.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, heldItem, 0, 0, 0));
                        } else
                            ClickManager.getSharedInstance().rightClick();

                        waitForFishingAfterRemover = true;
                    }
                }
            }, 4000);

        } else if(event.chatMessage().getPlainText().endsWith("auf dem Boden liegende Items entfernt!")) {
            if(waitForFishingAfterRemover) {
                waitForFishingAfterRemover = false;
                EntityPlayerSP player = Helper.getPlayer();
                NetHandlerPlayClient nethandler = Minecraft.getMinecraft().getNetHandler();
                if(nethandler != null && player != null) {
                    ItemStack heldItem = player.getCurrentEquippedItem();
                    nethandler.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, heldItem, 0, 0, 0));
                } else {
                    ClickManager.getSharedInstance().rightClick();
                }
            }
        }
    }

    private EntityFishHook findPlayerFishHook() {
        List<Entity> entities = Helper.getWorld().loadedEntityList;

        for (Entity entity : entities) {
            if (entity instanceof EntityFishHook hook) {
                if (hook.angler != null && hook.angler.getUniqueID().equals(Helper.getPlayer().getUniqueID()))
                    return hook;
            }
        }
        return null;
    }

    private void dropRubbishFishedItems() {
        EntityPlayerSP player = Helper.getPlayer();
        if(player == null) return;

        if(player.openContainer instanceof ContainerPlayer container) {
            int size = container.inventorySlots.size();
            dropRubbish(container, 9, size);
        } else if(player.openContainer instanceof ContainerChest container) {
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
                    if (stack != null && stack.getItem() != Items.fishing_rod &&
                        !shouldDropItem(stack) && executedShifts < maxShifts) {
                        ClickManager.getSharedInstance().addClick(
                            QueueType.MEDIUM, new Click(container.windowId, i, 0, 1));
                        executedShifts++;
                    }
                }
            }
        }
    }

    private boolean shouldDropItem(ItemStack stack) {
        if (stack == null || stack.hasDisplayName()) {
            return false;
        }

        Item item = stack.getItem();
        int meta = stack.getMetadata();
        AutoFisherSubConfig subConfig = Addon.getSharedInstance().configuration().getAutoFisherSubConfig();

        return (item == Items.fishing_rod && subConfig.getAutoDropRods().get()) ||
            (item == Items.bow && subConfig.getAutoDropBows().get()) ||
            (item == Items.enchanted_book && subConfig.getAutoDropBooks().get()) ||
            (item == Items.saddle && subConfig.getAutoDropSaddle().get()) ||
            (Item.getIdFromItem(item) == 111 && subConfig.getAutoDropLilypads().get()) ||
            (item == Items.fish && subConfig.getAutoDropFish().get() && (meta == 0 || meta == 1));
    }

    private void dropRubbish(Container container, int minSlot, int maxSlot) {
        for(int i = minSlot; i < maxSlot; i++) {
            Slot slot = container.getSlot(i);
            if(slot != null && slot.getHasStack()) {
                ItemStack stack = slot.getStack();
                if (shouldDropItem(stack)) {
                    ClickManager.getSharedInstance().dropClick(i);
                }
            }
        }
    }

    private int getEmptySlotsInInventory(IInventory inventory) {
        int count = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if(inventory.getStackInSlot(i) == null) count++;
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
        EntityPlayerSP player = mc.thePlayer;
        if (player != null) {
            boolean rodFound = false;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);

                if (stack != null && stack.getItem().equals(Items.fishing_rod)) {
                    int itemDamage = stack.getItemDamage();
                    int maxDamage = stack.getMaxDamage();
                    if(itemDamage < maxDamage) {
                        player.inventory.currentItem = i;
                        rodFound = true;

                        new Thread(() -> {
                            try {
                                Thread.sleep(200);
                                NetHandlerPlayClient nethandler = Minecraft.getMinecraft().getNetHandler();
                                if(nethandler != null) {
                                    ItemStack heldItem = player.getCurrentEquippedItem();
                                    nethandler.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, heldItem, 0, 0, 0));
                                } else
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