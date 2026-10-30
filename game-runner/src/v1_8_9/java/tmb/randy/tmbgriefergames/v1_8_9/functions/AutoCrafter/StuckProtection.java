package tmb.randy.tmbgriefergames.v1_8_9.functions.AutoCrafter;

import net.minecraft.inventory.Container;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

/** Closes a container that stays open for too long, so the AutoCrafter can not get stuck in it. */
public class StuckProtection {
    private static final int TICK_LIMIT = 200;

    private static Container lastContainer;
    private static int counter = 0;

    public static void tick(Container container) {
        if (container != lastContainer) {
            lastContainer = container;
            counter = 0;
        } else if (++counter > TICK_LIMIT) {
            Helper.closeScreen();
            counter = 0;
        }
    }

    public static void reset() {
        counter = 0;
    }
}
