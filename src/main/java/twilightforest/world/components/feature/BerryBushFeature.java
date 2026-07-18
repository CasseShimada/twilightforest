package twilightforest.world.components.feature;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import twilightforest.block.SnowLoggable;
import twilightforest.tags.TFBlockTags;
import twilightforest.util.WorldUtil;
import twilightforest.world.components.feature.config.BerryBushConfig;

import java.util.List;

public class BerryBushFeature extends Feature<BerryBushConfig> {
	private static final float DEFAULT_RIPE_PROBABILITY = 0.2F;

	public BerryBushFeature(Codec<BerryBushConfig> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<BerryBushConfig> context) {
		WorldGenLevel level = context.level();
		BlockPos pos = context.origin();
		BlockState state = context.config().bushState();
		RandomSource random = context.random();
		TagKey<Block> generatesOn = context.config().placesOn();
		if (!level.getBlockState(pos.below()).is(generatesOn)) {
			return false;
		}
		boolean snowy = context.config().canBeSnowy() && level.getBiome(pos).value().shouldSnow(level, pos);
		return switch (this.chooseSize(random)) {
			case LARGE -> this.generateLargeNode(level, pos, state, generatesOn, random, snowy);
			case MEDIUM -> this.generateMediumNode(level, pos, state, generatesOn, random, snowy);
			case SMALL -> this.generateSmallNode(level, pos, state, generatesOn, random, snowy);
			case TINY -> this.setBush(level, pos, state, generatesOn, random.nextInt(4), snowy);
		};
	}

	private boolean generateLargeNode(WorldGenLevel level, BlockPos pos, BlockState state, TagKey<Block> generatesOn, RandomSource random, boolean snowy) {
		boolean placed = false;
		for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) placed |= this.setBush(level, pos.offset(dx, -2, dz), state, generatesOn, random, snowy);
		for (int dx = -2; dx <= 2; dx++) for (int dy = -1; dy <= 0; dy++) for (int dz = -2; dz <= 2; dz++) {
			if (taxicabDistance(dx, dz) < 4) placed |= this.setBush(level, pos.offset(dx, dy, dz), state, generatesOn, random, snowy);
		}
		for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) placed |= this.setBush(level, pos.offset(dx, 1, dz), state, generatesOn, random, snowy);
		return placed;
	}

	private boolean generateMediumNode(WorldGenLevel level, BlockPos pos, BlockState state, TagKey<Block> generatesOn, RandomSource random, boolean snowy) {
		boolean placed = false;
		for (int dy = -1; dy <= 2; dy++) {
			int maxDistance = Math.min(2 - dy, 2);
			for (int dx = -maxDistance; dx <= maxDistance; dx++) for (int dz = -maxDistance; dz <= maxDistance; dz++) {
				if (taxicabDistance(dx, dz) < 2 * maxDistance || random.nextBoolean()) placed |= this.setBush(level, pos.offset(dx, dy, dz), state, generatesOn, random, snowy);
			}
		}
		return placed;
	}

	private boolean generateSmallNode(WorldGenLevel level, BlockPos pos, BlockState state, TagKey<Block> generatesOn, RandomSource random, boolean snowy) {
		boolean placed = this.setBush(level, pos, state, generatesOn, random, snowy);
		for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 0; dy++) for (int dz = -1; dz <= 1; dz++) {
			if (taxicabDistance(dx, dz) == 1 && random.nextBoolean()) placed |= this.setBush(level, pos.offset(dx, dy, dz), state, generatesOn, random.nextInt(4), snowy);
		}
		return placed;
	}

	private boolean setBush(WorldGenLevel level, BlockPos pos, BlockState state, TagKey<Block> generatesOn, RandomSource random, boolean snowy) {
		return this.setBush(level, pos, state, generatesOn, random.nextFloat() < DEFAULT_RIPE_PROBABILITY ? 3 : 2, snowy);
	}

	private boolean setBush(WorldGenLevel level, BlockPos pos, BlockState state, TagKey<Block> generatesOn, int age, boolean snowy) {
		BlockState replaced = level.getBlockState(pos);
		if (!replaced.is(TFBlockTags.TF_BERRY_BUSHES_REPLACE) || replaced.is(BlockTags.FEATURES_CANNOT_REPLACE) || !replaced.getFluidState().isEmpty()) {
			return false;
		}
		if (!level.getBlockState(pos.below()).is(generatesOn) && age < 2) {
			return false;
		}
		BlockState placed = state.trySetValue(BlockStateProperties.AGE_3, age);
		if (snowy && !level.getBlockState(pos.below()).is(state.getBlock())) {
			placed = placed.trySetValue(SnowLoggable.SNOW_LAYERS, 1);
		}
		level.setBlock(pos, placed, Block.UPDATE_ALL);
		this.markAboveForPostProcessing(level, pos);
		if (snowy && age >= 2) {
			level.setBlock(pos.above(), Blocks.SNOW.defaultBlockState(), Block.UPDATE_ALL);
		}
		return true;
	}

	private BushNodeSize chooseSize(RandomSource random) {
		return WorldUtil.getRandomElementWithWeights(List.of(
			Pair.of(BushNodeSize.LARGE, 1F),
			Pair.of(BushNodeSize.MEDIUM, 2F),
			Pair.of(BushNodeSize.SMALL, 4F),
			Pair.of(BushNodeSize.TINY, 3F)
		), random);
	}

	private static int taxicabDistance(int x, int z) {
		return Math.abs(x) + Math.abs(z);
	}

	private enum BushNodeSize { TINY, SMALL, MEDIUM, LARGE }
}
