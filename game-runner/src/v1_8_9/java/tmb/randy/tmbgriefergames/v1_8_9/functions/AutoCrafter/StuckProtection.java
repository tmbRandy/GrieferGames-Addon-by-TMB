package tmb.randy.tmbgriefergames.v1_8_9.functions.AutoCrafter;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

public class StuckProtection {
    // To prevent the auto crafter from being stuck in a container, let's close the container if the same container is open for too many ticks
    private static final int TICK_LIMIT = 200;
    private static int counter = 0;

    private static Container lastContainer;


    public static void tick(Container container) {
        if(container != lastContainer) {
            lastContainer = container;
            counter = 0;
            return;
        }

        counter++;

        if(counter > TICK_LIMIT) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            Helper.getPlayer().closeScreen();
            counter = 0;
        }
    }

    public static void reset() {
        counter = 0;
    }
}