package twilightforest.datagen.data.tags;

import net.minecraft.core.HolderLookup;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import twilightforest.tags.TFBlockTags;
import twilightforest.tags.TFItemTags;

import java.util.concurrent.CompletableFuture;

public final class ItemTagGenerator extends FabricTagsProvider.ItemTagsProvider {
	public ItemTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries, BlockTagGenerator blockTags) {
		super(output, registries, blockTags);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.copy(TFBlockTags.TWILIGHT_OAK_LOGS, TFItemTags.TWILIGHT_OAK_LOGS);
		this.copy(TFBlockTags.CANOPY_LOGS, TFItemTags.CANOPY_LOGS);
		this.copy(TFBlockTags.MANGROVE_LOGS, TFItemTags.MANGROVE_LOGS);
		this.copy(TFBlockTags.DARKWOOD_LOGS, TFItemTags.DARKWOOD_LOGS);
		this.copy(TFBlockTags.TIME_LOGS, TFItemTags.TIME_LOGS);
		this.copy(TFBlockTags.TRANSFORMATION_LOGS, TFItemTags.TRANSFORMATION_LOGS);
		this.copy(TFBlockTags.MINING_LOGS, TFItemTags.MINING_LOGS);
		this.copy(TFBlockTags.SORTING_LOGS, TFItemTags.SORTING_LOGS);
		this.copy(TFBlockTags.TF_LOGS, TFItemTags.TWILIGHT_LOGS);
		this.copy(TFBlockTags.TOWERWOOD, TFItemTags.TOWERWOOD);
		this.copy(TFBlockTags.BANISTERS, TFItemTags.BANISTERS);
		this.copy(TFBlockTags.DRYING_RACKS, TFItemTags.DRYING_RACKS);
	}
}
