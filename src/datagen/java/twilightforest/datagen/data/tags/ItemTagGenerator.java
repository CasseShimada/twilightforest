package twilightforest.datagen.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;
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

		this.copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
		this.copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
		this.copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
		this.copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
		this.copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
		this.copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
		this.copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
		this.copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
		this.copy(BlockTags.PLANKS, ItemTags.PLANKS);
		this.copy(BlockTags.LOGS, ItemTags.LOGS);
		this.builder(ItemTags.LEAVES).add(
			key(TFBlocks.RAINBOW_OAK_LEAVES.asItem()),
			key(TFBlocks.TWILIGHT_OAK_LEAVES.asItem()),
			key(TFBlocks.CANOPY_LEAVES.asItem()),
			key(TFBlocks.MANGROVE_LEAVES.asItem()),
			key(TFBlocks.DARK_LEAVES.asItem()),
			key(TFBlocks.TIME_LEAVES.asItem()),
			key(TFBlocks.TRANSFORMATION_LEAVES.asItem()),
			key(TFBlocks.MINING_LEAVES.asItem()),
			key(TFBlocks.SORTING_LEAVES.asItem()),
			key(TFBlocks.THORN_LEAVES.asItem()),
			key(TFBlocks.BEANSTALK_LEAVES.asItem())
		);
		this.copy(BlockTags.STANDING_SIGNS, ItemTags.SIGNS);
		this.copy(BlockTags.CEILING_HANGING_SIGNS, ItemTags.HANGING_SIGNS);

		this.copy(TFBlockTags.STORAGE_BLOCKS_ARCTIC_FUR, TFItemTags.STORAGE_BLOCKS_ARCTIC_FUR);
		this.copy(TFBlockTags.STORAGE_BLOCKS_CARMINITE, TFItemTags.STORAGE_BLOCKS_CARMINITE);
		this.copy(TFBlockTags.STORAGE_BLOCKS_FIERY, TFItemTags.STORAGE_BLOCKS_FIERY);
		this.copy(TFBlockTags.STORAGE_BLOCKS_IRONWOOD, TFItemTags.STORAGE_BLOCKS_IRONWOOD);
		this.copy(TFBlockTags.STORAGE_BLOCKS_KNIGHTMETAL, TFItemTags.STORAGE_BLOCKS_KNIGHTMETAL);
		this.copy(TFBlockTags.STORAGE_BLOCKS_STEELEAF, TFItemTags.STORAGE_BLOCKS_STEELEAF);
		this.copy(ConventionalBlockTags.STORAGE_BLOCKS, ConventionalItemTags.STORAGE_BLOCKS);
		this.copy(ConventionalBlockTags.WOODEN_CHESTS, ConventionalItemTags.WOODEN_CHESTS);
		this.copy(ConventionalBlockTags.WOODEN_FENCE_GATES, ConventionalItemTags.WOODEN_FENCE_GATES);
		this.copy(ConventionalBlockTags.ROPES, ConventionalItemTags.ROPES);

		this.builder(TFItemTags.FIERY_INGOTS).add(key(TFItems.FIERY_INGOT));
		this.builder(TFItemTags.IRONWOOD_INGOTS).add(key(TFItems.IRONWOOD_INGOT));
		this.builder(TFItemTags.KNIGHTMETAL_INGOTS).add(key(TFItems.KNIGHTMETAL_INGOT));
		this.builder(TFItemTags.STEELEAF_INGOTS).add(key(TFItems.STEELEAF_INGOT));
		this.builder(TFItemTags.WROUGHT_IRON_INGOTS).add(key(TFItems.WROUGHT_IRON_BAR));
		this.builder(ConventionalItemTags.INGOTS)
			.addTag(TFItemTags.IRONWOOD_INGOTS)
			.addTag(TFItemTags.FIERY_INGOTS)
			.addTag(TFItemTags.KNIGHTMETAL_INGOTS)
			.addTag(TFItemTags.STEELEAF_INGOTS);
		this.builder(TFItemTags.CARMINITE_GEMS).add(key(TFItems.CARMINITE));
		this.builder(ConventionalItemTags.GEMS).addTag(TFItemTags.CARMINITE_GEMS);
		this.builder(TFItemTags.RAW_MATERIALS_IRONWOOD).add(key(TFItems.RAW_IRONWOOD));
		this.builder(TFItemTags.RAW_MATERIALS_KNIGHTMETAL).add(key(TFItems.ARMOR_SHARD_CLUSTER));
		this.builder(ConventionalItemTags.RAW_MATERIALS)
			.addTag(TFItemTags.RAW_MATERIALS_IRONWOOD)
			.addTag(TFItemTags.RAW_MATERIALS_KNIGHTMETAL);
	}

	private static ResourceKey<Item> key(Item item) {
		return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
	}
}
