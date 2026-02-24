package twilightforest.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DarkLeavesBlock extends LeavesBlock {
	private static final float LEAF_PARTICLE_CHANCE = 0.01F;
	public static final MapCodec<DarkLeavesBlock> CODEC = simpleCodec(DarkLeavesBlock::new);

	public DarkLeavesBlock(Properties properties) {
		super(LEAF_PARTICLE_CHANCE, properties);
	}

	@Override
	public MapCodec<? extends LeavesBlock> codec() {
		return CODEC;
	}

	@Override
	public VoxelShape getBlockSupportShape(BlockState state, BlockGetter getter, BlockPos pos) {
		return Shapes.block();
	}

	@Override
	protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
	}

	@Override
	public int getLightBlock(BlockState state) {
		return 15;
	}
}
