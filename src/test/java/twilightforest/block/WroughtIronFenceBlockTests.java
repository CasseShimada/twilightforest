package twilightforest.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tamaized.beanification.junit.MockitoFixer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoFixer.class)
class WroughtIronFenceBlockTests {
	@Test
	void onlyWroughtIronStatesWithPostsSupportLeashKnots() {
		WroughtIronFenceBlock fence = mock(WroughtIronFenceBlock.class);
		BlockState state = mock(BlockState.class);
		when(state.getBlock()).thenReturn(fence);

		when(state.getValue(WroughtIronFenceBlock.POST)).thenReturn(WroughtIronFenceBlock.PostState.POST);
		assertTrue(WroughtIronFenceBlock.supportsLeashKnot(state));

		when(state.getValue(WroughtIronFenceBlock.POST)).thenReturn(WroughtIronFenceBlock.PostState.CAPPED);
		assertTrue(WroughtIronFenceBlock.supportsLeashKnot(state));

		when(state.getValue(WroughtIronFenceBlock.POST)).thenReturn(WroughtIronFenceBlock.PostState.NONE);
		assertFalse(WroughtIronFenceBlock.supportsLeashKnot(state));
	}

	@Test
	void nonWroughtIronBlocksNeverSupportLeashKnots() {
		BlockState state = mock(BlockState.class);
		Block otherBlock = mock(Block.class);
		when(state.getBlock()).thenReturn(otherBlock);

		assertFalse(WroughtIronFenceBlock.supportsLeashKnot(state));
		verifyNoInteractions(otherBlock);
	}
}
