package twilightforest.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class TooltipBlockItem extends BlockItem {
	@FunctionalInterface
	public interface TooltipAppender {
		void append(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag);
	}

	private final TooltipAppender tooltipAppender;

	public TooltipBlockItem(Block block, Properties properties, TooltipAppender tooltipAppender) {
		super(block, properties);
		this.tooltipAppender = tooltipAppender;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		if (this.tooltipAppender != null) {
			this.tooltipAppender.append(stack, context, display, tooltip, flag);
		}
	}
}
