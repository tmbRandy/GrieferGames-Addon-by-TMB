package tmb.randy.tmbgriefergames.v1_8_9.util;

import net.labymod.api.event.client.chat.ChatReceiveEvent;
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
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.input.Keyboard;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.HopperFinalAction;
import tmb.randy.tmbgriefergames.core.enums.HopperItemStackSizeEnum;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.v1_8_9.util.click.Click;
import tmb.randy.tmbgriefergames.v1_8_9.util.click.ClickManager;

public class AutoHopper {
    boolean receivedPlotBorderMessage = false;
    private boolean toggeledSneak = false;

    public void mouseInput(MouseButtonEvent event) {
        if(event.button().isRight() && event.action() == Action.CLICK && isLookingAtHopper() && Addon.getSharedInstance().configuration().getHopperSubConfig().getAutoSneak().get()) {
            MovingObjectPosition trace = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1.0F);

            Keyboard.enableRepeatEvents(true);
            KeyBinding sneakKey = Minecraft.getMinecraft().gameSettings.keyBindSneak;
            KeyBinding jumpKey = Minecraft.getMinecraft().gameSettings.keyBindJump;
            KeyBinding.setKeyBindState(sneakKey.getKeyCode(), true);
            if(!Minecraft.getMinecraft().thePlayer.onGround) {
                KeyBinding.setKeyBindState(jumpKey.getKeyCode(), true);
            }
            Minecraft.getMinecraft().playerController.clickBlock(trace.getBlockPos(), trace.sideHit);
            toggeledSneak = true;

            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if(toggeledSneak) {
                            KeyBinding sneakKey = Minecraft.getMinecraft().gameSettings.keyBindSneak;
                            KeyBinding jumpKey = Minecraft.getMinecraft().gameSettings.keyBindJump;
                            KeyBinding.setKeyBindState(sneakKey.getKeyCode(), false);
                            KeyBinding.setKeyBindState(jumpKey.getKeyCode(), false);
                            Keyboard.enableRepeatEvents(false);
                            toggeledSneak = false;
                        }
                    }
                }, 10
            );
        }
    }
    public void tick(GameTickEvent event) {
        if(Addon.getSharedInstance().configuration().getHopperSubConfig().getEnabled().get()) {
            Container cont = Minecraft.getMinecraft().thePlayer.openContainer;
            if (cont instanceof ContainerChest chest) {
                IInventory inv = chest.getLowerChestInventory();
                if (inv.getName().equalsIgnoreCase("§6Trichter-Einstellungen")) {

                    if(toggeledSneak) {
                        KeyBinding sneakKey = Minecraft.getMinecraft().gameSettings.keyBindSneak;
                        KeyBinding jumpKey = Minecraft.getMinecraft().gameSettings.keyBindJump;
                        KeyBinding.setKeyBindState(sneakKey.getKeyCode(), false);
                        KeyBinding.setKeyBindState(jumpKey.getKeyCode(), false);
                        Keyboard.enableRepeatEvents(false);
                        toggeledSneak = false;
                    }

                    boolean clicked = false;
                    if (ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM) && chest.getSlot(28).getHasStack()) {
                        if (Addon.getSharedInstance().configuration().getHopperSubConfig().getFilterItem().get()) {

                            if(chest.getSlot(72).getHasStack()) {
                                if(((!chest.getSlot(28).getStack().getItem().equals(chest.getSlot(72).getStack().getItem())) &&
                                    !(Block.getBlockFromItem(chest.getSlot(28).getStack().getItem()) == Blocks.barrier && !chest.getSlot(72).getHasStack())) ||
                                    (chest.getSlot(28).getStack().getMetadata() != chest.getSlot(72).getStack().getMetadata())) {
                                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 72, 0, 1));
                                    clicked = true;
                                }
                            } else if(!(Block.getBlockFromItem(chest.getSlot(28).getStack().getItem()) == Blocks.barrier)) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 72, 0, 1));
                                clicked = true;
                            }
                        }

                            if (Addon.getSharedInstance().configuration().getHopperSubConfig().getRadius().get() > -1 && !receivedPlotBorderMessage && !clicked) {
                            if (Addon.getSharedInstance().configuration().getHopperSubConfig().getRadius().get() == 0) {
                                if (chest.getSlot(30).getStack() != null && Item.getIdFromItem(chest.getSlot(30).getStack().getItem()) == 397) {
                                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 30, 0, 1));
                                    clicked = true;
                                }
                            } else {
                                if (Addon.getSharedInstance().configuration().getHopperSubConfig().getRadius().get() > chest.getSlot(31).getStack().stackSize) {
                                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 32, 0, 1));
                                    clicked = true;
                                } else if (Addon.getSharedInstance().configuration().getHopperSubConfig().getRadius().get() < chest.getSlot(31).getStack().stackSize) {
                                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 30, 0, 1));
                                    clicked = true;
                                }
                            }
                        }

                        if(!clicked && Addon.getSharedInstance().configuration().getHopperSubConfig().getStackSize().get() != HopperItemStackSizeEnum.NONE && chest.inventorySlots.get(10).getHasStack()) {
                            int currentStackSize = chest.getSlot(10).getStack().stackSize;
                            switch (Addon.getSharedInstance().configuration().getHopperSubConfig().getStackSize().get()) {
                                case SINGLE_ITEM:
                                    if(currentStackSize != 1) {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 10, 0, 1));
                                        clicked = true;
                                    }
                                    break;
                                case TWELVE:
                                    if(currentStackSize != 12) {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 10, 0, 1));
                                        clicked = true;
                                    }
                                    break;
                                case FULL_STACK:
                                    if(currentStackSize != 64) {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 10, 0, 1));
                                        clicked = true;
                                    }
                                    break;
                            }
                        }

                        if (!clicked && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM) && Addon.getSharedInstance().configuration().getHopperSubConfig().getFinalAction().get() != HopperFinalAction.NONE && chest.inventorySlots.get(10).getHasStack()) {
                            if (Addon.getSharedInstance().configuration().getHopperSubConfig().getFinalAction().get() == HopperFinalAction.CLOSE) {
                                Minecraft.getMinecraft().thePlayer.closeScreen();
                            } else if (Addon.getSharedInstance().configuration().getHopperSubConfig().getFinalAction().get() == HopperFinalAction.CONNECT) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 16, 0, 0));
                            } else if (Addon.getSharedInstance().configuration().getHopperSubConfig().getFinalAction().get() == HopperFinalAction.MULTI_CONNECTION) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 15, 0, 0));
                            }
                        }
                    }

                } else if (inv.getName().equalsIgnoreCase("§6Trichter-Mehrfach-Verbindungen") && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM) && Addon.getSharedInstance().configuration().getHopperSubConfig().getFinalAction().get() == HopperFinalAction.MULTI_CONNECTION) {
                    ClickManager.getSharedInstance().addClick(
                        QueueType.MEDIUM, new Click(chest.windowId, 53, 0, 0));
                }
            } else {
                if(receivedPlotBorderMessage) {
                    receivedPlotBorderMessage = false;
                }
            }
        }
    }

    public void messageReceived(ChatReceiveEvent event) {
        if(event.chatMessage().getPlainText().endsWith("Das Ende vom Grundstück wurde erreicht.")) {
            receivedPlotBorderMessage = true;
        }
    }

    private boolean isLookingAtHopper() {
        if(Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null)
            return false;

        MovingObjectPosition trace = Minecraft.getMinecraft().thePlayer.rayTrace(5, 1.0F);
        if (trace != null && trace.typeOfHit == MovingObjectType.BLOCK) {
            BlockPos blockPos = trace.getBlockPos();
            Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();

            return (block instanceof BlockHopper);
        }
        return false;
    }
}

