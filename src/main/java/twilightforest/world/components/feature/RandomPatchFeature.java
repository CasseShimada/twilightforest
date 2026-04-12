package twilightforest.world.components.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import twilightforest.world.components.feature.config.RandomPatchConfiguration;

public class RandomPatchFeature extends Feature<RandomPatchConfiguration> {
	public RandomPatchFeature(Codec<RandomPatchConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<RandomPatchConfiguration> ctx) {
		RandomPatchConfiguration config = ctx.config();
		RandomSource random = ctx.random();
		BlockPos origin = ctx.origin();
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		int spread = config.xzSpread() + 1;
		int verticalSpread = config.ySpread() + 1;
		int placed = 0;

		for (int attempt = 0; attempt < config.tries(); attempt++) {
			mutablePos.setWithOffset(origin,
				random.nextInt(spread) - random.nextInt(spread),
				random.nextInt(verticalSpread) - random.nextInt(verticalSpread),
				random.nextInt(spread) - random.nextInt(spread));

			if (config.feature().value().place(ctx.level(), ctx.chunkGenerator(), random, mutablePos)) {
				placed++;
			}
		}

		return placed > 0;
	}
}
