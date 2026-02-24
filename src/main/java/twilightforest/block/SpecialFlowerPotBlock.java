package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SpecialFlowerPotBlock extends FlowerPotBlock {
	@Nullable
	private final Supplier<FlowerPotBlock> emptyPot;

	public SpecialFlowerPotBlock(@Nullable Supplier<FlowerPotBlock> emptyPot, Supplier<? extends Block> flower, Properties properties) {
		super(flower.get(), properties);
		this.emptyPot = emptyPot;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {
		if (this.getPotted() != Blocks.AIR) {
			level.setBlock(pos, getEmptyPot().defaultBlockState(), Block.UPDATE_ALL);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			return InteractionResult.SUCCESS;
		} else {
			return super.useWithoutItem(state, level, pos, player, result);
		}
	}

	private FlowerPotBlock getEmptyPot() {
		return emptyPot == null ? (FlowerPotBlock) Blocks.FLOWER_POT : emptyPot.get();
	}
}
