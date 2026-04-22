package tmb.randy.tmbgriefergames.v1_12_2;

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
import tmb.randy.tmbgriefergames.v1_12_2.click.ClickManager;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoComp;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter.AutoCrafterV1;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter.AutoCrafterV2;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter.AutoCrafterV3;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoDecomp;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoFisher;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoHopper;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoLoot;
import tmb.randy.tmbgriefergames.v1_12_2.functions.BlockMarker;
import tmb.randy.tmbgriefergames.v1_12_2.functions.Eject;
import tmb.randy.tmbgriefergames.v1_12_2.functions.HABK;
import tmb.randy.tmbgriefergames.v1_12_2.functions.HopperConnections;
import tmb.randy.tmbgriefergames.v1_12_2.functions.InfinityMiner;
import tmb.randy.tmbgriefergames.v1_12_2.functions.ItemShifter;
import tmb.randy.tmbgriefergames.v1_12_2.functions.NatureBordersRenderer;
import tmb.randy.tmbgriefergames.v1_12_2.functions.PotionTimer;
import tmb.randy.tmbgriefergames.v1_12_2.functions.VABK;

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
        Addon.registerFunction(new AutoCrafterV1());
        Addon.registerFunction(new AutoCrafterV2());
        Addon.registerFunction(new AutoCrafterV3());
        Addon.registerFunction(new AutoComp());
        Addon.registerFunction(new AutoDecomp());
        Addon.registerFunction(new AutoHopper());
        Addon.registerFunction(new AutoLoot());
        Addon.registerFunction(new BlockMarker());
        Addon.registerFunction(new Eject());
        Addon.registerFunction(new PotionTimer());
        Addon.registerFunction(new HABK());
        Addon.registerFunction(new HopperConnections());
        Addon.registerFunction(new ItemShifter());
        Addon.registerFunction(new NatureBordersRenderer());
        Addon.registerFunction(new VABK());
        Addon.registerFunction(new AutoFisher());
        Addon.registerFunction(new InfinityMiner());
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
                Laby.fireEvent(new ToggleFunctionEvent(Functions.COMP.name(), FunctionState.STOP, null));
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

        double difference = event.getNewBalance() - event.getOldBalance();

        NumberFormat format = NumberFormat.getNumberInstance(Locale.GERMANY);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        format.setGroupingUsed(true);

        Minecraft.getMinecraft().ingameGUI.setOverlayMessage((difference < 0 ? "§c§l" : "§a§l+")+ format.format(difference) + "$", true);
    }


    public void onGuiOpenEvent(GuiScreen screen) {
        ClickManager.getSharedInstance().clearAllQueues();
        if(screen == null) {
            Laby.fireEvent(new ToggleFunctionEvent(Functions.COMP.name(), FunctionState.STOP, null));
            if(Addon.getFunction(Functions.ITEMSHIFTER.name()) instanceof ItemShifter itemShifter)
                itemShifter.stopShifting();
        }
        if(Addon.getFunction(Functions.HOPPERCONNECTIONS.name()) instanceof HopperConnections hopperConnections)
            hopperConnections.onGuiOpenEvent();
    }

    public void changeSlot(int slot) { Helper.getPlayer().inventory.currentItem = slot;}

    public static VersionedConnect getSharedInstance() {return sharedInstance;}
}
