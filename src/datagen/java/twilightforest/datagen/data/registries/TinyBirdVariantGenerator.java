package twilightforest.datagen.data.registries;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import twilightforest.TFRegistries;
import twilightforest.entity.passive.TinyBirdVariant;

import java.util.concurrent.CompletableFuture;

public final class TinyBirdVariantGenerator extends FabricDynamicRegistryProvider {
	public TinyBirdVariantGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		HolderLookup.RegistryLookup<TinyBirdVariant> variants = registries.lookupOrThrow(TFRegistries.Keys.TINY_BIRD_VARIANT);
		entries.addAll(variants);
	}

	@Override
	public String getName() {
		return "Tiny Bird Variants";
	}
}
