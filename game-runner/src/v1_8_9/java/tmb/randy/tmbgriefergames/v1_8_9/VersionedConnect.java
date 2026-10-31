package tmb.randy.tmbgriefergames.v1_8_9;

import javax.inject.Singleton;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.models.Implements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.BlockPos;
import tmb.randy.tmbgriefergames.api.events.MoneyBalanceChangedEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.IConnect;
import tmb.randy.tmbgriefergames.core.click.ClickManager;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.v1_8_9.functions.AutoCrafter.AutoCrafterV1;
import tmb.randy.tmbgriefergames.v1_8_9.functions.AutoCrafter.AutoCrafterV2;
import tmb.randy.tmbgriefergames.v1_8_9.functions.AutoCrafter.AutoCrafterV3;
import tmb.randy.tmbgriefergames.v1_8_9.functions.AutoDecomp;
import tmb.randy.tmbgriefergames.v1_8_9.functions.AutoFisher;
import tmb.randy.tmbgriefergames.v1_8_9.functions.AutoHopper;
import tmb.randy.tmbgriefergames.v1_8_9.functions.BlockMarker;
import tmb.randy.tmbgriefergames.v1_8_9.functions.Eject;
import tmb.randy.tmbgriefergames.v1_8_9.functions.HABK;
import tmb.randy.tmbgriefergames.v1_8_9.functions.HopperConnections;
import tmb.randy.tmbgriefergames.v1_8_9.functions.InfinityMiner;
import tmb.randy.tmbgriefergames.v1_8_9.functions.ItemShifter;
import tmb.randy.tmbgriefergames.v1_8_9.functions.NatureBordersRenderer;
import tmb.randy.tmbgriefergames.v1_8_9.functions.PotionTimer;
import tmb.randy.tmbgriefergames.v1_8_9.functions.VABK;

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

    @Override
    public int getContainerSize() {
        return Helper.getContainerSize();
    }

    @Override
    public int getChestSize() {
        return Helper.getChestSize();
    }

    @Override
    public int getSlotCompressionLevel(int slot) {
        ItemStack stack = getStack(slot);
        int level = Helper.getCompressionLevel(stack).getLevel();
        // Decompressed items only carry the level as roman numeral in their name
        return level == 0 && stack != null && stack.hasDisplayName() ? Helper.getRomanSuffixLevel(stack.getDisplayName()) : level;
    }

    @Override
    public String getHeldItem() {
        ItemStack stack = Helper.getHeldItem();
        return Helper.isEmpty(stack) ? null : Helper.getRegistryName(stack);
    }

    @Override
    public int getHeldItemDurability() {
        ItemStack stack = Helper.getHeldItem();
        return Helper.isEmpty(stack) || stack.getMaxDamage() <= 0 ? -1 : stack.getMaxDamage() - stack.getItemDamage();
    }

    @Override
    public int getHotbarSlot() {
        return Helper.getHotbarSlot();
    }

    @Override
    public String getBlockAt(int x, int y, int z) {
        return Helper.getBlockName(new BlockPos(x, y, z));
    }

    @Override
    public double[] getPlayerPosition() {
        return new double[] {Helper.getPlayer().posX, Helper.getPlayer().posY, Helper.getPlayer().posZ};
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        Helper.getPlayer().rotationYaw = yaw;
        Helper.getPlayer().rotationPitch = pitch;
    }

    @Override
    public void setKeyPressed(String key, boolean pressed) {
        GameSettings settings = Helper.mc().gameSettings;
        KeyBinding binding = switch (key.toLowerCase()) {
            case "sneak" -> settings.keyBindSneak;
            case "jump" -> settings.keyBindJump;
            case "sprint" -> settings.keyBindSprint;
            case "forward" -> settings.keyBindForward;
            case "back" -> settings.keyBindBack;
            case "left" -> settings.keyBindLeft;
            case "right" -> settings.keyBindRight;
            case "attack" -> settings.keyBindAttack;
            case "use" -> settings.keyBindUseItem;
            default -> null;
        };
        if (binding != null)
            KeyBinding.setKeyBindState(binding.getKeyCode(), pressed);
    }

    @Override
    public void leftClickBlockLookingAt() {
        Helper.clickBlockLookingAt();
        Helper.swingArm();
    }

    @Override
    public boolean breakBlockLookingAt() {
        boolean breaking = Helper.damageBlockLookingAt();
        if (breaking)
            Helper.swingArm();
        return breaking;
    }

    @Override
    public void releaseUseItem() {
        Helper.releaseBow();
    }

    @Override
    public void swingArm() {
        Helper.swingArm();
    }

    @Override
    public void openInventory() {
        Helper.openInventory();
    }

    @Override
    public String getScreenType() {
        if (Helper.mc().currentScreen == null)
            return "none";
        if (Helper.isInventoryScreen())
            return "inventory";
        if (Helper.isChestScreen())
            return "chest";
        if (Helper.isCraftingScreen())
            return "crafting";
        return "other";
    }

    @Override
    public void takeScreenshot(String name) {
        Minecraft mc = Helper.mc();
        ScreenShotHelper.saveScreenshot(mc.mcDataDir, name + ".png", mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
    }

    @Override
    public boolean isFishBiting() {
        EntityFishHook hook = Helper.getFishHook();
        return hook != null && hook.motionY < Helper.FISH_BITE_MOTION_Y && hook.isInWater() && hook.motionX == 0 && hook.motionZ == 0;
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
            if (Addon.getFunction(Functions.ITEMSHIFTER.name()) instanceof ItemShifter itemShifter)
                itemShifter.stopShifting();
        }

        if (Addon.getFunction(Functions.HOPPERCONNECTIONS.name()) instanceof HopperConnections hopperConnections)
            hopperConnections.onGuiOpenEvent();
    }
}
