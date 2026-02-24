package twilightforest.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class SpecialStemLeavesBlock extends LeavesBlock {
	private static final float LEAF_PARTICLE_CHANCE = 0.01F;
	protected final Predicate<BlockState> stemPredicate;
	private final MapCodec<? extends LeavesBlock> codec;

	public SpecialStemLeavesBlock(Predicate<BlockState> stemPredicate, Properties properties) {
		super(LEAF_PARTICLE_CHANCE, properties);
		this.stemPredicate = stemPredicate;
		this.codec = simpleCodec(props -> new SpecialStemLeavesBlock(this.stemPredicate, props));
	}

	@Override
	public MapCodec<? extends LeavesBlock> codec() {
		return this.codec;
	}

	@Override
	public void tick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource randomSource) {
		serverLevel.setBlock(pos, updateDistance(state, serverLevel, pos), Block.UPDATE_ALL);
	}

	@Override
	protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
	}

	//Vanilla copy from LeavesBlock class, due to getDistanceAt being a private static method, we need to copy and slightly alter both
	protected BlockState updateDistance(BlockState state, LevelAccessor levelAccessor, BlockPos pos) {
		int i = 7;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.values()) {
			mutableBlockPos.setWithOffset(pos, direction);
			i = Math.min(i, getDistanceAt(levelAccessor.getBlockState(mutableBlockPos)) + 1);
			if (i == 1) break;
		}

		return state.setValue(DISTANCE, i);
	}

	protected int getDistanceAt(BlockState state) {
		return this.stemPredicate.test(state) ? 0 : state.getBlock() instanceof LeavesBlock ? state.getValue(DISTANCE) : 7;
	}
}
