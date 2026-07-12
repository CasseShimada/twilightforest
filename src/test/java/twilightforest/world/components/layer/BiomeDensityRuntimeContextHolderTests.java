package twilightforest.world.components.layer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BiomeDensityRuntimeContextHolderTests {
	@Test
	void contextRoundTripsThroughHolder() {
		TestHolder holder = new TestHolder();
		BiomeDensityRuntimeContext context = new BiomeDensityRuntimeContext(1234L);

		BiomeDensityRuntimeContextHolder.set(holder, context);

		assertSame(context, BiomeDensityRuntimeContextHolder.get(holder));
		assertEquals(1234L, context.worldSeed());
	}

	@Test
	void unsupportedOwnerFailsFast() {
		Object unsupported = new Object();
		BiomeDensityRuntimeContext context = new BiomeDensityRuntimeContext(1234L);

		assertThrows(IllegalStateException.class, () -> BiomeDensityRuntimeContextHolder.get(unsupported));
		assertThrows(IllegalStateException.class, () -> BiomeDensityRuntimeContextHolder.set(unsupported, context));
	}

	private static final class TestHolder implements BiomeDensityRuntimeContextHolder {
		private BiomeDensityRuntimeContext context;

		@Override
		public BiomeDensityRuntimeContext twilightforest$getBiomeDensityRuntimeContext() {
			return this.context;
		}

		@Override
		public void twilightforest$setBiomeDensityRuntimeContext(BiomeDensityRuntimeContext context) {
			this.context = context;
		}
	}
}
