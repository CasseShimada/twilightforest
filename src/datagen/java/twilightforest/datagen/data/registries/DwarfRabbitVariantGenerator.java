package twilightforest.datagen.data.registries;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import twilightforest.TFRegistries;
import twilightforest.entity.passive.DwarfRabbitVariant;

import java.util.concurrent.CompletableFuture;

public final class DwarfRabbitVariantGenerator extends FabricDynamicRegistryProvider {
	public DwarfRabbitVariantGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		HolderLookup.RegistryLookup<DwarfRabbitVariant> variants = registries.lookupOrThrow(TFRegistries.Keys.DWARF_RABBIT_VARIANT);
		entries.addAll(variants);
	}

	@Override
	public String getName() {
		return "Dwarf Rabbit Variants";
	}
}
