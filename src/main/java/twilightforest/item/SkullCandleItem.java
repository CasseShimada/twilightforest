package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
import twilightforest.components.item.SkullCandles;
import twilightforest.init.TFDataComponents;

import java.util.function.Consumer;

public class SkullCandleItem extends StandingAndWallBlockItem {

	public SkullCandleItem(AbstractSkullCandleBlock floor, AbstractSkullCandleBlock wall, Properties properties) {
		super(floor, wall, Direction.DOWN, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		SkullCandles candleInfo = getCandleInfo(stack);
		if (candleInfo != null) {
			tooltip.accept(
				Component.translatable(candleInfo.count() > 1 ?
							"item.twilightforest.skull_candle.desc.multiple" :
							"item.twilightforest.skull_candle.desc",
						String.valueOf(candleInfo.count()),
						WordUtils.capitalize(AbstractSkullCandleBlock.CandleColors.colorFromInt(candleInfo.color()).getSerializedName()
							.replace("\"", "").replace("_", " ")))
					.withStyle(ChatFormatting.GRAY));
		}
	}

	private static SkullCandles getCandleInfo(ItemStack stack) {
		SkullCandles component = stack.get(TFDataComponents.SKULL_CANDLES);
		if (component != null) return component;

		TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
		if (data == null) return null;

		CompoundTag tag = data.copyTagWithoutId();
		if (tag.contains("info")) {
			SkullCandles decoded = SkullCandles.CODEC.parse(NbtOps.INSTANCE, tag.get("info")).result().orElse(null);
			if (decoded != null) return decoded;
		}
		if (tag.contains("CandleColor")) {
			return new SkullCandles(tag.getIntOr("CandleColor", 0),
				Math.max(1, Math.min(4, tag.getIntOr("CandleAmount", 1))));
		}
		return null;
	}

	@Override
	public Component getName(ItemStack stack) {
		ResolvableProfile resolvableprofile = stack.get(DataComponents.PROFILE);
		return resolvableprofile != null && resolvableprofile.name().isPresent()
			? Component.translatable(this.getDescriptionId() + ".named", resolvableprofile.name().get())
			: super.getName(stack);
	}
}
