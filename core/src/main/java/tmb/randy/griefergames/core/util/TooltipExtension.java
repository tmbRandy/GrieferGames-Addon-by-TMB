package tmb.randy.griefergames.core.util;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import tmb.randy.griefergames.core.Addon;
import java.util.Objects;

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

        if(Addon.getSharedInstance().configuration().getTooltipConfig().getShowCompTooltip().get() && (stack.getNBTTag().contains("currentAmount") || stack.getNBTTag().contains("stackSize"))) {
            // Show com item size
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

            event.getTooltipLines().add(Component.translatable("griefergames.tooltip.compressedTooltip", Component.text(DKs), Component.text(stacks), Component.text(items)));
        } else if(Addon.getSharedInstance().configuration().getTooltipConfig().getShowAdventurerTooltip().get() && Objects.requireNonNull(stack.getNBTTag()).contains("adventure")) {
            // Show extended adventurer tool data
            int amount = stack.getNBTTag().getCompound("adventure").getInt("adventure.amount");
            int total = stack.getNBTTag().getCompound("adventure").getInt("adventure.req_amount");

            float percent = 100f / (float)total * (float)amount;

            int needed = total - amount;

            int DKs = needed / (64 * 9 * 6);
            int rest = needed - (DKs * 64 * 9 * 6);

            int stacks = rest / 64;
            int items = rest - (stacks * 64);

            float percentRounded = Math.round(percent * 10) / 10.0f;
            event.getTooltipLines().add(Component.translatable("griefergames.tooltip.adventurerTooltip", Component.text(percentRounded), Component.text(DKs), Component.text(stacks), Component.text(items)));
        }
    }
  }
}
