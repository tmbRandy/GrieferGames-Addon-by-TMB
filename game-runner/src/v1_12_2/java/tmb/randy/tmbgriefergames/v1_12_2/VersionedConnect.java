package tmb.randy.tmbgriefergames.v1_12_2;

import javax.inject.Singleton;
import net.labymod.api.Laby;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.models.Implements;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import tmb.randy.tmbgriefergames.api.enums.FunctionState;
import tmb.randy.tmbgriefergames.api.events.MoneyBalanceChangedEvent;
import tmb.randy.tmbgriefergames.api.events.ToggleFunctionEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.IConnect;
import tmb.randy.tmbgriefergames.core.click.ClickManager;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoComp;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter.AutoCrafterV1;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter.AutoCrafterV2;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoCrafter.AutoCrafterV3;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoDecomp;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoFisher;
import tmb.randy.tmbgriefergames.v1_12_2.functions.AutoHopper;
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
    private final ClickManager clickManager = new VersionedClickManager();
    private GuiScreen lastGui;

    @Override
    public void loadFunctions() {
        Addon.registerFunction(new AutoCrafterV1());
        Addon.registerFunction(new AutoCrafterV2());
        Addon.registerFunction(new AutoCrafterV3());
        Addon.registerFunction(new AutoComp());
        Addon.registerFunction(new AutoDecomp());
        Addon.registerFunction(new AutoHopper());
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

    @Override
    public void changeSlot(int slot) {
        Helper.setHotbarSlot(slot);
    }

    @Override
    public String getChestTitle() {
        return Helper.getChestTitle();
    }

    @Override
    public String getSlotItem(int slot) {
        ItemStack stack = getStack(slot);
        return stack == null ? null : Helper.getRegistryName(stack);
    }

    @Override
    public String getSlotItemName(int slot) {
        ItemStack stack = getStack(slot);
        return stack == null ? null : stack.getDisplayName();
    }

    @Override
    public int getSlotCount(int slot) {
        return Helper.getCount(getStack(slot));
    }

    private static ItemStack getStack(int slot) {
        return slot >= 0 && slot < Helper.getContainerSize() ? Helper.getContainerStack(slot) : null;
    }

    @Override
    public boolean isInventoryFull() {
        return Helper.isInventoryFull();
    }

    @Override
    public int countInventoryStacks(String registryName) {
        return Helper.countInventory(stack -> Helper.isItem(stack, registryName));
    }

    @Override
    public int[] getBlockLookingAt() {
        BlockPos pos = Helper.getBlockPosLookingAt();
        return pos == null ? null : new int[] {pos.getX(), pos.getY(), pos.getZ()};
    }

    @Override
    public void lookAtBlock(int x, int y, int z) {
        Helper.lookAtBlockPos(new BlockPos(x, y, z));
    }

    @Override
    public void rightClickBlockLookingAt() {
        Helper.rightClickBlockLookingAt();
    }

    @Override
    public void useHeldItem() {
        Helper.rightClick();
    }

    @Override
    public void closeScreen() {
        Helper.closeScreen();
    }

    @Subscribe
    public void tick(GameTickEvent event) {
        if (!Addon.isGG())
            return;

        GuiScreen currentScreen = Helper.mc().currentScreen;
        if (lastGui != currentScreen) {
            lastGui = currentScreen;
            onGuiChanged();
        }

        clickManager.tick();
    }

    @Subscribe
    public void moneyBalanceChangedEvent(MoneyBalanceChangedEvent event) {
        if (Addon.isGG())
            Helper.showActionBar(Helper.formatMoneyDifference(event.getNewBalance() - event.getOldBalance()));
    }

    private void onGuiChanged() {
        clickManager.clearAllQueues();

        if (!Helper.isClickableScreen()) {
            Laby.fireEvent(new ToggleFunctionEvent(Functions.COMP.name(), FunctionState.STOP, null));
            if (Addon.getFunction(Functions.ITEMSHIFTER.name()) instanceof ItemShifter itemShifter)
                itemShifter.stopShifting();
        }

        if (Addon.getFunction(Functions.HOPPERCONNECTIONS.name()) instanceof HopperConnections hopperConnections)
            hopperConnections.onGuiOpenEvent();
    }
}
