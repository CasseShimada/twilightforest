package twilightforest.events;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFEntities;
import twilightforest.mixin.accessor.SpawnPlacementsInvoker;

public class RegistrationEvents {

	public static void register() {
		registerAttributes();
		registerPlacements();
		registerBlockStorage();
		LegacyBlockEntityMigration.register();
	}

	private static void registerBlockStorage() {
		ItemStorage.SIDED.registerForBlockEntity(
			(masonJar, side) -> side == Direction.UP ? masonJar.getItemHandler() : null,
			TFBlockEntities.MASON_JAR
		);
	}

	private static void registerAttributes() {
		TFEntities.forEachAttribute((type, builder) ->
			FabricDefaultAttributeRegistry.register(type, builder.get().build())
		);
	}

	@SuppressWarnings({"unchecked", "rawtypes"}) // predicates are keyed by their entity types
	private static void registerPlacements() {
		TFEntities.forEachSpawnPredicate((type, predicate) ->
			SpawnPlacementsInvoker.twilightforest$register(
				(EntityType) type,
				SpawnPlacementTypes.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(SpawnPlacements.SpawnPredicate) predicate
			)
		);
	}
}
