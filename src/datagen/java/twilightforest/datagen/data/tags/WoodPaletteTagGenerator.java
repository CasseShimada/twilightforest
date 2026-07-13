package twilightforest.datagen.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import twilightforest.TFRegistries;
import twilightforest.init.custom.WoodPalettes;
import twilightforest.tags.TFWoodPaletteTags;
import twilightforest.util.woods.WoodPalette;

import java.util.concurrent.CompletableFuture;

public final class WoodPaletteTagGenerator extends FabricTagsProvider<WoodPalette> {
	public WoodPaletteTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, TFRegistries.Keys.WOOD_PALETTES, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.builder(TFWoodPaletteTags.WELL_SWIZZLE_MASK).add(WoodPalettes.OAK);
		this.builder(TFWoodPaletteTags.DRUID_HUT_SWIZZLE_MASK)
			.add(WoodPalettes.OAK)
			.add(WoodPalettes.SPRUCE)
			.add(WoodPalettes.BIRCH);
		this.builder(TFWoodPaletteTags.COMMON_PALETTES)
			.add(WoodPalettes.SPRUCE)
			.add(WoodPalettes.CANOPY);
		this.builder(TFWoodPaletteTags.UNCOMMON_PALETTES)
			.add(WoodPalettes.OAK)
			.add(WoodPalettes.DARKWOOD)
			.add(WoodPalettes.TWILIGHT_OAK);
		this.builder(TFWoodPaletteTags.RARE_PALETTES)
			.add(WoodPalettes.BIRCH)
			.add(WoodPalettes.JUNGLE)
			.add(WoodPalettes.MANGROVE);
		this.builder(TFWoodPaletteTags.TREASURE_PALETTES)
			.add(WoodPalettes.TIMEWOOD)
			.add(WoodPalettes.TRANSWOOD)
			.add(WoodPalettes.MINEWOOD)
			.add(WoodPalettes.SORTWOOD);
	}
}
