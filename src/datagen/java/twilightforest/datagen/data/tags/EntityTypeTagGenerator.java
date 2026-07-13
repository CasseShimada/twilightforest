package twilightforest.datagen.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
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
		this.builder(EntityTypeTags.ARROWS)
			.add(key(TFEntities.ICE_ARROW))
			.add(key(TFEntities.SEEKER_ARROW));
		this.builder(EntityTypeTags.IMPACT_PROJECTILES)
			.add(key(TFEntities.NATURE_BOLT))
			.add(key(TFEntities.LICH_BOLT))
			.add(key(TFEntities.WAND_BOLT))
			.add(key(TFEntities.LICH_BOMB))
			.add(key(TFEntities.MOONWORM_SHOT))
			.add(key(TFEntities.SLIME_BLOB))
			.add(key(TFEntities.THROWN_WEP))
			.add(key(TFEntities.THROWN_ICE))
			.add(key(TFEntities.FALLING_ICE))
			.add(key(TFEntities.ICE_SNOWBALL))
			.add(key(TFEntities.CHAIN_BLOCK));
		this.builder(EntityTypeTags.REDIRECTABLE_PROJECTILE)
			.add(key(TFEntities.HYDRA_MORTAR))
			.add(key(TFEntities.LICH_BOLT));
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
		this.builder(TFEntityTypeTags.LIFEDRAIN_DROPS_NO_FLESH)
			.addOptionalTag(EntityTypeTags.SKELETONS)
			.addOptionalTag(EntityTypeTags.FROG_FOOD)
			.add(key(EntityTypes.BLAZE))
			.add(key(EntityTypes.BREEZE))
			.add(key(EntityTypes.IRON_GOLEM))
			.add(key(EntityTypes.PHANTOM))
			.add(key(EntityTypes.SHULKER))
			.add(key(EntityTypes.SKELETON_HORSE))
			.add(key(EntityTypes.SNOW_GOLEM))
			.add(key(EntityTypes.VEX))
			.add(key(EntityTypes.WITHER))
			.add(key(TFEntities.CARMINITE_GOLEM))
			.add(key(TFEntities.DEATH_TOME))
			.add(key(TFEntities.ICE_CRYSTAL))
			.add(key(TFEntities.KNIGHT_PHANTOM))
			.add(key(TFEntities.LICH))
			.add(key(TFEntities.MOSQUITO_SWARM))
			.add(key(TFEntities.SNOW_GUARDIAN))
			.add(key(TFEntities.STABLE_ICE_CORE))
			.add(key(TFEntities.UNSTABLE_ICE_CORE))
			.add(key(TFEntities.WRAITH));
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
