package twilightforest.block.entity;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CinderFurnaceTests {
	@Test
	void ownsItsRegisteredBlockEntityTypeInsteadOfVanillaFurnaceType() {
		assertEquals(AbstractFurnaceBlockEntity.class, CinderFurnaceBlockEntity.class.getSuperclass());
	}

	@Test
	void speedAndYieldMultipliersPreserveHistoricalCinderLogCurve() {
		assertEquals(1, CinderFurnaceBlockEntity.calculateMultiplier(0, 2, 0));
		assertEquals(1, CinderFurnaceBlockEntity.calculateMultiplier(2, 2, 0));
		assertEquals(2, CinderFurnaceBlockEntity.calculateMultiplier(3, 2, 0));
		assertEquals(1, CinderFurnaceBlockEntity.calculateMultiplier(3, 2, 1));
		assertEquals(14, CinderFurnaceBlockEntity.calculateMultiplier(27, 2, 0));
		assertEquals(13, CinderFurnaceBlockEntity.calculateMultiplier(27, 2, 1));
	}

	@Test
	void maximumOutputMultiplierNeverDropsBelowVanillaYield() {
		assertEquals(1, CinderFurnaceBlockEntity.maxOutputMultiplier(0));
		assertEquals(1, CinderFurnaceBlockEntity.maxOutputMultiplier(10));
		assertEquals(2, CinderFurnaceBlockEntity.maxOutputMultiplier(11));
		assertEquals(3, CinderFurnaceBlockEntity.maxOutputMultiplier(27));
	}

	@Test
	void invalidRandomRollIsRejectedInsteadOfBiasingOutput() {
		assertThrows(IllegalArgumentException.class,
			() -> CinderFurnaceBlockEntity.calculateMultiplier(12, 10, 10));
		assertThrows(IllegalArgumentException.class,
			() -> CinderFurnaceBlockEntity.calculateMultiplier(12, 0, 0));
	}
}
