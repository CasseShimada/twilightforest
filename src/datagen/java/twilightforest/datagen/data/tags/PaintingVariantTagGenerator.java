package twilightforest.datagen.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.decoration.painting.PaintingVariants;
import twilightforest.tags.TFPaintingVariantTags;

import java.util.concurrent.CompletableFuture;

public final class PaintingVariantTagGenerator extends FabricTagsProvider<PaintingVariant> {
	public PaintingVariantTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, Registries.PAINTING_VARIANT, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.builder(TFPaintingVariantTags.LICH_BOSS_PAINTINGS)
			.add(PaintingVariants.KEBAB)
			.add(PaintingVariants.AZTEC)
			.add(PaintingVariants.ALBAN)
			.add(PaintingVariants.AZTEC2)
			.add(PaintingVariants.BOMB)
			.add(PaintingVariants.PLANT)
			.add(PaintingVariants.WASTELAND)
			.add(PaintingVariants.POOL)
			.add(PaintingVariants.COURBET)
			.add(PaintingVariants.SEA)
			.add(PaintingVariants.SUNSET)
			.add(PaintingVariants.CREEBET)
			.add(PaintingVariants.WANDERER)
			.add(PaintingVariants.GRAHAM)
			.add(PaintingVariants.MATCH)
			.add(PaintingVariants.BUST)
			.add(PaintingVariants.STAGE)
			.add(PaintingVariants.VOID)
			.add(PaintingVariants.SKULL_AND_ROSES)
			.add(PaintingVariants.WITHER)
			.add(PaintingVariants.FIGHTERS)
			.add(PaintingVariants.POINTER)
			.add(PaintingVariants.PIGSCENE)
			.add(PaintingVariants.BURNING_SKULL)
			.add(PaintingVariants.SKELETON)
			.add(PaintingVariants.DONKEY_KONG)
			.add(PaintingVariants.BAROQUE)
			.add(PaintingVariants.MEDITATIVE)
			.add(PaintingVariants.PRAIRIE_RIDE)
			.add(PaintingVariants.BACKYARD)
			.add(PaintingVariants.BOUQUET)
			.add(PaintingVariants.CAVEBIRD)
			.add(PaintingVariants.CHANGING)
			.add(PaintingVariants.COTAN)
			.add(PaintingVariants.ENDBOSS)
			.add(PaintingVariants.FERN)
			.add(PaintingVariants.FINDING)
			.add(PaintingVariants.LOWMIST)
			.add(PaintingVariants.ORB)
			.add(PaintingVariants.OWLEMONS)
			.add(PaintingVariants.PASSAGE)
			.add(PaintingVariants.POND)
			.add(PaintingVariants.SUNFLOWERS)
			.add(PaintingVariants.TIDES);

		this.builder(TFPaintingVariantTags.LICH_TOWER_PAINTINGS)
			.add(PaintingVariants.KEBAB)
			.add(PaintingVariants.AZTEC)
			.add(PaintingVariants.ALBAN)
			.add(PaintingVariants.AZTEC2)
			.add(PaintingVariants.BOMB)
			.add(PaintingVariants.PLANT)
			.add(PaintingVariants.WASTELAND)
			.add(PaintingVariants.POOL)
			.add(PaintingVariants.COURBET)
			.add(PaintingVariants.SEA)
			.add(PaintingVariants.SUNSET)
			.add(PaintingVariants.CREEBET)
			.add(PaintingVariants.WANDERER)
			.add(PaintingVariants.GRAHAM)
			.add(PaintingVariants.MATCH)
			.add(PaintingVariants.BUST)
			.add(PaintingVariants.STAGE)
			.add(PaintingVariants.VOID)
			.add(PaintingVariants.SKULL_AND_ROSES)
			.add(PaintingVariants.WITHER)
			.add(PaintingVariants.FIGHTERS)
			.add(PaintingVariants.POINTER)
			.add(PaintingVariants.PIGSCENE)
			.add(PaintingVariants.BURNING_SKULL)
			.add(PaintingVariants.SKELETON)
			.add(PaintingVariants.DONKEY_KONG)
			.add(PaintingVariants.EARTH)
			.add(PaintingVariants.WIND)
			.add(PaintingVariants.WATER)
			.add(PaintingVariants.FIRE)
			.add(PaintingVariants.BAROQUE)
			.add(PaintingVariants.MEDITATIVE)
			.add(PaintingVariants.PRAIRIE_RIDE)
			.add(PaintingVariants.UNPACKED)
			.add(PaintingVariants.BACKYARD)
			.add(PaintingVariants.BOUQUET)
			.add(PaintingVariants.CAVEBIRD)
			.add(PaintingVariants.CHANGING)
			.add(PaintingVariants.COTAN)
			.add(PaintingVariants.ENDBOSS)
			.add(PaintingVariants.FERN)
			.add(PaintingVariants.FINDING)
			.add(PaintingVariants.LOWMIST)
			.add(PaintingVariants.ORB)
			.add(PaintingVariants.OWLEMONS)
			.add(PaintingVariants.PASSAGE)
			.add(PaintingVariants.POND)
			.add(PaintingVariants.SUNFLOWERS)
			.add(PaintingVariants.TIDES);
	}
}
