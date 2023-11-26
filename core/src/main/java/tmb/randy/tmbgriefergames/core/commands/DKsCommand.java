package tmb.randy.tmbgriefergames.core.commands;

import net.labymod.api.client.chat.command.Command;
import tmb.randy.tmbgriefergames.core.Addon;

public class DKsCommand extends Command {

    private final String LEVELSTYLE = "§6";
    private final String SEPARATORSTYLE = "§7";

    public DKsCommand() {
        super("dks", "dk");
    }
    @Override
    public boolean execute(String prefix, String[] arguments) {
        if(!Addon.isGG())
            return false;

        int dksLevel7;
        int dksLevel6;
        int dksLevel5;
        int dksLevel4;
        int dksLevel3;
        int dksLevel2;
        int dksLevel1;

        if(arguments.length == 1) {
            String arg = arguments[0];
            int numberOfDKs = Integer.parseInt(arg);

            if(numberOfDKs > 99999) {
                return true;
            }

            int itemsPerDK = 64*9*6;
            double restItems = numberOfDKs * itemsPerDK;
            double itemsForLevel;

            //Stufe 7
            itemsForLevel = getItemsForLevel(7);
            dksLevel7 = (int)(restItems / itemsForLevel);
            restItems -= (itemsForLevel * dksLevel7);

            //Stufe 6
            itemsForLevel = getItemsForLevel(6);
            dksLevel6 = (int)(restItems / itemsForLevel);
            restItems -= (itemsForLevel * dksLevel6);

            //Stufe 5
            itemsForLevel = getItemsForLevel(5);
            dksLevel5 = (int)(restItems / itemsForLevel);
            restItems -= (itemsForLevel * dksLevel5);

            //Stufe 4
            itemsForLevel = getItemsForLevel(4);
            dksLevel4 = (int)(restItems / itemsForLevel);
            restItems -= (itemsForLevel * dksLevel4);

            //Stufe 3
            itemsForLevel = getItemsForLevel(3);
            dksLevel3 = (int)(restItems / itemsForLevel);
            restItems -= (itemsForLevel * dksLevel3);

            //Stufe 2
            itemsForLevel = getItemsForLevel(2);
            dksLevel2 = (int)(restItems / itemsForLevel);
            restItems -= (itemsForLevel * dksLevel2);

            //Stufe 1
            itemsForLevel = getItemsForLevel(1);
            dksLevel1 = (int)(restItems / itemsForLevel);
            restItems -= (itemsForLevel * dksLevel1);

            StringBuilder builder = new StringBuilder();

            if(dksLevel7 > 0) {
                builder.append(LEVELSTYLE).append(dksLevel7).append(" * Ⅶ").append(SEPARATORSTYLE);
            }

            if(dksLevel6 > 0) {
                if(!builder.isEmpty()) {
                    builder.append(" • ");
                }
                builder.append(LEVELSTYLE).append(dksLevel6).append(" * Ⅵ").append(SEPARATORSTYLE);
            }

            if(dksLevel5 > 0) {
                if(!builder.isEmpty()) {
                    builder.append(" • ");
                }
                builder.append(LEVELSTYLE).append(dksLevel5).append(" * Ⅴ").append(SEPARATORSTYLE);
            }

            if(dksLevel4 > 0) {
                if(!builder.isEmpty()) {
                    builder.append(" • ");
                }
                builder.append(LEVELSTYLE).append(dksLevel4).append(" * Ⅳ").append(SEPARATORSTYLE);
            }

            if(dksLevel3 > 0) {
                if(!builder.isEmpty()) {
                    builder.append(" • ");
                }
                builder.append(LEVELSTYLE).append(dksLevel3).append(" * Ⅲ").append(SEPARATORSTYLE);
            }

            if(dksLevel2 > 0) {
                if(!builder.isEmpty()) {
                    builder.append(" • ");
                }
                builder.append(LEVELSTYLE).append(dksLevel2).append(" * Ⅱ").append(SEPARATORSTYLE);
            }

            if(dksLevel1 > 0) {
                if(!builder.isEmpty()) {
                    builder.append(" • ");
                }
                builder.append(LEVELSTYLE).append(dksLevel1).append(" * Ⅰ").append(SEPARATORSTYLE);
            }

            if(restItems > 0) {
                if(!builder.isEmpty()) {
                    builder.append(" • ");
                }
                builder.append(LEVELSTYLE).append(restItems).append(" Items").append(SEPARATORSTYLE);
            }

            Addon.getSharedInstance().displayNotification("§e" + numberOfDKs + " DKs" + SEPARATORSTYLE + " = " + builder);
        }

        return true;
    }

    private double getItemsForLevel(int level) {
        return Math.pow(9, level);
    }
}
