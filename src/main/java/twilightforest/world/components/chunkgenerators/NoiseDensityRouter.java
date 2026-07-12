package twilightforest.world.components.chunkgenerators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import twilightforest.TFRegistries;
import twilightforest.world.components.layer.BiomeDensitySource;

import java.util.Objects;

/**
 * A DensityFunction implementation that enables Biomes to influence terrain formulations, if in the noise chunk generator.
 */
public class NoiseDensityRouter implements DensityFunction.SimpleFunction {
	public static final MapCodec<NoiseDensityRouter> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		RegistryFileCodec.create(TFRegistries.Keys.BIOME_TERRAIN_DATA, BiomeDensitySource.CODEC, false).fieldOf("terrain_source").forGetter(NoiseDensityRouter::biomeDensitySourceHolder),
		Codec.doubleRange(-64, 0).fieldOf("lower_density_bound").forGetter(NoiseDensityRouter::lowerDensityBound),
		Codec.doubleRange(0, 64).fieldOf("upper_density_bound").forGetter(NoiseDensityRouter::upperDensityBound),
		Codec.doubleRange(0, 32).orElse(8.0).fieldOf("depth_scalar").forGetter(NoiseDensityRouter::depthScalar)
	).apply(inst, NoiseDensityRouter::new));
	public static final KeyDispatchDataCodec<NoiseDensityRouter> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

	private final Holder<BiomeDensitySource> biomeDensitySourceHolder;
	private final double lowerDensityBound;
	private final double upperDensityBound;
	private final double depthScalar;
	@Nullable
	private final BiomeDensitySource.Runtime runtime;

	/**
	 * @param biomeDensitySource A BiomeDensitySource containing TerrainColumns, providing per-biome scaling and depth behavior that allows biomes to distinguish their landscapes.
	 * @param lowerDensityBound  Lower clamp bound
	 * @param upperDensityBound  Upper clamp bound
	 */
	public NoiseDensityRouter(Holder<BiomeDensitySource> biomeDensitySource, double lowerDensityBound, double upperDensityBound, double depthScalar) {
		this(biomeDensitySource, lowerDensityBound, upperDensityBound, depthScalar, null);
	}

	protected NoiseDensityRouter(Holder<BiomeDensitySource> biomeDensitySource, double lowerDensityBound, double upperDensityBound, double depthScalar, @Nullable BiomeDensitySource.Runtime runtime) {
		this.biomeDensitySourceHolder = biomeDensitySource;
		this.lowerDensityBound = lowerDensityBound;
		this.upperDensityBound = upperDensityBound;
		this.depthScalar = depthScalar;
		this.runtime = runtime;
	}

	@Override
	public double compute(FunctionContext context) {
		return this.computeTerrain(context).scale;
	}

	// Our default method for obtaining column samples of the biome source.
	// This method is overridden by ChunkCachedNoiseDensityRouter, operating that subclass's cache.
	@NotNull
	public BiomeDensitySource.DensityData computeTerrain(FunctionContext context) {
		return this.runtime().sampleTerrain(context.blockX(), context.blockZ(), context);
	}

	@Override
	public double minValue() {
		return this.lowerDensityBound;
	}

	@Override
	public double maxValue() {
		return this.upperDensityBound;
	}

	@Override
	public KeyDispatchDataCodec<? extends DensityFunction> codec() {
		return KEY_CODEC;
	}

	public Holder<BiomeDensitySource> biomeDensitySourceHolder() {
		return this.biomeDensitySourceHolder;
	}

	public double lowerDensityBound() {
		return this.lowerDensityBound;
	}

	public double upperDensityBound() {
		return this.upperDensityBound;
	}

	public double depthScalar() {
		return this.depthScalar;
	}

	public NoiseDensityRouter withRuntime(BiomeDensitySource.Runtime runtime) {
		return this.recreate(runtime);
	}

	public NoiseDensityRouter cacheForChunk() {
		return new ChunkCachedNoiseDensityRouter(
			this.biomeDensitySourceHolder,
			this.lowerDensityBound,
			this.upperDensityBound,
			this.depthScalar,
			this.runtime()
		);
	}

	protected final BiomeDensitySource.Runtime runtime() {
		return Objects.requireNonNull(this.runtime, "Noise density function was used before RandomState wiring");
	}

	/**
	 * NoiseDensityRouter is at best, a configuration class with DensityFunction capabilities.
	 * ChunkCachedNoiseDensityRouter is the actual DensityFunction used in worldgen.
	 * This cache is made once per Chunk in noisegen, and caches first density value obtained from each unique X-Z coordinate, ambiguating the Y value in coordinate.
	 * Plan your biome density functions accordingly! Don't use anything that's vertically sensitive
	 */
	@Override
	public DensityFunction mapChildren(Visitor visitor) {
		return this.recreate(this.runtime);
	}

	protected NoiseDensityRouter recreate(@Nullable BiomeDensitySource.Runtime runtime) {
		return new NoiseDensityRouter(
			this.biomeDensitySourceHolder,
			this.lowerDensityBound,
			this.upperDensityBound,
			this.depthScalar,
			runtime
		);
	}

	public static class ChunkCachedNoiseDensityRouter extends NoiseDensityRouter {
		private final BiomeDensitySource.DensityData[] horizontalCache = new BiomeDensitySource.DensityData[16 * 16];

		public ChunkCachedNoiseDensityRouter(Holder<BiomeDensitySource> biomeDensitySource, double lowerDensityBound, double upperDensityBound, double depthScalar) {
			this(biomeDensitySource, lowerDensityBound, upperDensityBound, depthScalar, null);
		}

		private ChunkCachedNoiseDensityRouter(Holder<BiomeDensitySource> biomeDensitySource, double lowerDensityBound, double upperDensityBound, double depthScalar, @Nullable BiomeDensitySource.Runtime runtime) {
			super(biomeDensitySource, lowerDensityBound, upperDensityBound, depthScalar, runtime);
		}

		@Override
		protected NoiseDensityRouter recreate(@Nullable BiomeDensitySource.Runtime runtime) {
			return new ChunkCachedNoiseDensityRouter(
				this.biomeDensitySourceHolder(),
				this.lowerDensityBound(),
				this.upperDensityBound(),
				this.depthScalar(),
				runtime
			);
		}

		@NotNull
		@Override
		public BiomeDensitySource.DensityData computeTerrain(FunctionContext context) {
			int xInChunk = SectionPos.sectionRelative(context.blockX());
			int zInChunk = SectionPos.sectionRelative(context.blockZ());

			int arrayCoord = zInChunk + (xInChunk << 4);

			BiomeDensitySource.DensityData dataColumn = this.horizontalCache[arrayCoord];

			if (dataColumn == null) {
				dataColumn = this.runtime().sampleTerrain(context.blockX(), context.blockZ(), context);
				this.horizontalCache[arrayCoord] = dataColumn;
			}

			return dataColumn;
		}
	}
}
