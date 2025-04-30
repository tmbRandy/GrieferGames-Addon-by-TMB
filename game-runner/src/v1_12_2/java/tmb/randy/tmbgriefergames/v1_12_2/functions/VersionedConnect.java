package tmb.randy.tmbgriefergames.v1_12_2.functions;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import javax.inject.Singleton;
import net.labymod.api.Laby;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.models.Implements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.IConnect;
import tmb.randy.tmbgriefergames.core.enums.FunctionState;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.events.MoneyBalanceChangedEvent;
import tmb.randy.tmbgriefergames.core.events.ToggleFunctionEvent;
import tmb.randy.tmbgriefergames.v1_12_2.functions.click.ClickManager;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.AutoComp;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.AutoCrafter.AutoCrafterV1;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.AutoCrafter.AutoCrafterV2;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.AutoCrafter.AutoCrafterV3;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.AutoDecomp;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.AutoFisher;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.AutoHopper;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.AutoLoot;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.BlockMarker;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.Eject;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.HABK;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.HopperConnections;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.ItemShifter;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.NatureBordersRenderer;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.PotionTimer;
import tmb.randy.tmbgriefergames.v1_12_2.functions.functions.VABK;

@Singleton
@Implements(IConnect.class)
public class VersionedConnect implements IConnect {
    private static VersionedConnect sharedInstance;

    private GuiScreen lastGui;

    public VersionedConnect() {
        sharedInstance = this;
    }

    @Override
    public void loadFunctions() {
        Addon.getSharedInstance().addFunction(new AutoCrafterV1());
        Addon.getSharedInstance().addFunction(new AutoCrafterV2());
        Addon.getSharedInstance().addFunction(new AutoCrafterV3());
        Addon.getSharedInstance().addFunction(new AutoComp());
        Addon.getSharedInstance().addFunction(new AutoDecomp());
        Addon.getSharedInstance().addFunction(new AutoHopper());
        Addon.getSharedInstance().addFunction(new AutoLoot());
        Addon.getSharedInstance().addFunction(new BlockMarker());
        Addon.getSharedInstance().addFunction(new Eject());
        Addon.getSharedInstance().addFunction(new PotionTimer());
        Addon.getSharedInstance().addFunction(new HABK());
        Addon.getSharedInstance().addFunction(new HopperConnections());
        Addon.getSharedInstance().addFunction(new ItemShifter());
        Addon.getSharedInstance().addFunction(new NatureBordersRenderer());
        Addon.getSharedInstance().addFunction(new VABK());
        Addon.getSharedInstance().addFunction(new AutoFisher());
    }

    @Subscribe
    public void tick(GameTickEvent event) {
        if(!Addon.isGG())
            return;

        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

        if (lastGui != currentScreen) {
            if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest
                || Minecraft.getMinecraft().currentScreen instanceof GuiCrafting
                || Minecraft.getMinecraft().currentScreen instanceof GuiInventory)) {
                ClickManager.getSharedInstance().clearAllQueues();
                Laby.fireEvent(new ToggleFunctionEvent(Functions.COMP, FunctionState.STOP, null));
                ItemShifter.getSharedInsance().stopShifting();
            }

            onGuiOpenEvent(currentScreen);
            lastGui = currentScreen;
        }

        ClickManager.getSharedInstance().tick();
    }

    @Subscribe
    public void moneyBalanceChangedEvent(MoneyBalanceChangedEvent event) {
        if(!Addon.isGG())
            return;

        BigDecimal difference = event.getNewBalance().subtract(event.getOldBalance());

        NumberFormat format = NumberFormat.getNumberInstance(Locale.GERMANY);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        format.setGroupingUsed(true);

        Minecraft.getMinecraft().ingameGUI.setOverlayMessage((difference.signum() < 0 ? "§c§l" : "§a§l+")+ format.format(difference) + "$", true);
    }


    public void onGuiOpenEvent(GuiScreen screen) {
        ClickManager.getSharedInstance().clearAllQueues();
        if(screen == null) {
            Laby.fireEvent(new ToggleFunctionEvent(Functions.COMP, FunctionState.STOP, null));
            if(Addon.getSharedInstance().getFunction(Functions.ITEMSHIFTER) instanceof ItemShifter itemShifter)
                itemShifter.stopShifting();
        }
        if(Addon.getSharedInstance().getFunction(Functions.HOPPERCONNECTIONS) instanceof HopperConnections hopperConnections)
            hopperConnections.onGuiOpenEvent();
    }

    public void changeSlot(int slot) { Helper.getPlayer().inventory.currentItem = slot;}

    public static VersionedConnect getSharedInstance() {return sharedInstance;}
}
