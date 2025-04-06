package tmb.randy.tmbgriefergames.v1_8_9.click;


public record Click(int windowID, int slot, int data, int action) {

    @Override
    public String toString() {
        return "window: " + windowID + " slot: " + slot;
    }
}

/* Action values:
 * 0: Standard Click
 * 1: Shift-Click
 * 2: Move item to/from hotbar slot (Depends on current slot and hotbar slot being full or empty)
 * 3: Duplicate item (only while in creative)
 * 4: Drop item
 * 5: Spread items (Drag behavior)
 * 6: Merge all valid items with held item
 */
