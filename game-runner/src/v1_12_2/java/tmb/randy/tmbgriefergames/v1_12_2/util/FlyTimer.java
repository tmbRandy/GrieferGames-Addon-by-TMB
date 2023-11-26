package tmb.randy.tmbgriefergames.v1_12_2.util;

import net.labymod.api.Laby;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.event.client.input.MouseButtonEvent;
import net.labymod.api.event.client.input.MouseButtonEvent.Action;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import tmb.randy.tmbgriefergames.core.Addon;
import java.util.Date;

public class FlyTimer {
    private ItemStack lastRenderedTooltipItemStack;

    Date totalDurationTime;

    public void tick(GameTickEvent event) {
        if (totalDurationTime != null) {
            if (getRemainingTotalTimeSeconds() < 0L) {
                totalDurationTime = null;
                Addon.getSharedInstance().displayNotification("Dein Flugtrank ist abgelaufen.");
            }
        }
    }

    public void onTooltipEvent(ItemStackTooltipEvent event) {
        this.lastRenderedTooltipItemStack = event.itemStack();
    }
    public void onMouseButtonEvent(MouseButtonEvent event) {
        if (!Laby.labyAPI().minecraft().isIngame())
            return;

        if (event.action() == Action.CLICK && event.button().isLeft()) {
            Container cont = Minecraft.getMinecraft().player.openContainer;
            if(cont != null) {
                if(cont instanceof ContainerChest chest) {
                    IInventory inv = chest.getLowerChestInventory();
                    if(inv.getName().equalsIgnoreCase("§6Möchtest du den Trank benutzen?")) {
                        if(lastRenderedTooltipItemStack != null) {
                            if(lastRenderedTooltipItemStack.getAsItem().getIdentifier().getPath().equals("dye") && lastRenderedTooltipItemStack.getDisplayName().toString().contains("Bestätigen")) {
                                startFlyTimer();
                                Addon.getSharedInstance().displayNotification(I18n.getTranslation("tmbgriefergames.flyTimer.flyPotionUsed"));
                            }
                        }
                    }
                }
            }
        }
    }

    private void startFlyTimer() {
        Date currentTime = new Date();
        totalDurationTime = new Date(currentTime.getTime() + 15 * 60 * 1000);
    }

    private String getRemainingTotalTimeString() {
        if(totalDurationTime == null)
            return "";

        Date currentTime = new Date();
        long remainingTime = totalDurationTime.getTime() - currentTime.getTime();
        int remainingMinutes = (int) (remainingTime / (60 * 1000));
        int remainingSeconds = (int) ((remainingTime / 1000) % 60);

        String separator = ":";
        if(remainingSeconds < 10) {
            separator = ":0";
        }

        return remainingMinutes + separator + remainingSeconds;
    }

    public String getWidgetString() {
        return getRemainingTotalTimeString();
    }

    private long getRemainingTotalTimeSeconds() {
        Date currentTime = new Date();
        return totalDurationTime.getTime() - currentTime.getTime();
    }

    public boolean isTotalCountdownActive() {
        if(totalDurationTime != null) {
            return getRemainingTotalTimeSeconds() > 0L;
        }
        return false;
    }
}