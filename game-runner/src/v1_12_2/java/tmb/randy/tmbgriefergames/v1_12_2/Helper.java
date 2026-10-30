package tmb.randy.tmbgriefergames.v1_12_2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.click.Click;
import tmb.randy.tmbgriefergames.core.enums.CompressionLevel;
import tmb.randy.tmbgriefergames.core.helper.BaseHelper;

/**
 * Wraps everything that differs between the Minecraft versions. The Helper of the other version offers the same
 * functions, so the function classes of both versions can stay identical.
 */
public class Helper extends BaseHelper {
    private static final double REACH = 5;
    /** Downwards motion of the hook that counts as a bite. */
    public static final double FISH_BITE_MOTION_Y = -0.05;

    // ---------------------------------------------------------------- Basics

    public static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    public static EntityPlayerSP getPlayer() {
        return mc().player;
    }

    public static WorldClient getWorld() {
        return mc().world;
    }

    // ---------------------------------------------------------------- Item stacks

    public static boolean isEmpty(ItemStack stack) {
        return stack == null || stack.isEmpty();
    }

    public static int getCount(ItemStack stack) {
        return isEmpty(stack) ? 0 : stack.getCount();
    }

    public static boolean isFullStack(ItemStack stack) {
        return !isEmpty(stack) && stack.getCount() == stack.getMaxStackSize();
    }

    public static String getRegistryName(ItemStack stack) {
        return isEmpty(stack) ? "" : Item.REGISTRY.getNameForObject(stack.getItem()).toString();
    }

    public static boolean isItem(ItemStack stack, String registryName) {
        return getRegistryName(stack).equals(registryName);
    }

    /** Same item and same metadata, a meta of -1 matches every metadata. */
    public static boolean matches(ItemStack stack, Item item, int meta) {
        return !isEmpty(stack) && stack.getItem() == item && (meta == -1 || stack.getMetadata() == meta);
    }

    public static boolean isSameItem(ItemStack first, ItemStack second) {
        return !isEmpty(second) && matches(first, second.getItem(), second.getMetadata());
    }

    public static boolean hasDurabilityLeft(ItemStack stack) {
        return !isEmpty(stack) && stack.getItemDamage() < stack.getMaxDamage();
    }

    public static List<String> getLore(ItemStack stack) {
        List<String> lore = new ArrayList<>();
        if (isEmpty(stack) || !stack.hasTagCompound())
            return lore;

        NBTTagList list = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
        for (int i = 0; i < list.tagCount(); i++)
            lore.add(list.getStringTagAt(i));

        return lore;
    }

    public static String getEnchantments(ItemStack stack) {
        if (isEmpty(stack) || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("ench"))
            return null;

        return stack.getTagCompound().getTag("ench").toString();
    }

    public static CompressionLevel getCompressionLevel(ItemStack stack) {
        if (isEmpty(stack) || !stack.hasTagCompound())
            return CompressionLevel.NONE;

        // GrieferGames used both spellings of the tag over time
        for (String key : new String[] {"CompressionLevel", "compressionLevel"}) {
            if (stack.getTagCompound().hasKey(key))
                return CompressionLevel.fromTagValue(stack.getTagCompound().getString(key));
        }
        return CompressionLevel.NONE;
    }

    public static float getDestroySpeed(ItemStack stack, BlockPos pos) {
        return stack.getDestroySpeed(getWorld().getBlockState(pos));
    }

    // ---------------------------------------------------------------- Player inventory

    public static ItemStack getInventoryStack(int index) {
        return getPlayer().inventory.mainInventory.get(index);
    }

    public static ItemStack getHeldItem() {
        return getPlayer().getHeldItemMainhand();
    }

    public static int getHotbarSlot() {
        return getPlayer().inventory.currentItem;
    }

    public static void setHotbarSlot(int slot) {
        getPlayer().inventory.currentItem = slot;
    }

    public static boolean isInventoryFull() {
        return findInventorySlot(Helper::isEmpty) == -1;
    }

