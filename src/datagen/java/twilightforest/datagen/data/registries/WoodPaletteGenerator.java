package twilightforest.datagen.data.registries;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import twilightforest.TFRegistries;
import twilightforest.init.custom.WoodPalettes;
import twilightforest.util.woods.WoodPalette;

import java.util.concurrent.CompletableFuture;

public final class WoodPaletteGenerator extends FabricDynamicRegistryProvider {
	public WoodPaletteGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		HolderLookup.RegistryLookup<WoodPalette> palettes = registries.lookupOrThrow(TFRegistries.Keys.WOOD_PALETTES);
		entries.add(palettes, WoodPalettes.OAK);
		entries.add(palettes, WoodPalettes.SPRUCE);
		entries.add(palettes, WoodPalettes.BIRCH);
		entries.add(palettes, WoodPalettes.JUNGLE);
		entries.add(palettes, WoodPalettes.ACACIA);
		entries.add(palettes, WoodPalettes.DARK_OAK);
		entries.add(palettes, WoodPalettes.CRIMSON);
		entries.add(palettes, WoodPalettes.WARPED);
		entries.add(palettes, WoodPalettes.VANGROVE);
		entries.addAll(palettes);
	}

	@Override
	public String getName() {
		return "Wood Palettes";
	}
}
