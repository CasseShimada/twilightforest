package twilightforest.world.components.biomesources;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import twilightforest.TFRegistries;
import twilightforest.world.components.layer.BiomeDensitySource;
import twilightforest.world.components.layer.BiomeDensityRuntimeContextHolder;

import java.util.List;
import java.util.stream.Stream;

public class TFBiomeProvider extends BiomeSource {
	public static final MapCodec<TFBiomeProvider> TF_CODEC = RegistryFileCodec.create(TFRegistries.Keys.BIOME_TERRAIN_DATA, BiomeDensitySource.CODEC, false).xmap(TFBiomeProvider::new, TFBiomeProvider::getBiomeConfig).fieldOf("terrain_data");

	private final Holder<BiomeDensitySource> biomeTerrainDataHolder;

	public TFBiomeProvider(Holder<BiomeDensitySource> biomeTerrainDataHolder) {
		super();

		this.biomeTerrainDataHolder = biomeTerrainDataHolder;
	}

	private Holder<BiomeDensitySource> getBiomeConfig() {
		return this.biomeTerrainDataHolder;
	}

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return this.biomeTerrainDataHolder.value().collectPossibleBiomes();
	}

	@Override
	protected MapCodec<? extends BiomeSource> codec() {
		return TF_CODEC;
	}

	@Override
	public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler sampler) {
		return this.runtime(sampler).getNoiseBiome(biomeX, biomeY, biomeZ);
	}

	public Holder<Biome> getMainBiome(int biomeX, int biomeZ, Climate.Sampler sampler) {
		return this.runtime(sampler).getBiomeColumnKey(biomeX, biomeZ);
	}

	@Deprecated
	public BiomeDensitySource getBiomeTerrain() {
		return this.biomeTerrainDataHolder.value();
	}

	@Override
	public void addDebugInfo(List<String> info, BlockPos cameraPos, Climate.Sampler sampler) {
		super.addDebugInfo(info, cameraPos, sampler);

		this.runtime(sampler).addDebugInfo(info, cameraPos);
	}

	private BiomeDensitySource.Runtime runtime(Climate.Sampler sampler) {
		return BiomeDensityRuntimeContextHolder.get(sampler).runtime(this.biomeTerrainDataHolder.value());
	}
}
