package twilightforest.world.components.layer;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;
import twilightforest.init.custom.BiomeLayerStack;
import twilightforest.util.WorldUtil;
import twilightforest.world.components.chunkgenerators.TerrainColumn;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerFactory;
import twilightforest.world.components.layer.vanillalegacy.area.LazyArea;
import twilightforest.world.components.layer.vanillalegacy.context.LazyAreaContext;
import twilightforest.TwilightForestMod;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BiomeDensitySource {
	public static final Codec<BiomeDensitySource> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
		TerrainColumn.CODEC.listOf().fieldOf("biome_landscape").xmap(l -> l.stream().collect(Collectors.toMap(TerrainColumn::getResourceKey, Function.identity())), m -> m.values().stream().sorted(Comparator.comparing(column -> column.getResourceKey().identifier().toString())).toList()).forGetter(o -> o.biomeList),
		BiomeLayerStack.HOLDER_CODEC.fieldOf("biome_layer_config").forGetter(BiomeDensitySource::getBiomeConfig)
	).apply(instance, instance.stable(BiomeDensitySource::new)));

	private final Map<ResourceKey<Biome>, TerrainColumn> biomeList;

	private final Holder<BiomeLayerFactory> genBiomeConfig;
	private final Supplier<LazyArea> genBiomes;

	public BiomeDensitySource(List<TerrainColumn> list, Holder<BiomeLayerFactory> biomeLayerFactory) {
		this(list.stream().collect(Collectors.toMap(TerrainColumn::getResourceKey, Function.identity())), biomeLayerFactory);
	}

	public BiomeDensitySource(Map<ResourceKey<Biome>, TerrainColumn> list, Holder<BiomeLayerFactory> biomeLayerFactory) {
		super();

		this.genBiomeConfig = biomeLayerFactory;
		this.genBiomes = Suppliers.memoize(() -> {
			long worldSeed = WorldUtil.getOverworldSeed();
			return this.genBiomeConfig.value().build(salt -> new LazyAreaContext(25, worldSeed, salt));
		});

		this.biomeList = list;
	}

	private Holder<BiomeLayerFactory> getBiomeConfig() {
		return this.genBiomeConfig;
	}

	@NotNull
	public Holder<Biome> getBiomeColumnKey(int biomeX, int biomeZ) {
		return this.biomeList.get(this.genBiomes.get().getBiome(biomeX, biomeZ)).getMainBiome();
	}

	public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ) {
		return this.biomeList.get(this.genBiomes.get().getBiome(biomeX, biomeZ)).getBiome(biomeY);
	}

	public Optional<TerrainColumn> getTerrainColumn(int biomeX, int biomeZ) {
		return this.getTerrainColumn(this.genBiomes.get().getBiome(biomeX, biomeZ));
	}

	public Optional<TerrainColumn> getTerrainColumn(ResourceKey<Biome> biome) {
		return Optional.ofNullable(this.biomeList.get(biome));
	}

	// Only used for building a cache
	public Stream<Holder<Biome>> collectPossibleBiomes() {
		return this.biomeList.values().stream().flatMap(TerrainColumn::getBiomes);
	}

	public void addDebugInfo(List<String> info, BlockPos cameraPos) {
		ResourceKey<Biome> biomeKey = this.genBiomes.get().getBiome(cameraPos.getX() >> 2, cameraPos.getZ() >> 2);
		TerrainColumn biomeColumn = this.biomeList.get(biomeKey);
		Holder<Biome> biomeAtY = biomeColumn.getBiome(cameraPos.getY() >> 2);
		info.add("BiomeDensitySource at " + cameraPos + ":");
		info.add("Twilight Biome Column:");
		biomeColumn.getBiomesDebug(info::add);
		info.add("Primary Biome: " + biomeKey.identifier());
		info.add("Biome at elevation: " + biomeAtY.unwrapKey().map(ResourceKey::identifier).map(Identifier::toString).orElse("NOT REFERENCED"));
	}

	public static final class DensityData {
		public final double depth;
		public final double scale;

		public DensityData(double depth, double scale) {
			this.depth = depth;
			this.scale = scale;
		}
	}

	// Thanks k.jpg!

	private static final double BLEND_RADIUS = 8.75;
	private static final int BLEND_RADIUS_INT = Mth.floor(BLEND_RADIUS + 1.0);
	private static final int BLOCK_XYZ_OFFSET = QuartPos.SIZE / 2;
	private static final Set<String> LOGGED_WORLDGEN_ANOMALIES = ConcurrentHashMap.newKeySet();

	private static void logWorldgenAnomalyOnce(String key, String message, Object... args) {
		if (LOGGED_WORLDGEN_ANOMALIES.add(key)) {
			TwilightForestMod.LOGGER.warn(message, args);
		}
	}

	public DensityData sampleTerrain(int blockX, int blockZ, DensityFunction.FunctionContext context) {
		double totalMappedDepth = 0.0;
		double totalContribution = 0.0;
		double totalScale = 0.0;
		double totalScaleContribution = 0.0;

		int blockXWithOffset = blockX - BLOCK_XYZ_OFFSET;
		int blockZWithOffset = blockZ - BLOCK_XYZ_OFFSET;

		int xQuartStart = (blockXWithOffset - BLEND_RADIUS_INT) >> QuartPos.BITS;
		int zQuartStart = (blockZWithOffset - BLEND_RADIUS_INT) >> QuartPos.BITS;
		int xQuartEnd = (blockXWithOffset + BLEND_RADIUS_INT) >> QuartPos.BITS;
		int zQuartEnd = (blockZWithOffset + BLEND_RADIUS_INT) >> QuartPos.BITS;
		int xCount = xQuartEnd - xQuartStart + 1;
		int zCount = zQuartEnd - zQuartStart + 1;

		double xQuartDelta = (blockXWithOffset - (xQuartStart << QuartPos.BITS)) * (1.0 / QuartPos.SIZE);
		double zQuartDelta = (blockZWithOffset - (zQuartStart << QuartPos.BITS)) * (1.0 / QuartPos.SIZE);

		for (int cz = 0, cx = 0; ; ) {
			double dX = xQuartDelta - cx;
			double dZ = zQuartDelta - cz;

			double distSq = dX * dX + dZ * dZ;

			if (distSq < BLEND_RADIUS * BLEND_RADIUS) {
				int sampleBiomeX = cx + xQuartStart;
				int sampleBiomeZ = cz + zQuartStart;
				Optional<TerrainColumn> terrainColumn = this.getTerrainColumn(sampleBiomeX, sampleBiomeZ);
				if (terrainColumn.isPresent()) {
					double falloff = BLEND_RADIUS * BLEND_RADIUS * terrainColumn.get().weight(context);
					double scaleFalloff = BLEND_RADIUS * BLEND_RADIUS * terrainColumn.get().weight(context);

					double neighborDepth = terrainColumn.get().depth(context);
					double neighborScale = terrainColumn.get().scale(context);

					falloff *= Math.exp((distSq * 2f + neighborDepth) * -0.4f);
					totalMappedDepth += neighborDepth * falloff;
					totalContribution += falloff;

					scaleFalloff *= Math.exp((distSq * 2f + neighborScale) * -0.4f);
					totalScale += neighborScale * scaleFalloff;
					totalScaleContribution += scaleFalloff;
				} else {
					ResourceKey<Biome> missingBiome = this.genBiomes.get().getBiome(sampleBiomeX, sampleBiomeZ);
					logWorldgenAnomalyOnce(
						"missing_column:" + missingBiome.identifier(),
						"TF terrain trace: missing terrain column for biome={} sampledAtQuart=({}, {}) block=({}, {})",
						missingBiome.identifier(),
						sampleBiomeX,
						sampleBiomeZ,
						blockX,
						blockZ
					);
				}
			}

			cz++;
			if (cz < zCount) continue;
			cz = 0;
			cx++;
			if (cx >= xCount) break;
		}

		if (totalContribution <= 0.0D || totalScaleContribution <= 0.0D) {
			logWorldgenAnomalyOnce(
				"zero_contribution:" + (blockX >> 4) + ":" + (blockZ >> 4),
				"TF terrain trace: zero terrain contribution at block=({}, {}) chunk=({}, {}) depthContribution={} scaleContribution={}",
				blockX,
				blockZ,
				blockX >> 4,
				blockZ >> 4,
				totalContribution,
				totalScaleContribution
			);
		}

		DensityData result = new DensityData(totalMappedDepth / totalContribution, totalScale / totalScaleContribution);
		if (!Double.isFinite(result.depth) || !Double.isFinite(result.scale)) {
			logWorldgenAnomalyOnce(
				"nonfinite_density:" + (blockX >> 4) + ":" + (blockZ >> 4),
				"TF terrain trace: non-finite terrain sample at block=({}, {}) chunk=({}, {}) depth={} scale={} depthContribution={} scaleContribution={}",
				blockX,
				blockZ,
				blockX >> 4,
				blockZ >> 4,
				result.depth,
				result.scale,
				totalContribution,
				totalScaleContribution
			);
		}
		return result;
	}
}
