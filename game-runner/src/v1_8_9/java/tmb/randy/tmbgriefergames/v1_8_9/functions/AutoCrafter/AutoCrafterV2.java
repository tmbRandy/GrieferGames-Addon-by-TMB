package tmb.randy.tmbgriefergames.v1_8_9.functions.AutoCrafter;

import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.event.Phase;
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
import tmb.randy.tmbgriefergames.core.menu.RezepteMenu.MinecraftRecipes;
import tmb.randy.tmbgriefergames.core.menu.RezepteMenu.VanillaRecipe;
import tmb.randy.tmbgriefergames.v1_8_9.Helper;

/** Crafts via the /rezepte menu until the inventory is full, then compresses or drops the result. */
public class AutoCrafterV2 extends ActiveFunction {
    private enum Step {
        OPEN_RECIPES, OPEN_CRAFT_PAGE, CRAFT,
        OPEN_INVENTORY, DROP_ITEMS,
        GO_BACK, OPEN_COMPRESSION, COMPRESS, RESTART
    }

    private static final int TICKS_BETWEEN_CRAFTS = 7;
    private static final int FULL_INVENTORY_STACKS = 27;
    private static final int MAX_COMPRESSION_LEVEL = 6;

    private Step step = Step.OPEN_RECIPES;
    private ItemStack itemToCraft;
    private int compressionLevel;
    private int cooldown;

    public AutoCrafterV2() {
        super(Functions.CRAFTV2.name());
    }

    @Override
    public void keyEvent(KeyEvent event) {
        if (event.state() == State.PRESS && event.key() == Key.ESCAPE && isEnabled())
            stop();
    }

    @Override
    public boolean stop() {
        if (!super.stop())
            return false;

        itemToCraft = null;
        step = Step.OPEN_RECIPES;
        return true;
    }

    @Override
    public void tickEvent(GameTickEvent event) {
        if (event.phase() != Phase.PRE || !isEnabled())
            return;

        String title = Helper.getChestTitle();

        switch (step) {
            case OPEN_RECIPES -> {
                if (title == null)
                    RezepteMenu.requestOpen();
                else if (title.equals(RezepteMenu.TITLE))
                    Helper.shiftClick(RezepteMenu.MINECRAFT_RECIPES);
                else if (title.equals(MinecraftRecipes.TITLE))
                    step = Step.OPEN_CRAFT_PAGE;
            }
            case OPEN_CRAFT_PAGE -> {
                if (MinecraftRecipes.TITLE.equals(title))
                    openRecipeOfItem();
                else if (VanillaRecipe.TITLE.equals(title))
                    step = Step.CRAFT;
            }
            case CRAFT -> craft();
            case OPEN_INVENTORY -> {
                if (Helper.isInventoryScreen())
                    step = Step.DROP_ITEMS;
                else
                    Helper.openInventory();
            }
            case DROP_ITEMS -> {
                if (!Helper.isInventoryScreen() || !Helper.isClickQueueEmpty())
                    return;

                if (countCraftedStacks() <= 1) {
                    Helper.getPlayer().closeScreen();
                    step = Step.OPEN_RECIPES;
                } else {
                    Helper.dropItems(stack -> Helper.isSameItem(stack, itemToCraft), true);
                }
            }
            case GO_BACK -> {
                if (RezepteMenu.TITLE.equals(title))
                    step = Step.OPEN_COMPRESSION;
                else if (VanillaRecipe.TITLE.equals(title) || MinecraftRecipes.TITLE.equals(title))
                    Helper.shiftClick(RezepteMenu.BACK);
            }
            case OPEN_COMPRESSION -> openCompression(title);
            case COMPRESS -> compress();
            case RESTART -> {
                if (MinecraftRecipes.TITLE.equals(title))
                    step = Step.OPEN_CRAFT_PAGE;
                else if (RezepteMenu.TITLE.equals(title))
                    Helper.shiftClick(RezepteMenu.MINECRAFT_RECIPES);
                else if (title != null)
                    Helper.shiftClick(RezepteMenu.BACK);
            }
        }
    }

    /** Clicks the item in the recipe overview. Until the first craft the player picks the recipe manually. */
    private void openRecipeOfItem() {
        if (itemToCraft == null)
            return;

        if (Helper.isItem(itemToCraft, Const.Id.GOLD_INGOT)) {
            int slot = Helper.findContainerSlot(MinecraftRecipes.FIRST_GOLD_INGOT_SLOT, MinecraftRecipes.LAST_GOLD_INGOT_SLOT,
                stack -> Helper.isItem(stack, Const.Id.GOLD_INGOT) && Helper.getCount(stack) == 1);
            Helper.shiftClick(slot > 0 ? slot : MinecraftRecipes.GOLD_INGOT_FALLBACK);
        } else {
            int slot = Helper.findInventorySlot(stack -> Helper.isSameItem(stack, itemToCraft));
            if (slot >= 0)
                Helper.shiftClick(Helper.toContainerSlot(slot, RezepteMenu.FIRST_PLAYER_SLOT));
        }
    }

    private void craft() {
        if (itemToCraft == null && Helper.getContainerStack(VanillaRecipe.RESULT) != null)
            itemToCraft = Helper.getContainerStack(VanillaRecipe.RESULT).copy();

        if (countCraftedStacks() >= FULL_INVENTORY_STACKS) {
            switch (Addon.settings().getAutoCrafterConfig().getFinalActionV2().get()) {
                case COMP -> step = Step.GO_BACK;
                case DROP -> {
                    Helper.getPlayer().closeScreen();
                    step = Step.OPEN_INVENTORY;
                }
            }
        } else if (++cooldown >= TICKS_BETWEEN_CRAFTS) {
            cooldown = 0;
            Helper.shiftClick(RezepteMenu.CONFIRM);
        }
    }

    private void openCompression(String title) {
        if (RezepteMenu.TITLE.equals(title)) {
            Helper.shiftClick(RezepteMenu.COMPRESSION);
        } else if (CompressionRecipes.TITLE.equals(title)) {
            int slot = Helper.findInventorySlot(stack -> Helper.isSameItem(stack, itemToCraft) && Helper.getCompressionLevel(stack).getLevel() == 0);
            Helper.shiftClick(slot >= 0 ? Helper.toContainerSlot(slot, RezepteMenu.FIRST_PLAYER_SLOT) : RezepteMenu.FIRST_HOTBAR_SLOT);
        } else if (Compression.TITLE.equals(title)) {
            compressionLevel = 1;
            step = Step.COMPRESS;
        }
    }

    private void compress() {
        if (!Helper.isClickQueueEmpty() || Helper.getChestTitle() == null)
            return;

        ItemStack indicator = Helper.getContainerStack(RezepteMenu.PAGE_INDICATOR);
        int level = indicator == null ? -1 : Compression.parseLevel(indicator.getDisplayName());
        if (level < 0)
            return;

        int slot = RezepteMenu.navigate(level, compressionLevel);
        Helper.shiftClick(slot);

        if (slot == RezepteMenu.CONFIRM && ++compressionLevel > MAX_COMPRESSION_LEVEL)
            step = Step.RESTART;
    }

    private int countCraftedStacks() {
        return Helper.countInventory(stack -> Helper.isSameItem(stack, itemToCraft));
    }
}
