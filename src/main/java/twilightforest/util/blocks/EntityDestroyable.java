package twilightforest.util.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface EntityDestroyable {
	boolean canEntityDestroy(BlockState state, BlockGetter getter, BlockPos pos, Entity entity);
}
