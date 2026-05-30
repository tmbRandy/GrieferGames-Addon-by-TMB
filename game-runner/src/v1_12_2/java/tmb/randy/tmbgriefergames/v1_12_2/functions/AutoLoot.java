package tmb.randy.tmbgriefergames.v1_12_2.functions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.text.ITextComponent;
import tmb.randy.tmbgriefergames.core.functions.AutoLootMaster;

public class AutoLoot extends AutoLootMaster {

    @Override
    protected String getPlayerRank(String name) {
        if (Minecraft.getMinecraft().getConnection() == null)
            return "";

        NetworkPlayerInfo info = Minecraft.getMinecraft().getConnection().getPlayerInfo(name);

        if (info == null)
            return "";

        ITextComponent component = info.getDisplayName();
        if (component != null) {
            String[] parts = component.getUnformattedText().split("┃");
            if (parts.length > 1) {
                return parts[0].trim();
            }
        }
        return "";
    }
}