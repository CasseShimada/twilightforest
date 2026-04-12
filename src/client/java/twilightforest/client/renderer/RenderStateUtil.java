package twilightforest.client.renderer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class RenderStateUtil {
	public static final int FULL_BRIGHT = 15728880;

	private RenderStateUtil() {
	}

	public static void populateMovingBlockRenderState(MovingBlockRenderState renderState, BlockState blockState, @Nullable ClientLevel level, BlockPos blockPos, @Nullable BlockPos randomSeedPos) {
		renderState.blockState = blockState;
		renderState.blockPos = blockPos;
		renderState.randomSeedPos = randomSeedPos != null ? randomSeedPos : blockPos;

		if (level != null) {
			renderState.biome = level.getBiome(blockPos);
			renderState.cardinalLighting = level.cardinalLighting();
			renderState.lightEngine = level.getLightEngine();
		}
	}
}
