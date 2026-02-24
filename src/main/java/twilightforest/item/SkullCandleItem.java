package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.text.WordUtils;
import twilightforest.block.AbstractSkullCandleBlock;

import java.util.function.Consumer;

public class SkullCandleItem extends StandingAndWallBlockItem {

	public SkullCandleItem(AbstractSkullCandleBlock floor, AbstractSkullCandleBlock wall, Properties properties) {
		super(floor, wall, Direction.DOWN, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
		if (data != null) {
			CompoundTag tag = data.copyTagWithoutId();
			if (tag.contains("CandleColor") && tag.contains("CandleAmount")) {
				int candleAmount = tag.getIntOr("CandleAmount", 0);
				int candleColor = tag.getIntOr("CandleColor", 0);
				tooltip.accept(
					Component.translatable(candleAmount > 1 ?
								"item.twilightforest.skull_candle.desc.multiple" :
								"item.twilightforest.skull_candle.desc",
							String.valueOf(candleAmount),
							WordUtils.capitalize(AbstractSkullCandleBlock.CandleColors.colorFromInt(candleColor).getSerializedName()
								.replace("\"", "").replace("_", " ")))
						.withStyle(ChatFormatting.GRAY));
			}
		}
	}

	@Override
	public Component getName(ItemStack stack) {
		ResolvableProfile resolvableprofile = stack.get(DataComponents.PROFILE);
		return resolvableprofile != null && resolvableprofile.name().isPresent()
			? Component.translatable(this.getDescriptionId() + ".named", resolvableprofile.name().get())
			: super.getName(stack);
	}
}
