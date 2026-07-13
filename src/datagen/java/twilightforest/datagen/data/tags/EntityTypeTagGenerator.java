package twilightforest.datagen.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import twilightforest.init.TFEntities;
import twilightforest.tags.TFEntityTypeTags;

import java.util.concurrent.CompletableFuture;

public final class EntityTypeTagGenerator extends FabricTagsProvider.EntityTypeTagsProvider {
	public EntityTypeTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.builder(TFEntityTypeTags.BOSSES)
			.add(key(TFEntities.NAGA))
			.add(key(TFEntities.LICH))
			.add(key(TFEntities.MINOSHROOM))
			.add(key(TFEntities.HYDRA))
			.add(key(TFEntities.KNIGHT_PHANTOM))
			.add(key(TFEntities.UR_GHAST))
			.add(key(TFEntities.ALPHA_YETI))
			.add(key(TFEntities.SNOW_QUEEN))
			.add(key(TFEntities.PLATEAU_BOSS));
		this.builder(ConventionalEntityTypeTags.BOSSES).addTag(TFEntityTypeTags.BOSSES);
		this.builder(TFEntityTypeTags.DONT_KILL_BUGS).add(key(TFEntities.MOONWORM_SHOT));
		this.builder(TFEntityTypeTags.LICH_DEFLECTS_PHASE_2)
			.add(key(TFEntities.WAND_BOLT))
			.add(key(TFEntities.LICH_BOLT))
			.add(key(TFEntities.LICH_BOMB));
		this.builder(TFEntityTypeTags.RIDES_OBSTRUCT_SNATCHING)
			.add(key(TFEntities.PINCH_BEETLE))
			.add(key(TFEntities.YETI))
			.add(key(TFEntities.ALPHA_YETI));
		this.builder(TFEntityTypeTags.MULTIPLAYER_INCLUSIVE_ENTITIES)
			.add(key(TFEntities.NAGA))
			.add(key(TFEntities.LICH))
			.add(key(TFEntities.MINOSHROOM))
			.add(key(TFEntities.HYDRA))
			.add(key(TFEntities.UR_GHAST))
			.add(key(TFEntities.ALPHA_YETI))
			.add(key(TFEntities.SNOW_QUEEN))
			.add(key(TFEntities.PLATEAU_BOSS));
		this.builder(TFEntityTypeTags.SORTABLE_ENTITIES)
			.add(key(EntityTypes.CHEST_MINECART))
			.add(key(EntityTypes.HOPPER_MINECART))
			.add(key(EntityTypes.LLAMA))
			.add(key(EntityTypes.TRADER_LLAMA))
			.add(key(EntityTypes.DONKEY))
			.add(key(EntityTypes.MULE));
	}

	private static ResourceKey<EntityType<?>> key(EntityType<?> entityType) {
		return BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType).orElseThrow();
	}
}
