package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.util.ToolActionUtil;

public class ArcticFurBlock extends Block {
	public ArcticFurBlock(Properties properties) {
		super(properties);
	}

	@Override
	@SuppressWarnings("deprecation")
	public float getDestroyProgress(BlockState state, Player player, BlockGetter getter, BlockPos pos) {
		//Shears dont allow extra additions to their override speed (what a dumb system) so this will do
		return ToolActionUtil.isShears(player.getMainHandItem()) ? 0.2F : super.getDestroyProgress(state, player, getter, pos);
	}

	@Override
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
		entity.causeFallDamage((float) fallDistance, 0.1F, level.damageSources().fall());
	}
}
