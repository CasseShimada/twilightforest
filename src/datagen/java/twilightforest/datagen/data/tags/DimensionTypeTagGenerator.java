package twilightforest.datagen.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.dimension.DimensionType;
import twilightforest.init.TFDimensionData;
import twilightforest.tags.TFDimensionTypeTags;

import java.util.concurrent.CompletableFuture;

public final class DimensionTypeTagGenerator extends FabricTagsProvider<DimensionType> {
	public DimensionTypeTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, Registries.DIMENSION_TYPE, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.builder(TFDimensionTypeTags.ALLOWS_MAGIC_MAP_CHARTING).add(TFDimensionData.TWILIGHT_DIM_TYPE);
	}
}
