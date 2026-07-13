package twilightforest.datagen.data.registries;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import twilightforest.TFRegistries;
import twilightforest.world.components.spelothem.StructureSpeleothemConfig;

import java.util.concurrent.CompletableFuture;

public final class StructureSpeleothemConfigGenerator extends FabricDynamicRegistryProvider {
	public StructureSpeleothemConfigGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		HolderLookup.RegistryLookup<StructureSpeleothemConfig> configs = registries.lookupOrThrow(TFRegistries.Keys.STRUCTURE_SPELEOTHEM_SETTINGS);
		entries.addAll(configs);
	}

	@Override
	public String getName() {
		return "Structure Speleothem Configs";
	}
}
