package twilightforest.mixin;

import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import twilightforest.world.components.layer.BiomeDensityRuntimeContext;
import twilightforest.world.components.layer.BiomeDensityRuntimeContextHolder;

import java.util.Objects;

@Mixin(Climate.Sampler.class)
public class ClimateSamplerMixin implements BiomeDensityRuntimeContextHolder {
	@Unique
	@Nullable
	private volatile BiomeDensityRuntimeContext twilightforest$biomeDensityRuntimeContext;

	@Override
	public BiomeDensityRuntimeContext twilightforest$getBiomeDensityRuntimeContext() {
		return Objects.requireNonNull(this.twilightforest$biomeDensityRuntimeContext, "Climate sampler has no biome density runtime context");
	}

	@Override
	public synchronized void twilightforest$setBiomeDensityRuntimeContext(BiomeDensityRuntimeContext context) {
		BiomeDensityRuntimeContext current = this.twilightforest$biomeDensityRuntimeContext;
		if (current != null && current != context) {
			throw new IllegalStateException("Climate sampler already belongs to another biome density runtime context");
		}
		this.twilightforest$biomeDensityRuntimeContext = context;
	}
}
