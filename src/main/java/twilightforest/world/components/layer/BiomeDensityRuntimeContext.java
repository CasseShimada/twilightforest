package twilightforest.world.components.layer;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;
import twilightforest.world.components.chunkgenerators.NoiseDensityRouter;
import twilightforest.world.components.chunkgenerators.TerrainDensityRouter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class BiomeDensityRuntimeContext implements DensityFunction.Visitor {
	private final long worldSeed;
	private final ConcurrentMap<BiomeDensitySource, BiomeDensitySource.Runtime> runtimes = new ConcurrentHashMap<>();

	public BiomeDensityRuntimeContext(long worldSeed) {
		this.worldSeed = worldSeed;
	}

	public long worldSeed() {
		return this.worldSeed;
	}

	public BiomeDensitySource.Runtime runtime(BiomeDensitySource source) {
		return this.runtimes.computeIfAbsent(source, key -> key.createRuntime(this.worldSeed));
	}

	public static NoiseRouter cacheForChunk(NoiseRouter router) {
		return router.mapAll(function -> {
			if (function instanceof TerrainDensityRouter terrainDensity) {
				return terrainDensity.cacheForChunk();
			}
			if (function instanceof NoiseDensityRouter noiseDensity) {
				return noiseDensity.cacheForChunk();
			}
			return function;
		});
	}

	@Override
	public DensityFunction apply(DensityFunction function) {
		if (function instanceof TerrainDensityRouter terrainDensity) {
			return terrainDensity.withRuntime(this.runtime(terrainDensity.biomeDensitySourceHolder().value()));
		}
		if (function instanceof NoiseDensityRouter noiseDensity) {
			return noiseDensity.withRuntime(this.runtime(noiseDensity.biomeDensitySourceHolder().value()));
		}
		return function;
	}
}
