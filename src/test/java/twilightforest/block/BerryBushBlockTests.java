package twilightforest.block;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import twilightforest.TwilightForestMod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BerryBushBlockTests {
	private static final ResourceKey<LootTable> TEST_LOOT = ResourceKey.create(
		Registries.LOOT_TABLE,
		TwilightForestMod.prefix("blocks/test_berry_bush")
	);

	@BeforeAll
	static void enableTestBlockConstruction() {
		try {
			MappedRegistry<?> registry = (MappedRegistry<?>) BuiltInRegistries.BLOCK;
			var intrusiveHolders = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
			intrusiveHolders.setAccessible(true);
			if (intrusiveHolders.get(registry) == null) {
				intrusiveHolders.set(registry, new java.util.IdentityHashMap<>());
			}
			var frozen = MappedRegistry.class.getDeclaredField("frozen");
			frozen.setAccessible(true);
			frozen.setBoolean(registry, false);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to prepare the block registry for bush tests", e);
		}
	}

	@Test
	void berryBushPreservesAgeAndSnowStateSchema() {
		BerryBushBlock bush = new BerryBushBlock(TEST_LOOT, properties("test_berry_bush"));

		assertEquals(0, bush.defaultBlockState().getValue(TFBushBlock.AGE));
		assertEquals(0, bush.defaultBlockState().getValue(SnowLoggable.SNOW_LAYERS));
		assertEquals(36, bush.getStateDefinition().getPossibleStates().size());
	}

	@Test
	void oreberryBushPreservesStateSchemaAndBlocksPathfinding() {
		OreBerryBushBlock bush = new OreBerryBushBlock(false, TEST_LOOT, properties("test_oreberry_bush"));

		assertEquals(0, bush.defaultBlockState().getValue(TFBushBlock.AGE));
		assertEquals(0, bush.defaultBlockState().getValue(SnowLoggable.SNOW_LAYERS));
		assertEquals(36, bush.getStateDefinition().getPossibleStates().size());
		assertFalse(bush.isPathfindable(bush.defaultBlockState(), PathComputationType.LAND));
	}

	private static BlockBehaviour.Properties properties(String path) {
		return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, TwilightForestMod.prefix(path)));
	}
}
