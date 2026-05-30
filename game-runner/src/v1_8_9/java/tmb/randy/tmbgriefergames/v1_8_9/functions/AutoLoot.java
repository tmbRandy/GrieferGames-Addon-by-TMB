package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;
import tmb.randy.tmbgriefergames.core.functions.AutoLootMaster;

public class AutoLoot extends AutoLootMaster {

    @Override
    protected String getPlayerRank(String name) {
        if (Minecraft.getMinecraft().getNetHandler() == null)
            return "";

        NetworkPlayerInfo info = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(name);

        if (info == null)
            return "";

        IChatComponent component = info.getDisplayName();
        if (component != null) {
            String[] parts = component.getUnformattedText().split("┃");
            if (parts.length > 1) {
                return parts[0].trim();
            }
        }
        return "";
    }
}