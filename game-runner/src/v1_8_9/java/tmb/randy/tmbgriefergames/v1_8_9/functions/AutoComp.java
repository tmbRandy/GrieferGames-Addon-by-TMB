package tmb.randy.tmbgriefergames.v1_8_9.functions;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.client.input.KeyEvent;
import net.labymod.api.event.client.input.KeyEvent.State;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.minecraft.item.ItemStack;
import tmb.randy.tmbgriefergames.core.Addon;
import tmb.randy.tmbgriefergames.core.Const;
import tmb.randy.tmbgriefergames.core.enums.Functions;
import tmb.randy.tmbgriefergames.core.functions.ActiveFunction;
import tmb.randy.tmbgriefergames.core.menu.RezepteMenu;
import tmb.randy.tmbgriefergames.core.menu.RezepteMenu.Compression;
import tmb.randy.tmbgriefergames.core.menu.RezepteMenu.CompressionRecipes;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

public class AutoComp extends ActiveFunction {
    private enum Step { IDLE, OPEN_COMPRESSION, COMPRESSING }

    private static final int TICKS_BETWEEN_CLICKS = 8;

    /** The compression levels that are crafted one after another, the player can switch lists with the arrow keys. */
    private static final int[][] LEVEL_LISTS = {
        {1, 2, 3, 4, 5},
        {
            1, 1, 2, 1, 1, 2, 1, 1, 2, 3, 1, 1, 2, 1, 1, 2, 1, 1, 2, 3, 4,
            1, 1, 2, 1, 1, 2, 1, 1, 2, 3, 1, 1, 2, 1, 1, 2, 1, 1, 2, 3, 4, 5,
            1, 1, 2, 1, 1, 2, 1, 1, 2, 3, 1, 1, 2, 1, 1, 2, 1, 1, 2, 3, 4,
            1, 1, 2, 1, 1, 2, 1, 1, 2, 3, 1, 1, 2, 1, 1, 2, 1, 1, 2, 3, 4, 5, 6
        }
    };

    private Step step = Step.IDLE;
    private int counter = 0;
    private int currentList = 0;
    private int currentEntry = 0;

    public AutoComp() {
        super(Functions.COMP.name());
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (event.state() != State.PRESS)
            return;

        if (!isEnabled()) {
            if (Addon.allKeysPressedAndGuiClosed(Addon.settings().getAutoCrafterConfig().getAutoCompHotkey().get()))
                start();
        } else if (Helper.isChestOpen(Compression.TITLE)) {
            if (event.key() == Key.ARROW_UP)
                changeList(1);
            else if (event.key() == Key.ARROW_DOWN)
                changeList(-1);
        }
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if (step == Step.IDLE || ++counter < TICKS_BETWEEN_CLICKS)
            return;

        counter = 0;
        String title = Helper.getChestTitle();

        if (RezepteMenu.TITLE.equals(title) && step == Step.OPEN_COMPRESSION) {
            step = Step.COMPRESSING;
            Helper.shiftClick(RezepteMenu.COMPRESSION);
        } else if (CompressionRecipes.TITLE.equals(title)) {
            Helper.shiftClick(RezepteMenu.FIRST_HOTBAR_SLOT);
        } else if (Compression.TITLE.equals(title)) {
            compress();
        }
    }

    private void compress() {
        int[] levels = LEVEL_LISTS[currentList];
        ItemStack indicator = Helper.getContainerStack(RezepteMenu.PAGE_INDICATOR);
        if (!Helper.isItem(indicator, Const.Id.SKULL))
            return;

        int level = Compression.parseLevel(indicator.getDisplayName());
        if (level < 0)
            return;

        int slot = RezepteMenu.navigate(level, levels[currentEntry]);
        Helper.shiftClick(slot);

        if (slot == RezepteMenu.CONFIRM)
            currentEntry = (currentEntry + 1) % levels.length;
    }

    @Override
    public boolean start(String[] arguments) {
        if (!super.start(arguments))
            return false;

        step = Step.OPEN_COMPRESSION;
        RezepteMenu.open();
        return true;
    }

    @Override
    public boolean stop() {
        if (!super.stop())
            return false;

        step = Step.IDLE;
        currentList = 0;
        currentEntry = 0;
        return true;
    }

    private void changeList(int direction) {
        int newList = currentList + direction;
        if (newList < 0 || newList >= LEVEL_LISTS.length)
            return;

        currentList = newList;
        currentEntry = 0;
        counter = 0;
        Addon.displayNotification(Addon.translate("autoComp.list", currentList + 1));
    }
}
