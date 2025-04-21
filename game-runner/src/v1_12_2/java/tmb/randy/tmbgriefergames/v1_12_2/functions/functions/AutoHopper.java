package tmb.randy.tmbgriefergames.v1_12_2.functions.functions;

import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemSkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Keyboard;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.enums.HopperFinalAction;
import tmb.randy.tmbgriefergames.core.enums.HopperItemStackSizeEnum;
import tmb.randy.tmbgriefergames.core.enums.QueueType;
import tmb.randy.tmbgriefergames.core.functions.Function;
import tmb.randy.tmbgriefergames.v1_12_2.functions.Helper;
import tmb.randy.tmbgriefergames.v1_12_2.functions.click.Click;
import tmb.randy.tmbgriefergames.v1_12_2.functions.click.ClickManager;

public class AutoHopper extends Function {
    boolean receivedPlotBorderMessage = false;
    private boolean toggeledSneak = false;

    public AutoHopper() {
        super(Functions.AUTOHOPPER);
    }

    @Override
    public void mouseButtonEvent(MouseButtonEvent event) {
        if(event.button().isRight() && event.action() == Action.CLICK && isLookingAtHopper() && Addon.getSharedInstance().configuration().getHopperSubConfig().getAutoSneak().get()) {
            RayTraceResult trace = Helper.getPlayer().rayTrace(5, 1.0F);

            if(trace != null) {
                Keyboard.enableRepeatEvents(true);
                KeyBinding sneakKey = Minecraft.getMinecraft().gameSettings.keyBindSneak;
                KeyBinding jumpKey = Minecraft.getMinecraft().gameSettings.keyBindJump;
                KeyBinding.setKeyBindState(sneakKey.getKeyCode(), true);
                if(!Helper.getPlayer().onGround) {
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
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if(Addon.getSharedInstance().configuration().getHopperSubConfig().getEnabled().get()) {
            Container cont = Helper.getPlayer().openContainer;
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
                    if (ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM)) {
                        if (Addon.getSharedInstance().configuration().getHopperSubConfig().getFilterItem().get() &&
                            (((!chest.getSlot(28).getStack().getItem().equals(chest.getSlot(72).getStack().getItem())) &&
                            !(Block.getBlockFromItem(chest.getSlot(28).getStack().getItem()) == Blocks.BARRIER && !chest.getSlot(72).getHasStack())) ||
                                (chest.getSlot(28).getStack().getMetadata() != chest.getSlot(72).getStack().getMetadata()))) {
                            ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 72, 0, ClickType.QUICK_MOVE));
                            clicked = true;
                        }

                        if (Addon.getSharedInstance().configuration().getHopperSubConfig().getRadius().get() > -1 && !receivedPlotBorderMessage && !clicked) {
                            if (Addon.getSharedInstance().configuration().getHopperSubConfig().getRadius().get() == 0) {
                                if (chest.getSlot(30).getStack().getItem() instanceof ItemSkull) {
                                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 30, 0, ClickType.QUICK_MOVE));
                                    clicked = true;
                                }
                            } else {
                                if (Addon.getSharedInstance().configuration().getHopperSubConfig().getRadius().get() > chest.getSlot(31).getStack().getCount()) {
                                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 32, 0, ClickType.QUICK_MOVE));
                                    clicked = true;
                                } else if (Addon.getSharedInstance().configuration().getHopperSubConfig().getRadius().get() < chest.getSlot(31).getStack().getCount()) {
                                    ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 30, 0, ClickType.QUICK_MOVE));
                                    clicked = true;
                                }
                            }
                        }

                        if(!clicked && Addon.getSharedInstance().configuration().getHopperSubConfig().getStackSize().get() != HopperItemStackSizeEnum.NONE && chest.inventorySlots.get(10).getHasStack()) {
                            int currentStackSize = chest.getSlot(10).getStack().getCount();
                            switch (Addon.getSharedInstance().configuration().getHopperSubConfig().getStackSize().get()) {
                                case SINGLEITEM:
                                    if(currentStackSize != 1) {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 10, 0, ClickType.QUICK_MOVE));
                                        clicked = true;
                                    }
                                    break;
                                case TWELVE:
                                    if(currentStackSize != 12) {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 10, 0, ClickType.QUICK_MOVE));
                                        clicked = true;
                                    }
                                    break;
                                case FULLSTACK:
                                    if(currentStackSize != 64) {
                                        ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 10, 0, ClickType.QUICK_MOVE));
                                        clicked = true;
                                    }
                                    break;
                            }
                        }

                        if (!clicked && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM) && Addon.getSharedInstance().configuration().getHopperSubConfig().getFinalAction().get() != HopperFinalAction.NONE && chest.inventorySlots.get(10).getHasStack()) {
                            if (Addon.getSharedInstance().configuration().getHopperSubConfig().getFinalAction().get() == HopperFinalAction.CLOSE) {
                                Helper.getPlayer().closeScreen();
                            } else if (Addon.getSharedInstance().configuration().getHopperSubConfig().getFinalAction().get() == HopperFinalAction.CONNECT) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 16, 0, ClickType.PICKUP));
                            } else if (Addon.getSharedInstance().configuration().getHopperSubConfig().getFinalAction().get() == HopperFinalAction.MULTICONNECTION) {
                                ClickManager.getSharedInstance().addClick(QueueType.MEDIUM, new Click(chest.windowId, 15, 0, ClickType.PICKUP));
                            }
                        }
                    }

                } else if (inv.getName().equalsIgnoreCase("§6Trichter-Mehrfach-Verbindungen") && ClickManager.getSharedInstance().isClickQueueEmpty(QueueType.MEDIUM) && Addon.getSharedInstance().configuration().getHopperSubConfig().getFinalAction().get() == HopperFinalAction.MULTICONNECTION) {
                    ClickManager.getSharedInstance().addClick(
                        QueueType.MEDIUM, new Click(chest.windowId, 53, 0, ClickType.PICKUP));
                }
            } else {
                if(receivedPlotBorderMessage) {
                    receivedPlotBorderMessage = false;
                }
            }
        }
    }

    @Override
    public void chatReceiveEvent(ChatReceiveEvent event) {
        if(event.chatMessage().getPlainText().endsWith("Das Ende vom Grundstück wurde erreicht.")) {
            receivedPlotBorderMessage = true;
        }
    }

    private boolean isLookingAtHopper() {
        BlockPos blockPos = Helper.getBlockPosLookingAt();
        if (blockPos != null) {
            Block block = Helper.getWorld().getBlockState(blockPos).getBlock();

            return (block instanceof BlockHopper);
        }
        return false;
    }
}