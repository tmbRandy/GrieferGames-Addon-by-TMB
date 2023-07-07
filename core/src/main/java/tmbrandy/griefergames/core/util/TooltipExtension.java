package tmbrandy.griefergames.core.util;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import tmbrandy.griefergames.core.Addon;

public class TooltipExtension {

  private ItemStack lastRenderedTooltipItemStack;
  @Subscribe
  public void renderTooltip(ItemStackTooltipEvent event) {
    if(!Addon.isGG()) {
      return;
    }

    ItemStack stack = event.itemStack();
    this.lastRenderedTooltipItemStack = stack;

    if(stack.hasNBTTag()) {

      if(stack.getNBTTag().contains("currentAmount") || stack.getNBTTag().contains("stackSize")) {
        int currentAmount;

        if(stack.getNBTTag().contains("currentAmount")) {
          currentAmount = stack.getNBTTag().getInt("currentAmount") * stack.getSize();
        } else {
          currentAmount = stack.getNBTTag().getInt("stackSize") * stack.getSize();
        }

        int maxStackSize = stack.getMaximumStackSize();

        int DKs = currentAmount / (maxStackSize * 9 * 6);
        int rest = currentAmount - (DKs * maxStackSize * 9 * 6);

        int stacks = rest / maxStackSize;
        int items = rest - (stacks * maxStackSize);

        event.getTooltipLines().add(Component.text("§6" + DKs + " §eDKs §6" + stacks + " §eStacks §6" + items + " §eItems"));
      }
    }
  }
}
