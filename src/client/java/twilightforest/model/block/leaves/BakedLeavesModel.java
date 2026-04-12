package twilightforest.client.model.block.leaves;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public final class BakedLeavesModel implements BlockStateModel {
	private final BlockStateModel delegate;

	public BakedLeavesModel(BlockStateModel delegate) {
		this.delegate = delegate;
	}

	@Override
	public void collectParts(@NotNull RandomSource random, @NotNull List<BlockStateModelPart> output) {
		delegate.collectParts(random, output);
	}

	@Override
	public @NotNull Material.Baked particleMaterial() {
		return delegate.particleMaterial();
	}

	@Override
	public int materialFlags() {
		return delegate.materialFlags();
	}
}
