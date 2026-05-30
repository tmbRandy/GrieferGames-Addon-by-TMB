package tmb.randy.tmbgriefergames.v1_8_9.functions.AutoCrafter;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import tmb.randy.tmbgriefergames.core.functions.StuckProtectionMaster;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

public class StuckProtection {

    private static final StuckProtectionMaster master = new StuckProtectionMaster();

    public static void tick(Container container) {
        master.tick(container, () -> {
            Minecraft.getMinecraft().displayGuiScreen(null);
            Helper.getPlayer().closeScreen();
        });
    }

    public static void reset() {
        master.reset();
    }
}