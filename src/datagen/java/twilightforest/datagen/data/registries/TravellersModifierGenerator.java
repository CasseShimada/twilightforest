package twilightforest.datagen.data.registries;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import twilightforest.TFRegistries;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

import java.util.concurrent.CompletableFuture;

public final class TravellersModifierGenerator extends FabricDynamicRegistryProvider {
	public TravellersModifierGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		HolderLookup.RegistryLookup<TravellersModifier> modifiers = registries.lookupOrThrow(TFRegistries.Keys.TRAVELLERS_MODIFIERS);
		entries.addAll(modifiers);
	}

	@Override
	public String getName() {
		return "Travellers Gear Modifiers";
	}
}
