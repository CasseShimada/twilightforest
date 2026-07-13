package twilightforest.datagen.data.registries;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import twilightforest.TFRegistries;
import twilightforest.entity.MagicPaintingVariant;

import java.util.concurrent.CompletableFuture;

public final class MagicPaintingVariantGenerator extends FabricDynamicRegistryProvider {
	public MagicPaintingVariantGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		HolderLookup.RegistryLookup<MagicPaintingVariant> variants = registries.lookupOrThrow(TFRegistries.Keys.MAGIC_PAINTINGS);
		entries.addAll(variants);
	}

	@Override
	public String getName() {
		return "Magic Painting Variants";
	}
}
