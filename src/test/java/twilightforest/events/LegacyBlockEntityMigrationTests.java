package twilightforest.events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.junit.jupiter.api.Test;
import twilightforest.block.entity.CandelabraBlockEntity;
import twilightforest.block.entity.JarBlockEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LegacyBlockEntityMigrationTests {
	private static final BlockPos POS = new BlockPos(3, 70, 5);

	@Test
	void legitimateCurrentEmptyCandelabraIsNeverConverted() {
		LevelChunk chunk = mock(LevelChunk.class);
		BlockEntity currentEmpty = mock(CandelabraBlockEntity.class);
		when(chunk.getBlockEntities()).thenReturn(Map.of(POS, currentEmpty));

		assertFalse(LegacyBlockEntityMigration.migrateMissingCandelabraBlockEntity(chunk, POS));
		verify(chunk, never()).getBlockEntity(any(), any(LevelChunk.EntityCreationType.class));
	}

	@Test
	void pendingCurrentDataIsNeverPromotedOrOverwrittenByMigration() {
		LevelChunk chunk = mock(LevelChunk.class);
		when(chunk.getBlockEntities()).thenReturn(Map.of());
		when(chunk.getBlockEntityNbt(POS)).thenReturn(new CompoundTag());

		assertFalse(LegacyBlockEntityMigration.migrateMissingCandelabraBlockEntity(chunk, POS));
		verify(chunk, never()).getBlockEntity(any(), any(LevelChunk.EntityCreationType.class));
	}

	@Test
	void missingBlockEntityGetsExactlyOneLegacyInitialization() {
		LevelChunk chunk = mock(LevelChunk.class);
		CandelabraBlockEntity created = mock(CandelabraBlockEntity.class);
		when(chunk.getBlockEntities()).thenReturn(Map.of());
		when(chunk.getBlockEntity(POS, LevelChunk.EntityCreationType.IMMEDIATE)).thenReturn(created);

		assertTrue(LegacyBlockEntityMigration.migrateMissingCandelabraBlockEntity(chunk, POS));
		verify(created).initializeLegacyPlainCandles();
		verify(chunk).markUnsaved();
	}

	@Test
	void failedCreationDoesNotPretendMigrationSucceeded() {
		LevelChunk chunk = mock(LevelChunk.class);
		when(chunk.getBlockEntities()).thenReturn(Map.of());
		when(chunk.getBlockEntity(POS, LevelChunk.EntityCreationType.IMMEDIATE)).thenReturn(null);

		assertFalse(LegacyBlockEntityMigration.migrateMissingCandelabraBlockEntity(chunk, POS));
		verify(chunk, never()).markUnsaved();
	}

	@Test
	void legitimateCurrentJarIsNeverConverted() {
		LevelChunk chunk = mock(LevelChunk.class);
		JarBlockEntity current = mock(JarBlockEntity.class);
		when(chunk.getBlockEntities()).thenReturn(Map.of(POS, current));

		assertFalse(LegacyBlockEntityMigration.migrateMissingJarBlockEntity(chunk, POS));
		verify(chunk, never()).getBlockEntity(any(), any(LevelChunk.EntityCreationType.class));
	}

	@Test
	void pendingCurrentJarDataIsNeverPromotedOrOverwrittenByMigration() {
		LevelChunk chunk = mock(LevelChunk.class);
		when(chunk.getBlockEntities()).thenReturn(Map.of());
		when(chunk.getBlockEntityNbt(POS)).thenReturn(new CompoundTag());

		assertFalse(LegacyBlockEntityMigration.migrateMissingJarBlockEntity(chunk, POS));
		verify(chunk, never()).getBlockEntity(any(), any(LevelChunk.EntityCreationType.class));
	}

	@Test
	void missingJarBlockEntityIsMarkedDirtyAndSaved() {
		LevelChunk chunk = mock(LevelChunk.class);
		JarBlockEntity created = mock(JarBlockEntity.class);
		when(chunk.getBlockEntities()).thenReturn(Map.of());
		when(chunk.getBlockEntity(POS, LevelChunk.EntityCreationType.IMMEDIATE)).thenReturn(created);

		assertTrue(LegacyBlockEntityMigration.migrateMissingJarBlockEntity(chunk, POS));
		verify(created).setChanged();
		verify(chunk).markUnsaved();
	}

	@Test
	void failedJarCreationDoesNotPretendMigrationSucceeded() {
		LevelChunk chunk = mock(LevelChunk.class);
		when(chunk.getBlockEntities()).thenReturn(Map.of());
		when(chunk.getBlockEntity(POS, LevelChunk.EntityCreationType.IMMEDIATE)).thenReturn(null);

		assertFalse(LegacyBlockEntityMigration.migrateMissingJarBlockEntity(chunk, POS));
		verify(chunk, never()).markUnsaved();
	}
}
