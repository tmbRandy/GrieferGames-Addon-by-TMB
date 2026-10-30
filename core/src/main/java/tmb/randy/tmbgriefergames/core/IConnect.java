package tmb.randy.tmbgriefergames.core;

import net.labymod.api.reference.annotation.Referenceable;

/** Bridge from the version independent core into the version specific game code. */
@Referenceable
public interface IConnect {
    void changeSlot(int slot);
    void loadFunctions();

    // ---------------------------------------------------------------- Used by the flow blocks

    /** Title of the open chest, null if no chest is open. */
    String getChestTitle();

    /** Registry name of the item in a slot of the open container (e.g. "minecraft:stone"), null if empty. */
    String getSlotItem(int slot);

    /** Display name of the item in a slot of the open container, null if empty. */
    String getSlotItemName(int slot);

    int getSlotCount(int slot);

    boolean isInventoryFull();

    /** Number of inventory stacks of the given registry name. */
    int countInventoryStacks(String registryName);

    /** Coordinates {x, y, z} of the block the player looks at, null if none. */
    int[] getBlockLookingAt();

    void lookAtBlock(int x, int y, int z);

    void rightClickBlockLookingAt();

    void useHeldItem();

    void closeScreen();

    /** Number of slots of the open container (chest part plus player inventory). */
    int getContainerSize();

    /** Number of chest slots of the open container, 0 if no chest is open. */
    int getChestSize();

    /** GrieferGames compression level (0-7) of the item in a slot of the open container. */
    int getSlotCompressionLevel(int slot);

    /** Registry name of the held item, null if the hand is empty. */
    String getHeldItem();

    /** Remaining uses of the held item, -1 if it has no durability. */
    int getHeldItemDurability();

    int getHotbarSlot();

    /** Registry name of the block at the position. */
    String getBlockAt(int x, int y, int z);

    /** {x, y, z} of the player. */
    double[] getPlayerPosition();

    void setRotation(float yaw, float pitch);

    /** Holds or releases a movement/interaction key: sneak, jump, sprint, forward, back, left, right, attack, use. */
    void setKeyPressed(String key, boolean pressed);

    void leftClickBlockLookingAt();

    /** Continues breaking the block the player looks at, returns true while it is being broken. */
    boolean breakBlockLookingAt();

    /** Releases a drawn bow (or stops using the held item). */
    void releaseUseItem();

    void swingArm();

    void openInventory();

    /** "inventory", "chest", "crafting", "other" or "none". */
    String getScreenType();

    /** True while the own fishing hook has a fish on the hook. */
    boolean isFishBiting();
}
