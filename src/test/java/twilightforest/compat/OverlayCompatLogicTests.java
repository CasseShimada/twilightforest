package twilightforest.compat;

import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import twilightforest.test.MinecraftBootstrapExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MinecraftBootstrapExtension.class)
class OverlayCompatLogicTests {
	@Test
	void normalizesDryingProgressAndRejectsInvalidOrInactiveTimers() {
		assertTrue(OverlayCompatLogic.dryingStatus(false, 10, 20).isEmpty());
		assertTrue(OverlayCompatLogic.dryingStatus(true, 10, 0).isEmpty());
		assertEquals(new OverlayCompatLogic.DryingStatus(0, 100, 100),
			OverlayCompatLogic.dryingStatus(true, -20, 100).orElseThrow());
		assertEquals(new OverlayCompatLogic.DryingStatus(100, 100, 0),
			OverlayCompatLogic.dryingStatus(true, 120, 100).orElseThrow());
	}

	@Test
	void derivesMissingQuestWoolFromTheAuthoritativeBitMask() {
		assertEquals(16, OverlayCompatLogic.missingQuestWools(0).size());
		assertTrue(OverlayCompatLogic.missingQuestWools(-1).isEmpty());
		assertEquals(15, OverlayCompatLogic.missingQuestWools(1 << DyeColor.RED.getId()).size());
	}
}
