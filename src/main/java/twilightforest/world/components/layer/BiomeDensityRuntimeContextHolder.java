package twilightforest.world.components.layer;

public interface BiomeDensityRuntimeContextHolder {
	BiomeDensityRuntimeContext twilightforest$getBiomeDensityRuntimeContext();

	void twilightforest$setBiomeDensityRuntimeContext(BiomeDensityRuntimeContext context);

	static BiomeDensityRuntimeContext get(Object owner) {
		if (owner instanceof BiomeDensityRuntimeContextHolder holder) {
			return holder.twilightforest$getBiomeDensityRuntimeContext();
		}
		throw new IllegalStateException("Biome density runtime context is unavailable for " + owner.getClass().getName());
	}

	static void set(Object owner, BiomeDensityRuntimeContext context) {
		if (owner instanceof BiomeDensityRuntimeContextHolder holder) {
			holder.twilightforest$setBiomeDensityRuntimeContext(context);
			return;
		}
		throw new IllegalStateException("Biome density runtime context cannot be attached to " + owner.getClass().getName());
	}
}
