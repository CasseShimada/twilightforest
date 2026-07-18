package twilightforest.block.entity;

import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;
import twilightforest.block.CandelabraBlock;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class CandelabraBlockEntityTests {
	@Test
	void legacyStateOnlyWritesTheThreeNewCandleProperties() {
		BlockState original = mock(BlockState.class);
		BlockState first = mock(BlockState.class);
		BlockState second = mock(BlockState.class);
		BlockState migrated = mock(BlockState.class);
		when(original.setValue(CandelabraBlock.CANDLES.get(0), true)).thenReturn(first);
		when(first.setValue(CandelabraBlock.CANDLES.get(1), true)).thenReturn(second);
		when(second.setValue(CandelabraBlock.CANDLES.get(2), true)).thenReturn(migrated);

		assertSame(migrated, CandelabraBlockEntity.legacyPlainCandleState(original));

		verify(original).setValue(CandelabraBlock.CANDLES.get(0), true);
		verify(first).setValue(CandelabraBlock.CANDLES.get(1), true);
		verify(second).setValue(CandelabraBlock.CANDLES.get(2), true);
		verifyNoMoreInteractions(original, first, second, migrated);
	}
}
