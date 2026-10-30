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
}