    public static int countInventory(Predicate<ItemStack> filter) {
        int count = 0;
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (filter.test(getInventoryStack(i)))
                count++;
        }
        return count;
    }

    public static int findInventorySlot(Predicate<ItemStack> filter) {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (filter.test(getInventoryStack(i)))
                return i;
        }
        return -1;
    }

    /** Inventory slot of the matching stack with the lowest compression level, -1 if there is none. */
    public static int findLowestCompressionSlot(Predicate<ItemStack> filter) {
        int bestSlot = -1;
        CompressionLevel bestLevel = null;

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack stack = getInventoryStack(i);
            if (isEmpty(stack) || !filter.test(stack))
                continue;

            CompressionLevel level = getCompressionLevel(stack);
            if (level == CompressionLevel.NONE)
                return i;

            if (bestLevel == null || level.getLevel() < bestLevel.getLevel()) {
                bestLevel = level;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    // ---------------------------------------------------------------- Containers

    public static Container getContainer() {
        return getPlayer().openContainer;
    }

    /** True if a container other than the player's own inventory is open. */
    public static boolean isContainerOpen() {
        return getContainer() != getPlayer().inventoryContainer;
    }

    public static int getWindowId() {
        return getContainer().windowId;
    }

    public static int getContainerSize() {
        return getContainer().inventorySlots.size();
    }

    /** The stack in a slot of the open container, null if the slot is empty. */
    public static ItemStack getContainerStack(int slot) {
        ItemStack stack = getContainer().getSlot(slot).getStack();
        return isEmpty(stack) ? null : stack;
    }

    /** Title of the open chest, null if no chest is open. */
    public static String getChestTitle() {
        return getContainer() instanceof ContainerChest chest ? chest.getLowerChestInventory().getName() : null;
    }

    public static boolean isChestOpen(String title) {
        return title.equals(getChestTitle());
    }

    /** Translated display name of the open chest (e.g. "Endertruhe"), null if no chest is open. */
    public static String getChestDisplayName() {
        return getContainer() instanceof ContainerChest chest ? chest.getLowerChestInventory().getDisplayName().getUnformattedText() : null;
    }

    /** Number of chest slots (without the player inventory), 0 if no chest is open. */
    public static int getChestSize() {
        return getContainer() instanceof ContainerChest chest ? chest.getLowerChestInventory().getSizeInventory() : 0;
    }

    /** Sends a click directly, bypassing the click queues. */
    public static void windowClick(Click click) {
        ClickType mode = click.mode() == Click.Mode.QUICK_MOVE ? ClickType.QUICK_MOVE : ClickType.PICKUP;
        mc().playerController.windowClick(click.windowId(), click.slot(), 0, mode, getPlayer());
    }

    /** First slot of the open container in the given range whose stack matches the filter, -1 if there is none. */
    public static int findContainerSlot(int from, int to, Predicate<ItemStack> filter) {
        for (int i = from; i <= to; i++) {
            ItemStack stack = getContainerStack(i);
            if (stack != null && filter.test(stack))
                return i;
        }
        return -1;
    }

    /** Container slot of a player inventory index, the player inventory is always the last part of a container. */
    public static int toContainerSlot(int inventoryIndex) {
        return toContainerSlot(inventoryIndex, getContainerSize() - INVENTORY_SIZE);
    }

    /**
     * Drops all stacks of the player inventory part of the open container that match the filter.
     * With {@code keepFirst} the first match (hotbar first) is kept.
     */
    public static void dropItems(Predicate<ItemStack> filter, boolean keepFirst) {
        boolean kept = !keepFirst;
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            int slot = toContainerSlot(i);
            ItemStack stack = getContainerStack(slot);
            if (stack == null || !filter.test(stack))
                continue;

            if (kept)
                dropClick(slot);
            else
                kept = true;
        }
    }

    // ---------------------------------------------------------------- Screens

    public static boolean isInventoryScreen() {
        return mc().currentScreen instanceof GuiInventory;
    }

    public static boolean isChestScreen() {
        return mc().currentScreen instanceof GuiChest;
    }

    public static boolean isCraftingScreen() {
        return mc().currentScreen instanceof GuiCrafting;
    }

    /** Screens in which queued clicks may be sent. */
    public static boolean isClickableScreen() {
        return isInventoryScreen() || isChestScreen() || isCraftingScreen();
    }

    public static void openInventory() {
        mc().displayGuiScreen(new GuiInventory(getPlayer()));
    }

    public static void closeScreen() {
        mc().displayGuiScreen(null);
        getPlayer().closeScreen();
    }

    public static void showActionBar(String message) {
        mc().ingameGUI.setOverlayMessage(message, true);
    }

    // ---------------------------------------------------------------- World

    private static RayTraceResult getBlockTrace() {
        RayTraceResult trace = getPlayer().rayTrace(REACH, 1.0F);
        return trace != null && trace.typeOfHit == Type.BLOCK ? trace : null;
    }

    public static BlockPos getBlockPosLookingAt() {
        RayTraceResult trace = getBlockTrace();
        return trace == null ? null : trace.getBlockPos();
    }

    public static Block getBlock(BlockPos pos) {
        return getWorld().getBlockState(pos).getBlock();
    }

    public static boolean isBlock(BlockPos pos, String registryName) {
        return pos != null && Block.REGISTRY.getNameForObject(getBlock(pos)).toString().equals(registryName);
    }

    public static void lookAtBlockPos(BlockPos pos) {
        Vec3d eyes = getPlayer().getPositionEyes(1.0F);
        double diffX = pos.getX() + 0.5 - eyes.x;
        double diffY = pos.getY() + 0.5 - eyes.y;
        double diffZ = pos.getZ() + 0.5 - eyes.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        getPlayer().rotationYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        getPlayer().rotationPitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));
    }

    /** The axis along which the player looks at the given block. */
    public static Axis getViewAxis(BlockPos pos) {
        Vec3d eyes = getPlayer().getPositionEyes(1.0F);
        Vec3d direction = new Vec3d(pos).add(0.5, 0.5, 0.5).subtract(eyes).normalize();
        return getDominantAxis(direction.x, direction.y, direction.z);
    }

    private static Axis getDominantAxis(double x, double y, double z) {
        if (Math.abs(x) > Math.abs(y) && Math.abs(x) > Math.abs(z))
            return Axis.X;
        return Math.abs(z) > Math.abs(y) ? Axis.Z : Axis.Y;
    }

    /** Highest block below {@code pos + 3} that is solid or liquid (used for the nature border lines). */
    public static BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
        BlockPos current = pos.up(3);
        boolean insideBlock = true;

        for (; current.getY() >= 0; current = current.down()) {
            if (insideBlock && isSolidOrLiquid(current))
                continue;

            insideBlock = false;
            if (isSolidOrLiquid(current.down()))
                break;
        }
        return current;
    }

    private static boolean isSolidOrLiquid(BlockPos pos) {
        Material material = getWorld().getChunk(pos).getBlockState(pos).getMaterial();
        return material == Material.WATER || material == Material.LAVA || material.blocksMovement() && material != Material.LEAVES;
    }

    // ---------------------------------------------------------------- Actions

    public static void swingArm() {
        getPlayer().swingArm(EnumHand.MAIN_HAND);
    }

    /** Right clicks the block the player is looking at with the held item. */
    public static void rightClickBlockLookingAt() {
        RayTraceResult trace = getBlockTrace();
        if (trace != null)
            mc().playerController.processRightClickBlock(getPlayer(), getWorld(), trace.getBlockPos(), trace.sideHit, trace.hitVec, EnumHand.MAIN_HAND);
    }

    /** Starts breaking (left clicks) the block the player is looking at. */
    public static boolean clickBlockLookingAt() {
        RayTraceResult trace = getBlockTrace();
        return trace != null && mc().playerController.clickBlock(trace.getBlockPos(), trace.sideHit);
    }

    /** Continues breaking the block the player is looking at. */
    public static boolean damageBlockLookingAt() {
        RayTraceResult trace = getBlockTrace();
        return trace != null && mc().playerController.onPlayerDamageBlock(trace.getBlockPos(), trace.sideHit);
    }

    /** Uses the held item like a right click into the air. */
    public static void rightClick() {
        mc().playerController.processRightClick(getPlayer(), getWorld(), EnumHand.MAIN_HAND);
    }

    /** Sends the "use item" packet directly, which works even while a GUI is open. */
    public static void sendUseItemPacket() {
        NetHandlerPlayClient connection = mc().getConnection();
        if (connection != null)
            connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        else
            rightClick();
    }

    /** Releases a drawn bow. Returns true if the player should switch back to the sword. */
    public static boolean releaseBow() {
        if (!getPlayer().isHandActive() || !isItem(getPlayer().getActiveItemStack(), Const.Id.BOW) || getPlayer().getItemInUseCount() < 20)
            return false;

        getPlayer().connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        return true;
    }

    public static EntityFishHook getFishHook() {
        return getPlayer().fishEntity;
    }

    // ---------------------------------------------------------------- Rendering

    public static void startQuads() {
        Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
    }

    public static void vertex(double x, double y, double z) {
        Tessellator.getInstance().getBuffer().pos(x, y, z).endVertex();
    }

    public static void draw() {
        Tessellator.getInstance().draw();
    }
}
