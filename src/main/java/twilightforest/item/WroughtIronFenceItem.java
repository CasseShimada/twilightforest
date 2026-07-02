package twilightforest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.block.WroughtIronFenceBlock;
import twilightforest.init.TFSounds;

import java.util.function.Consumer;

public class WroughtIronFenceItem extends BlockItem {
	public WroughtIronFenceItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		tooltip.accept(Component.translatable("block.twilightforest.wrought_iron_fence.cap").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Block block = this.getBlock();
		if (context.isSecondaryUseActive() && level.getBlockState(pos).is(block) && !level.getBlockState(pos.above()).is(block)) {
			BlockState state = level.getBlockState(pos);
			if (state.getValue(WroughtIronFenceBlock.POST) == WroughtIronFenceBlock.PostState.CAPPED || level.getBlockState(pos.above()).isSolid()) return InteractionResult.FAIL;
			level.setBlockAndUpdate(pos, state.setValue(WroughtIronFenceBlock.POST, WroughtIronFenceBlock.PostState.CAPPED));
			level.playSound(null, pos, TFSounds.WROUGHT_IRON_FENCE_EXTENDED, SoundSource.BLOCKS, 0.35F, level.getRandom().nextFloat() * 0.1F + 0.75F);
			return InteractionResult.SUCCESS;
		}
		return super.useOn(context);
	}
}
