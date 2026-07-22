package twilightforest.world;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import org.slf4j.Logger;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFDimension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Development-run-only integration probe. This source set is a Loom test mod and is never
 * included in Twilight Forest's binary or sources artifacts.
 */
public final class RuntimeCompatServerProbe implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String MARKER = "[TF-RUNTIME-PROBE]";
	private static ProbeState activeProbe;

	@Override
	public void onInitialize() {
		if (!Boolean.getBoolean("twilightforest.runtimeServerProbe")) {
			return;
		}
		ServerLifecycleEvents.SERVER_STARTED.register(RuntimeCompatServerProbe::start);
		ServerTickEvents.END_SERVER_TICK.register(RuntimeCompatServerProbe::tick);
	}

	private static void start(MinecraftServer server) {
		String scenario = System.getProperty("twilightforest.runtimeScenario", "manual");
		int pass = Integer.getInteger("twilightforest.runtimePass", 1);
		try {
			verifyExpectedMods();
			ServerLevel overworld = require(server.overworld(), "overworld");
			ServerLevel twilight = require(server.getLevel(TFDimension.DIMENSION_KEY), "Twilight Forest dimension");
			activeProbe = new ProbeState(scenario, pass, overworld, twilight);
		} catch (Throwable failure) {
			LOGGER.error("{} FAIL server scenario={} pass={}", MARKER, scenario, pass, failure);
			propagate(failure);
		}
	}

	private static void tick(MinecraftServer server) {
		ProbeState probe = activeProbe;
		if (probe == null) {
			return;
		}
		long started = System.nanoTime();
		try {
			if (probe.tick()) {
				activeProbe = null;
			}
		} catch (Throwable failure) {
			activeProbe = null;
			probe.cleanupOptionalPlayer();
			probe.releaseForcedChunks();
			LOGGER.error("{} FAIL server scenario={} pass={}", MARKER, probe.scenario, probe.pass, failure);
			propagate(failure);
		} finally {
			long stepNanos = System.nanoTime() - started;
			probe.workNanos += stepNanos;
			probe.worstStepNanos = Math.max(probe.worstStepNanos, stepNanos);
		}
	}

	private static void propagate(Throwable failure) {
		if (failure instanceof RuntimeException runtimeException) {
			throw runtimeException;
		}
		if (failure instanceof Error error) {
			throw error;
		}
		throw new IllegalStateException(failure);
	}

	private static final class ProbeState {
		private final MinecraftServer server;
		private final String scenario;
		private final int pass;
		private final long startedNanos = System.nanoTime();
		private final TeleporterCache cache;
		private final List<PortalSpec> specs;
		private final List<PortalPair> pairs = new ArrayList<>();
		private final List<Long> timings = new ArrayList<>(202);
		private final List<ForcedChunk> chunksToForce = new ArrayList<>();
		private final Set<ForcedChunk> forcedChunks = new HashSet<>();
		private int phase;
		private int index;
		private int timingIterations;
		private int cooldownTicks;
		private long workNanos;
		private long worstStepNanos;
		private Pig sourceEntity;
		private Pig targetEntity;
		private ServerPlayer diggusPlayer;
		private ServerPlayer carryOnPlayer;
		private RuntimeDiggusCompatProbe.Evidence diggusEvidence;

		private ProbeState(String scenario, int pass, ServerLevel overworld, ServerLevel twilight) {
			this.server = overworld.getServer();
			this.scenario = scenario;
			this.pass = pass;
			this.cache = TeleporterCache.get(overworld);
			this.specs = List.of(
				new PortalSpec(overworld, new BlockPos(8, 120, 8), twilight, new BlockPos(8, 120, 8), "primary"),
				new PortalSpec(overworld, new BlockPos(14, 120, 8), twilight, new BlockPos(14, 120, 8), "nearby"),
				new PortalSpec(overworld, new BlockPos(8, 126, 8), twilight, new BlockPos(8, 126, 8), "stacked"),
				new PortalSpec(overworld, new BlockPos(31, 120, 31), twilight, new BlockPos(31, 120, 31), "cross_chunk")
			);
			for (PortalSpec spec : this.specs) {
				collectPortalChunks(spec.sourceLevel, spec.sourceAnchor);
				collectPortalChunks(spec.targetLevel, spec.targetAnchor);
			}
		}

		private void collectPortalChunks(ServerLevel level, BlockPos anchor) {
			for (int chunkX = (anchor.getX() - 2) >> 4; chunkX <= (anchor.getX() + 3) >> 4; chunkX++) {
				for (int chunkZ = (anchor.getZ() - 2) >> 4; chunkZ <= (anchor.getZ() + 3) >> 4; chunkZ++) {
					ForcedChunk planned = new ForcedChunk(level, chunkX, chunkZ);
					if (!this.chunksToForce.contains(planned)) {
						this.chunksToForce.add(planned);
					}
				}
			}
		}

		private void forceOneChunk() {
			ForcedChunk forced = this.chunksToForce.get(this.index++);
			if (this.forcedChunks.add(forced)) {
				forced.level.setChunkForced(forced.chunkX, forced.chunkZ, true);
			}
			forced.level.getChunk(forced.chunkX, forced.chunkZ);
			this.cooldownTicks = 30;
			if (this.index == this.chunksToForce.size()) {
				this.index = 0;
				this.phase = 1;
			}
		}

		private void releaseForcedChunks() {
			for (ForcedChunk forced : this.forcedChunks) {
				forced.level.setChunkForced(forced.chunkX, forced.chunkZ, false);
			}
			this.forcedChunks.clear();
		}

		private boolean tick() {
			if (this.cooldownTicks > 0) {
				this.cooldownTicks--;
				return false;
			}
			switch (this.phase) {
				case 0 -> forceOneChunk();
				case 1 -> startOptionalCompatTransactions();
				case 2 -> finishOptionalCompatTransactions();
				case 3 -> prepareOnePair();
				case 4 -> validatePersistedLink();
				case 5 -> linkOnePair();
				case 6 -> invalidateNearbyPair();
				case 7 -> rebuildNearbyPair();
				case 8 -> measureRoundTripBatch();
				case 9 -> {
					finish();
					return true;
				}
				default -> throw new IllegalStateException("unknown server probe phase " + this.phase);
			}
			return false;
		}

		private void startOptionalCompatTransactions() {
			boolean diggus = FabricLoader.getInstance().isModLoaded("diggusmaximus");
			boolean carryOn = FabricLoader.getInstance().isModLoaded("carryon");
			if (!diggus && !carryOn) {
				this.phase = 3;
				return;
			}
			if (diggus) {
				this.diggusPlayer = RuntimeCompatTestPlayer.create(this.server, this.specs.getFirst().sourceLevel,
					"twilightforest-runtime-diggus-" + this.scenario + '-' + this.pass);
				this.diggusEvidence = RuntimeDiggusCompatProbe.run(this.specs.getFirst().sourceLevel, this.diggusPlayer);
			}
			if (carryOn) {
				this.carryOnPlayer = RuntimeCompatTestPlayer.create(this.server, this.specs.getFirst().sourceLevel,
					"twilightforest-runtime-carryon-" + this.scenario + '-' + this.pass);
				LOGGER.info("{} COMPAT server scenario={} mod=carryon result={}", MARKER, this.scenario,
					RuntimeCarryOnCompatProbe.run(this.specs.getFirst().sourceLevel, this.carryOnPlayer));
			}
			// Drops spawned from the synchronous Diggus transaction become visible to
			// level entity queries only after the pending-entity queues have ticked.
			this.cooldownTicks = 5;
			this.phase = 2;
		}

		private void finishOptionalCompatTransactions() {
			try {
				if (this.diggusEvidence != null) {
					LOGGER.info("{} COMPAT server scenario={} mod=diggusmaximus result={}", MARKER, this.scenario,
						this.diggusEvidence.verifyAfterEntityTick(this.specs.getFirst().sourceLevel, this.diggusPlayer));
				}
			} finally {
				cleanupOptionalPlayer();
			}
			this.phase = 3;
		}

		private void cleanupOptionalPlayer() {
			if (this.diggusPlayer != null) {
				RuntimeCompatTestPlayer.remove(this.server, this.diggusPlayer);
				this.diggusPlayer = null;
			}
			if (this.carryOnPlayer != null) {
				RuntimeCompatTestPlayer.remove(this.server, this.carryOnPlayer);
				this.carryOnPlayer = null;
			}
		}

		private void prepareOnePair() {
			PortalSpec spec = this.specs.get(this.index++);
			this.pairs.add(pair(spec.sourceLevel, spec.sourceAnchor, spec.targetLevel, spec.targetAnchor, spec.name));
			if (this.index == this.specs.size()) {
				this.index = 0;
				this.phase = 4;
			}
		}

		private void validatePersistedLink() {
			if (this.pass <= 1) {
				this.phase = 5;
				return;
			}
			assertLinked(this.cache, this.pairs.get(this.index++));
			if (this.index == this.pairs.size()) {
				this.index = 0;
				this.phase = 5;
			}
		}

		private void linkOnePair() {
			PortalPair pair = this.pairs.get(this.index++);
			check(this.cache.link(pair.source.endpoint(), pair.target.endpoint()), "could not link " + pair.name);
			assertLinked(this.cache, pair);
			if (this.index == this.pairs.size()) {
				this.phase = 6;
			}
		}

		private void invalidateNearbyPair() {
			PortalPair nearby = this.pairs.get(1);
			nearby.targetLevel.setBlockAndUpdate(nearby.target.endpoint().anchor(), Blocks.AIR.defaultBlockState());
			check(this.cache.getLinkedPortal(nearby.source.endpoint()) == null, "broken target did not atomically remove its reverse link");
			check(TFTeleporter.placeInExistingPortal(this.cache, nearby.targetLevel,
				pigAt(nearby.sourceLevel, nearby.source.endpoint().anchor()), nearby.target.endpoint().anchor()) == null,
				"a three-block portal remnant was accepted as a valid target");
			this.phase = 7;
		}

		private void rebuildNearbyPair() {
			PortalPair nearby = this.pairs.get(1);
			preparePortal(nearby.targetLevel, nearby.target.endpoint().anchor());
			TFTeleporter.PortalShape rebuilt = require(
				TFTeleporter.resolvePortalShape(nearby.targetLevel, nearby.target.endpoint().anchor()), "rebuilt nearby portal");
			TeleportTransition relinked = TFTeleporter.placeInExistingPortal(this.cache, nearby.targetLevel,
				pigAt(nearby.sourceLevel, nearby.source.endpoint().anchor()), rebuilt.endpoint().anchor());
			check(relinked != null, "rebuilt target was not relinked");
			PortalPair rebuiltPair = new PortalPair(nearby.sourceLevel, nearby.targetLevel, nearby.source, rebuilt, nearby.name);
			assertLinked(this.cache, rebuiltPair);
			this.pairs.set(1, rebuiltPair);
			PortalPair measuredPair = this.pairs.getFirst();
			this.sourceEntity = pigAt(measuredPair.sourceLevel, measuredPair.source.endpoint().anchor());
			this.targetEntity = pigAt(measuredPair.targetLevel, measuredPair.target.endpoint().anchor());
			this.phase = 8;
		}

		private void measureRoundTripBatch() {
			PortalPair pair = this.pairs.getFirst();
			for (int batch = 0; batch < 10 && this.timingIterations < 101; batch++, this.timingIterations++) {
				long start = System.nanoTime();
				TeleportTransition forward = TFTeleporter.placeInExistingPortal(
					this.cache, pair.targetLevel, this.sourceEntity, pair.target.endpoint().anchor());
				this.timings.add(System.nanoTime() - start);
				checkTransition(forward, pair.target.endpoint().anchor(), "forward");

				start = System.nanoTime();
				TeleportTransition reverse = TFTeleporter.placeInExistingPortal(
					this.cache, pair.sourceLevel, this.targetEntity, pair.source.endpoint().anchor());
				this.timings.add(System.nanoTime() - start);
				checkTransition(reverse, pair.source.endpoint().anchor(), "reverse");
			}
			if (this.timingIterations == 101) {
				this.phase = 9;
			}
		}

		private void finish() {
			this.timings.sort(Long::compareTo);
			long medianMicros = percentile(this.timings, 50) / 1_000L;
			long p95Micros = percentile(this.timings, 95) / 1_000L;
			long worstMicros = this.timings.getLast() / 1_000L;
			check(worstMicros < 3_500_000L, "exact cached portal lookup exceeded 3.5 seconds");
			long elapsedMillis = (System.nanoTime() - this.startedNanos) / 1_000_000L;
			long workMillis = this.workNanos / 1_000_000L;
			long worstStepMillis = this.worstStepNanos / 1_000_000L;
			releaseForcedChunks();
			LOGGER.info("{} PASS server scenario={} pass={} pairs={} median_us={} p95_us={} worst_us={} work_ms={} worst_step_ms={} elapsed_ms={}",
				MARKER, this.scenario, this.pass, this.pairs.size(), medianMicros, p95Micros, worstMicros, workMillis, worstStepMillis, elapsedMillis);
		}
	}

	private static PortalPair pair(ServerLevel sourceLevel, BlockPos sourceAnchor, ServerLevel targetLevel, BlockPos targetAnchor, String name) {
		preparePortal(sourceLevel, sourceAnchor);
		preparePortal(targetLevel, targetAnchor);
		TFTeleporter.PortalShape source = require(TFTeleporter.resolvePortalShape(sourceLevel, sourceAnchor), name + " source portal");
		TFTeleporter.PortalShape target = require(TFTeleporter.resolvePortalShape(targetLevel, targetAnchor), name + " target portal");
		return new PortalPair(sourceLevel, targetLevel, source, target, name);
	}

	private static void preparePortal(ServerLevel level, BlockPos anchor) {
		for (int chunkX = (anchor.getX() - 2) >> 4; chunkX <= (anchor.getX() + 3) >> 4; chunkX++) {
			for (int chunkZ = (anchor.getZ() - 2) >> 4; chunkZ <= (anchor.getZ() + 3) >> 4; chunkZ++) {
				level.getChunk(chunkX, chunkZ);
			}
		}
		boolean completePortal = true;
		for (int x = 0; x < 2; x++) {
			for (int z = 0; z < 2; z++) {
				completePortal &= level.getBlockState(anchor.offset(x, 0, z)).is(TFBlocks.TWILIGHT_PORTAL);
			}
		}
		for (int x = -1; x <= 2; x++) {
			for (int z = -1; z <= 2; z++) {
				BlockPos floor = anchor.offset(x, -1, z);
				level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
				if (!completePortal || x < 0 || x > 1 || z < 0 || z > 1) {
					level.setBlockAndUpdate(floor.above(), Blocks.GRASS_BLOCK.defaultBlockState());
				}
				level.setBlockAndUpdate(floor.above(2), Blocks.AIR.defaultBlockState());
			}
		}
		if (!completePortal) {
			for (int x = 0; x < 2; x++) {
				for (int z = 0; z < 2; z++) {
					level.setBlock(anchor.offset(x, 0, z), TFBlocks.TWILIGHT_PORTAL.defaultBlockState(), Block.UPDATE_CLIENTS);
				}
			}
		}
	}

	private static void checkTransition(TeleportTransition transition, BlockPos target, String direction) {
		check(transition != null, direction + " exact portal lookup returned null");
		double horizontalDistance = Math.hypot(transition.position().x() - target.getX(), transition.position().z() - target.getZ());
		check(horizontalDistance < 4.0D, direction + " exact portal lookup selected a different portal");
	}

	private static Pig pigAt(ServerLevel level, BlockPos pos) {
		Pig pig = require(EntityTypes.PIG.create(level, EntitySpawnReason.COMMAND), "probe pig");
		pig.setPos(pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D);
		return pig;
	}

	private static void assertLinked(TeleporterCache cache, PortalPair pair) {
		check(pair.target.endpoint().equals(cache.getLinkedPortal(pair.source.endpoint())), pair.name + " forward link mismatch");
		check(pair.source.endpoint().equals(cache.getLinkedPortal(pair.target.endpoint())), pair.name + " reverse link mismatch");
	}

	private static long percentile(List<Long> sorted, int percentile) {
		int index = Math.min(sorted.size() - 1, Math.max(0, (int) Math.ceil(sorted.size() * percentile / 100.0D) - 1));
		return sorted.get(index);
	}

	private static void verifyExpectedMods() {
		String expected = System.getProperty("twilightforest.runtimeExpectedMods", "");
		Arrays.stream(expected.split(","))
			.map(String::trim)
			.filter(id -> !id.isEmpty())
			.forEach(id -> check(FabricLoader.getInstance().isModLoaded(id), "expected mod is not loaded: " + id));
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}

	private static <T> T require(T value, String description) {
		if (value == null) {
			throw new IllegalStateException("Missing " + description);
		}
		return value;
	}

	private record PortalPair(
		ServerLevel sourceLevel,
		ServerLevel targetLevel,
		TFTeleporter.PortalShape source,
		TFTeleporter.PortalShape target,
		String name
	) {
	}

	private record PortalSpec(
		ServerLevel sourceLevel,
		BlockPos sourceAnchor,
		ServerLevel targetLevel,
		BlockPos targetAnchor,
		String name
	) {
	}

	private record ForcedChunk(ServerLevel level, int chunkX, int chunkZ) {
	}
}
