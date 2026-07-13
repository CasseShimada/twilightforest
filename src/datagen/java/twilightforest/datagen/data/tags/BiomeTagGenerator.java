package twilightforest.datagen.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import twilightforest.init.TFBiomes;
import twilightforest.tags.TFBiomeTags;

import java.util.concurrent.CompletableFuture;

public final class BiomeTagGenerator extends FabricTagsProvider<Biome> {
	public BiomeTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, Registries.BIOME, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.builder(TFBiomeTags.IS_TWILIGHT)
			.add(TFBiomes.CLEARING)
			.add(TFBiomes.DENSE_FOREST)
			.add(TFBiomes.DENSE_MUSHROOM_FOREST)
			.add(TFBiomes.FIREFLY_FOREST)
			.add(TFBiomes.FOREST)
			.add(TFBiomes.MUSHROOM_FOREST)
			.add(TFBiomes.OAK_SAVANNAH)
			.add(TFBiomes.SPOOKY_FOREST)
			.add(TFBiomes.ENCHANTED_FOREST)
			.add(TFBiomes.LAKE)
			.add(TFBiomes.STREAM)
			.add(TFBiomes.UNDERGROUND)
			.add(TFBiomes.SWAMP)
			.add(TFBiomes.FIRE_SWAMP)
			.add(TFBiomes.DARK_FOREST)
			.add(TFBiomes.DARK_FOREST_CENTER)
			.add(TFBiomes.SNOWY_FOREST)
			.add(TFBiomes.GLACIER)
			.add(TFBiomes.HIGHLANDS)
			.add(TFBiomes.THORNLANDS)
			.add(TFBiomes.FINAL_PLATEAU);
		this.builder(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)
			.add(TFBiomes.SNOWY_FOREST)
			.add(TFBiomes.GLACIER);
		this.builder(BiomeTags.SPAWNS_SNOW_FOXES)
			.add(TFBiomes.SNOWY_FOREST)
			.add(TFBiomes.GLACIER);
		this.builder(BiomeTags.SPAWNS_WARM_VARIANT_FROGS)
			.add(TFBiomes.OAK_SAVANNAH)
			.add(TFBiomes.FIRE_SWAMP);
		this.builder(BiomeTags.SPAWNS_WHITE_RABBITS)
			.add(TFBiomes.SNOWY_FOREST)
			.add(TFBiomes.GLACIER);
		this.builder(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS).addTag(TFBiomeTags.IS_TWILIGHT);
		this.builder(BiomeTags.WITHOUT_ZOMBIE_SIEGES).addTag(TFBiomeTags.IS_TWILIGHT);
	}
}
