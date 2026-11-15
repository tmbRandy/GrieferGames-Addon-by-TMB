package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

/** Keeps breaking the block the player looks at while the held tool still has durability. */
public class InfinityMiner extends ActiveFunction {
    private boolean startedBreaking;

    public InfinityMiner() {
        super(Functions.INFINITYMINER.name());
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if (isEnabled())
            breakBlock();
    }

    @Override
    public void keyEvent(KeyEvent event) {
        // Starting requires SHIFT, so the miner is not started by accident
        if (event.state() == State.PRESS && Addon.allKeysPressedAndGuiClosed(Addon.settings().getInfinityMiner().get()) && (isEnabled() || Key.L_SHIFT.isPressed()))
            toggle();
    }

    private void breakBlock() {
        if (!Helper.hasDurabilityLeft(Helper.getHeldItem()) || Helper.getBlockPosLookingAt() == null)
            return;

        if (!startedBreaking) {
            Helper.clickBlockLookingAt();
            Helper.swingArm();
        }

        if (Helper.damageBlockLookingAt())
            Helper.swingArm();

        startedBreaking = true;
    }
}
