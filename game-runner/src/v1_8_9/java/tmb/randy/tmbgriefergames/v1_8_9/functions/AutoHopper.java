package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemSkull;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.enums.HopperFinalAction;
import tmb.randy.tmbgriefergames.core.enums.HopperItemStackSizeEnum;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.AutoHopperMaster;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;
import tmb.randy.tmbgriefergames.v1_8_9.click.Click;
import tmb.randy.tmbgriefergames.v1_8_9.click.ClickManager;

public class AutoHopper extends AutoHopperMaster {

    @Override
    public void mouseButtonEvent(MouseButtonEvent event) {
        if(event.button().isRight() && event.action() == Action.CLICK && Helper.getBlockLookingAt() instanceof BlockHopper && Addon.settings().getHopperSubConfig().getAutoSneak().get()) {
            MovingObjectPosition trace = Helper.getLookTrace();

            if(trace != null) {
                Keyboard.enableRepeatEvents(true);
                KeyBinding sneakKey = Minecraft.getMinecraft().gameSettings.keyBindSneak;
                KeyBinding jumpKey = Minecraft.getMinecraft().gameSettings.keyBindJump;
                KeyBinding.setKeyBindState(sneakKey.getKeyCode(), true);
                if(!Helper.getPlayer().onGround) {
                    KeyBinding.setKeyBindState(jumpKey.getKeyCode(), true);
                }
                Minecraft.getMinecraft().playerController.clickBlock(trace.getBlockPos(), trace.sideHit);
                toggledSneak = true;

                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            releaseSneakKeys();
                        }
                    }, 10
                );
            }
        }
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if(Addon.settings().getHopperSubConfig().getEnabled().get()) {
            Container cont = Helper.getPlayer().openContainer;
            if (cont instanceof ContainerChest chest) {
                IInventory inv = chest.getLowerChestInventory();
                if (inv.getName().equalsIgnoreCase(Const.Menu.TRICHTER_EINSTELLUNGEN)) {

                    releaseSneakKeys();

                    boolean clicked = false;
                    if (ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                        int radius = Addon.settings().getHopperSubConfig().getRadius().get();
                        if (Addon.settings().getHopperSubConfig().getFilterItem().get() &&
                            chest.getSlot(28).getStack() != null &&
                            chest.getSlot(72).getStack() != null &&
                            (((!chest.getSlot(28).getStack().getItem().equals(chest.getSlot(72).getStack().getItem())) &&
                                !(Block.getBlockFromItem(chest.getSlot(28).getStack().getItem()) == Blocks.barrier && !chest.getSlot(72).getHasStack())) ||
                                (chest.getSlot(28).getStack().getMetadata() != chest.getSlot(72).getStack().getMetadata()))) {
                            ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 72, 0, 1));
                            clicked = true;
                        }

                        if (radius > -1 && !receivedPlotBorderMessage && !clicked) {
                            if (radius == 0) {
                                if (!Helper.isStackEmpty(chest.getSlot(30).getStack()) && chest.getSlot(30).getStack().getItem() instanceof ItemSkull) {
                                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 30, 0, 1));
                                    clicked = true;
                                }
                            } else {
                                int currentRadius = Helper.getStackSize(chest.getSlot(31).getStack());
                                if (radius > currentRadius) {
                                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 32, 0, 1));
                                    clicked = true;
                                } else if (radius < currentRadius) {
                                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 30, 0, 1));
                                    clicked = true;
                                }
                            }
                        }

                        if(!clicked && Addon.settings().getHopperSubConfig().getStackSize().get() != HopperItemStackSizeEnum.NONE && chest.inventorySlots.get(10).getHasStack()) {
                            int currentStackSize = Helper.getStackSize(chest.getSlot(10).getStack());
                            if(needsStackSizeClick(Addon.settings().getHopperSubConfig().getStackSize().get(), currentStackSize)) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 10, 0, 1));
                                clicked = true;
                            }
                        }

                        if (!clicked && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM) && Addon.settings().getHopperSubConfig().getFinalAction().get() != HopperFinalAction.NONE && chest.inventorySlots.get(10).getHasStack()) {
                            if (Addon.settings().getHopperSubConfig().getFinalAction().get() == HopperFinalAction.CLOSE) {
                                Helper.getPlayer().closeScreen();
                            } else if (Addon.settings().getHopperSubConfig().getFinalAction().get() == HopperFinalAction.CONNECT) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 16, 0, 0));
                            } else if (Addon.settings().getHopperSubConfig().getFinalAction().get() == HopperFinalAction.MULTICONNECTION) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 15, 0, 0));
                            }
                        }
                    }

                } else if (inv.getName().equalsIgnoreCase(Const.Menu.TRICHTER_MEHRFACH_VERBINDUNGEN) && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM) && Addon.settings().getHopperSubConfig().getFinalAction().get() == HopperFinalAction.MULTICONNECTION) {
                    ClickManager.getSharedInstance().addClick(
                        QueueType.MEDIUM, new Click(chest.windowId, 53, 0, 0));
                }
            } else {
                if(receivedPlotBorderMessage) {
                    resetPlotBorderMessage();
                }
            }
        }
    }

    private void releaseSneakKeys() {
        if(toggledSneak) {
            KeyBinding sneakKey = Minecraft.getMinecraft().gameSettings.keyBindSneak;
            KeyBinding jumpKey = Minecraft.getMinecraft().gameSettings.keyBindJump;
            KeyBinding.setKeyBindState(sneakKey.getKeyCode(), false);
            KeyBinding.setKeyBindState(jumpKey.getKeyCode(), false);
            Keyboard.enableRepeatEvents(false);
            toggledSneak = false;
        }
    }
}