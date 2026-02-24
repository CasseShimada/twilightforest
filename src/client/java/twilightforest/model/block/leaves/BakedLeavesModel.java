package twilightforest.client.model.block.leaves;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public final class BakedLeavesModel implements BlockStateModel {
	private final BlockStateModel delegate;

	public BakedLeavesModel(BlockStateModel delegate) {
		this.delegate = delegate;
	}

	@Override
	public void collectParts(@NotNull RandomSource random, @NotNull List<BlockModelPart> output) {
		delegate.collectParts(random, output);
	}

	@Override
	public @NotNull TextureAtlasSprite particleIcon() {
		return delegate.particleIcon();
	}
}
