package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.block.BlockChest;
import net.minecraft.util.BlockPos;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.menu.StorageMenus.Storage;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

/** /auswurf: empties a storage chest by dropping its content, then reopens it until it stays empty. */
public class Eject extends ActiveFunction {
    private BlockPos chestPos;

    public Eject() {
        super(Functions.EJECT.name());
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        String title = Helper.getChestTitle();
        if (!isEnabled() || title == null || !title.startsWith(Storage.TITLE_PREFIX))
            return;

        if (chestPos == null) {
            BlockPos lookingAt = Helper.getBlockPosLookingAt();
            if (lookingAt != null && Helper.getBlock(lookingAt) instanceof BlockChest)
                chestPos = lookingAt;
            return;
        }

        if (!Helper.isClickQueueEmpty())
            return;

        for (int i = 0; i < Storage.STORAGE_SLOTS; i++) {
            if (Helper.getContainerStack(i) != null)
                Helper.dropClick(i);
        }

        if (Helper.isClickQueueEmpty()) {
            Helper.closeScreen();
            if (chestPos.equals(Helper.getBlockPosLookingAt()))
                Helper.rightClickBlockLookingAt();
        }
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (event.state() == State.PRESS && event.key() == Key.ESCAPE)
            stop();
    }

    @Override
    public boolean stop() {
        if (!super.stop())
            return false;

        Helper.clicks().clearAllQueues();
        chestPos = null;
        return true;
    }
}
