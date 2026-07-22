package twilightforest.compat.diggus;

import net.kyrptonaught.diggusmaximus.api.ExcavationDecision;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tamaized.beanification.junit.MockitoFixer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoFixer.class)
class DiggusMaximusCompatTests {
	@Test
	void unloadedCandidatesFailClosedWithoutReadingOrLoadingTheChunk() {
		ServerLevel level = mock(ServerLevel.class);
		ServerPlayer player = mock(ServerPlayer.class);
		BlockState state = mock(BlockState.class);
		BlockPos pos = new BlockPos(512, 80, -512);
		when(level.hasChunkAt(pos)).thenReturn(false);

		assertEquals(ExcavationDecision.DENY, DiggusMaximusCompat.decide(player, level, pos, state));
		verify(level, never()).getBlockState(pos);
		verify(level, never()).getBlockEntity(pos);
	}
}
