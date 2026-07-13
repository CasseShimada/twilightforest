package twilightforest.datagen.data.registries;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import twilightforest.TFRegistries;
import twilightforest.world.components.layer.BiomeDensitySource;

import java.util.concurrent.CompletableFuture;

public final class BiomeTerrainDataGenerator extends FabricDynamicRegistryProvider {
	public BiomeTerrainDataGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		HolderLookup.RegistryLookup<BiomeDensitySource> terrainData = registries.lookupOrThrow(TFRegistries.Keys.BIOME_TERRAIN_DATA);
		entries.addAll(terrainData);
	}

	@Override
	public String getName() {
		return "Biome Terrain Data";
	}
}
