package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.labymod.api.event.Phase;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.config.SwordsSubConfig;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

/** Full automatic bow sword: switches between the sword (slot 1) and the bow (slot 3) and shoots. */
public class VABK extends ActiveFunction {
    private static final int SWORD_SLOT = 0;
    private static final int BOW_SLOT = 2;

    private int cooldown = 0;

    public VABK() {
        super(Functions.VABK.name());
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if (event.phase() != Phase.PRE || !isEnabled())
            return;

        int switchCooldown = config().getVABKswitchCooldown().get();
        cooldown++;

        if (cooldown >= switchCooldown + config().getVABKloadTime().get()) {
            cooldown = 0;
            if (Helper.releaseBow())
                Helper.setHotbarSlot(SWORD_SLOT);
        } else if (cooldown == switchCooldown - 2) {
            Helper.setHotbarSlot(BOW_SLOT);
        } else if (cooldown == switchCooldown && Helper.isItem(Helper.getHeldItem(), Const.Id.BOW)) {
            Helper.rightClick();
        }
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (event.state() == State.PRESS && Addon.allKeysPressedAndGuiClosed(config().getVABKhotkey().get()) && Addon.isChatGuiClosed())
            toggle();
    }

    private static SwordsSubConfig config() {
        return Addon.settings().getSwordsSubConfig();
    }
}
