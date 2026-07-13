package twilightforest.datagen.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import twilightforest.init.TFBlockEntities;
import twilightforest.tags.TFBlockEntityTypeTags;

import java.util.concurrent.CompletableFuture;

public final class BlockEntityTypeTagGenerator extends FabricTagsProvider.BlockEntityTypeTagsProvider {
	public BlockEntityTypeTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.addImmovableBlockEntities(TFBlockEntityTypeTags.IMMOVABLE);
		this.addImmovableBlockEntities(TFBlockEntityTypeTags.RELOCATION_NOT_SUPPORTED);
	}

	private void addImmovableBlockEntities(TagKey<BlockEntityType<?>> tag) {
		this.builder(tag)
			.add(key(TFBlockEntities.ANTIBUILDER))
			.add(key(TFBlockEntities.BEANSTALK_GROWER))
			.add(key(TFBlockEntities.NAGA_SPAWNER))
			.add(key(TFBlockEntities.LICH_SPAWNER))
			.add(key(TFBlockEntities.MINOSHROOM_SPAWNER))
			.add(key(TFBlockEntities.HYDRA_SPAWNER))
			.add(key(TFBlockEntities.KNIGHT_PHANTOM_SPAWNER))
			.add(key(TFBlockEntities.UR_GHAST_SPAWNER))
			.add(key(TFBlockEntities.ALPHA_YETI_SPAWNER))
			.add(key(TFBlockEntities.SNOW_QUEEN_SPAWNER))
			.add(key(TFBlockEntities.FINAL_BOSS_SPAWNER));
	}

	private static ResourceKey<BlockEntityType<?>> key(BlockEntityType<?> blockEntityType) {
		return BuiltInRegistries.BLOCK_ENTITY_TYPE.getResourceKey(blockEntityType).orElseThrow();
	}
}
