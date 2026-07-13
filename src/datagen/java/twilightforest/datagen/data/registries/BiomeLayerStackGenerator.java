package twilightforest.datagen.data.registries;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import twilightforest.TFRegistries;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerFactory;

import java.util.concurrent.CompletableFuture;

public final class BiomeLayerStackGenerator extends FabricDynamicRegistryProvider {
	public BiomeLayerStackGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		HolderLookup.RegistryLookup<BiomeLayerFactory> layers = registries.lookupOrThrow(TFRegistries.Keys.BIOME_STACK);
		entries.addAll(layers);
	}

	@Override
	public String getName() {
		return "Biome Layer Stack";
	}
}
