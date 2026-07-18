package twilightforest.init;

import org.junit.jupiter.api.Test;
import twilightforest.TwilightForestMod;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TFBlockEntitiesTests {
	@Test
	void preservesChestBlockEntityIdsAndFabricPortAliases() {
		assertEquals("chest", TFBlockEntityIds.CHEST);
		assertEquals("trapped_chest", TFBlockEntityIds.TRAPPED_CHEST);
		assertEquals(TwilightForestMod.prefix("chest"), TFBlockEntityIds.ALIASES.get(TwilightForestMod.prefix("tf_chest")));
		assertEquals(TwilightForestMod.prefix("trapped_chest"), TFBlockEntityIds.ALIASES.get(TwilightForestMod.prefix("tf_trapped_chest")));
		assertEquals(TwilightForestMod.prefix("chiseled_canopy_bookshelf"),
			TFBlockEntityIds.ALIASES.get(TwilightForestMod.prefix("tome_spawner")));
	}
}
