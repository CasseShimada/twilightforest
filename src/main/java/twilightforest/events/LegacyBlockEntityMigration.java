package twilightforest.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import twilightforest.TwilightForestMod;
import twilightforest.block.entity.CandelabraBlockEntity;
import twilightforest.block.entity.JarBlockEntity;
import twilightforest.init.TFBlocks;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Restores block entities that were added to existing block IDs without an old-chunk migration.
 *
 * <p>A current block can legitimately have state that resembles an old block, so the migration
 * discriminator is always absence of both a live block entity and pending serialized block-entity
 * data. Chunk-load callbacks only enqueue work: changing a block while its full chunk future is
 * completing can re-enter the same future and deadlock the server.</p>
 */
public final class LegacyBlockEntityMigration {
	private static final Map<ServerLevel, Set<ChunkPos>> PENDING_CHUNKS = new IdentityHashMap<>();

	private LegacyBlockEntityMigration() {
	}

	public static void register() {
		ServerChunkEvents.CHUNK_LOAD.register(LegacyBlockEntityMigration::onChunkLoad);
		ServerTickEvents.END_LEVEL_TICK.register(LegacyBlockEntityMigration::migratePendingChunks);
	}

	private static void onChunkLoad(ServerLevel level, LevelChunk chunk, boolean generated) {
		if (!generated) {
			PENDING_CHUNKS.computeIfAbsent(level, ignored -> new HashSet<>()).add(chunk.getPos());
		}
	}

	private static void migratePendingChunks(ServerLevel level) {
		Set<ChunkPos> pending = PENDING_CHUNKS.remove(level);
		if (pending == null) {
			return;
		}

		for (ChunkPos chunkPos : pending) {
			LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z());
			if (chunk != null) {
				migrateLoadedChunk(level, chunk);
			}
		}
	}

	private static void migrateLoadedChunk(ServerLevel level, LevelChunk chunk) {
		int[] migrated = {0, 0};
		chunk.findBlocks(
			state -> state.is(TFBlocks.CANDELABRA) || state.is(TFBlocks.FIREFLY_JAR) || state.is(TFBlocks.CICADA_JAR),
			(pos, state) -> {
				if (state.is(TFBlocks.CANDELABRA)) {
					if (migrateMissingCandelabraBlockEntity(chunk, pos)) {
						migrated[0]++;
					}
				} else if (migrateMissingJarBlockEntity(chunk, pos)) {
					migrated[1]++;
				}
			}
		);

		if (migrated[0] > 0) {
			TwilightForestMod.LOGGER.info("Restored {} legacy candelabra(s) in chunk {} of {}",
				migrated[0], chunk.getPos(), level.dimension().identifier());
		}
		if (migrated[1] > 0) {
			TwilightForestMod.LOGGER.info("Restored {} legacy firefly/cicada jar(s) in chunk {} of {}",
				migrated[1], chunk.getPos(), level.dimension().identifier());
		}
	}

	static boolean migrateMissingCandelabraBlockEntity(LevelChunk chunk, BlockPos mutablePos) {
		BlockPos pos = mutablePos.immutable();
		if (hasBlockEntityData(chunk, pos)) {
			return false;
		}

		BlockEntity created = chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.IMMEDIATE);
		if (!(created instanceof CandelabraBlockEntity candelabra)) {
			TwilightForestMod.LOGGER.error("Could not create the candelabra block entity required to migrate {} in chunk {}",
				pos, chunk.getPos());
			return false;
		}

		candelabra.initializeLegacyPlainCandles();
		chunk.markUnsaved();
		return true;
	}

	static boolean migrateMissingJarBlockEntity(LevelChunk chunk, BlockPos mutablePos) {
		BlockPos pos = mutablePos.immutable();
		if (hasBlockEntityData(chunk, pos)) {
			return false;
		}

		BlockEntity created = chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.IMMEDIATE);
		if (!(created instanceof JarBlockEntity jar)) {
			TwilightForestMod.LOGGER.error("Could not create the jar block entity required to migrate {} in chunk {}",
				pos, chunk.getPos());
			return false;
		}

		// JarBlockEntity's constructor applies the block's canonical default lid (Twilight Oak for
		// firefly jars and Canopy for cicada jars). Mark it dirty so the new BE is saved immediately.
		jar.setChanged();
		chunk.markUnsaved();
		return true;
	}

	private static boolean hasBlockEntityData(LevelChunk chunk, BlockPos pos) {
		return chunk.getBlockEntities().containsKey(pos) || chunk.getBlockEntityNbt(pos) != null;
	}
}
