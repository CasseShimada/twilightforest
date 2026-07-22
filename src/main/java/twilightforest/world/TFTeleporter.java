package twilightforest.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.block.TFPortalBlock;
import twilightforest.config.TFConfig;
import twilightforest.tags.TFBlockTags;
import twilightforest.tags.TFStructureTags;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFDimension;
import twilightforest.util.iterators.DiagonalSpiralIterator;
import twilightforest.util.iterators.XZQuadrantIterator;
import twilightforest.util.landmarks.LandmarkUtil;
import twilightforest.util.landmarks.LegacyLandmarkPlacements;
import twilightforest.util.Restriction;

import java.util.*;
import java.util.function.Predicate;

public class TFTeleporter {
	private static final int PORTAL_SEARCH_RADIUS = 200;
	private static final int MIN_PORTAL_BLOCKS = 4;
	private static final Comparator<BlockPos> PORTAL_BLOCK_ORDER = Comparator
		.comparingInt((BlockPos block) -> block.getY())
		.thenComparingInt(block -> block.getX())
		.thenComparingInt(block -> block.getZ());

	public static TeleportTransition createTransition(Entity entity, ServerLevel dest, BlockPos pos, boolean forcedEntry) {
		TeleportTransition transition;
		TeleporterCache cache = TeleporterCache.get(dest);

		if ((transition = placeInExistingPortal(cache, dest, entity, pos)) == null) {
			TwilightForestMod.LOGGER.debug("Did not find existing portal, making a new one.");
			transition = createPosition(dest, entity, pos, cache, forcedEntry);
		}

		if (transition != null) return transition;
		return makePortalInfo(dest, entity, Vec3.atCenterOf(dest.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos)));
	}

	@Nullable
	protected static TeleportTransition createPosition(ServerLevel dest, Entity entity, BlockPos destPos, TeleporterCache cache, boolean locked) {
		TeleportTransition info = moveToSafeCoords(dest, entity, destPos);
		makePortal(cache, entity, dest, info.position(), locked);
		info = placeInExistingPortal(cache, dest, entity, BlockPos.containing(info.position()));
		return info;
	}

	@Nullable
	protected static TeleportTransition placeInExistingPortal(TeleporterCache cache, ServerLevel destDim, Entity entity, BlockPos pos) {
		if (!(entity.level() instanceof ServerLevel sourceLevel)) {
			return null;
		}

		PortalShape source = resolvePortalShape(sourceLevel, entity.blockPosition());
		if (source == null) {
			TwilightForestMod.LOGGER.warn("Could not resolve a Twilight Forest portal shape around {} in {}; refusing to create an ambiguous return link",
				entity.blockPosition().toShortString(), sourceLevel.dimension().identifier());
			return null;
		}
		cache.registerPortal(source.endpoint());

		PortalShape target = findLinkedTarget(cache, source, destDim);
		if (target == null) {
			PortalPosition legacy = cache.consumeLegacyHint(destDim.dimension().identifier(), source.columns());
			if (legacy != null) {
				target = resolvePortalShape(destDim, legacy.pos);
				if (target == null || !cache.isAvailableFor(target.endpoint(), source.endpoint()) || !cache.link(source.endpoint(), target.endpoint())) {
					target = null;
				}
			}
		}
		if (target == null) {
			target = findIndexedTarget(cache, source, destDim, pos);
		}
		if (target == null) {
			for (PortalShape candidate : findLoadedPortals(destDim, pos, PORTAL_SEARCH_RADIUS)) {
				if (cache.isAvailableFor(candidate.endpoint(), source.endpoint()) && cache.link(source.endpoint(), candidate.endpoint())) {
					target = candidate;
					break;
				}
			}
		}

		if (target == null) {
			return null;
		}

		BlockPos anchor = target.endpoint().anchor();
		destDim.getChunkSource().addTicketWithRadius(TicketType.PORTAL, ChunkPos.containing(anchor), 3);
		BlockPos[] portalBorder = getBoundaryPositions(destDim, target.blocks()).toArray(new BlockPos[0]);
		BlockPos borderPos = portalBorder.length > 0
			? portalBorder[destDim.getRandom().nextInt(portalBorder.length)]
			: anchor;
		return makePortalInfo(destDim, entity, borderPos.getX() + 0.5, borderPos.getY() + 1.0, borderPos.getZ() + 0.5);
	}

	@Nullable
	private static PortalShape findLinkedTarget(TeleporterCache cache, PortalShape source, ServerLevel destination) {
		TeleporterCache.PortalEndpoint linked = cache.getLinkedPortal(source.endpoint(), destination.dimension().identifier());
		if (linked == null) {
			TeleporterCache.PortalEndpoint wrongDimension = cache.getLinkedPortal(source.endpoint());
			if (wrongDimension != null) {
				cache.invalidate(source.endpoint());
				cache.registerPortal(source.endpoint());
			}
			return null;
		}
		loadEndpointChunks(destination, linked);
		PortalShape target = readPortalShape(destination, linked.anchor());
		if (target == null || !target.endpoint().equals(linked)) {
			TwilightForestMod.LOGGER.debug("Invalidating stale portal link from {} to {}", source.endpoint().anchor(), linked.anchor());
			cache.invalidate(source.endpoint());
			cache.registerPortal(source.endpoint());
			return null;
		}
		return target;
	}

	@Nullable
	private static PortalShape findIndexedTarget(TeleporterCache cache, PortalShape source, ServerLevel destination, BlockPos near) {
		for (TeleporterCache.PortalEndpoint endpoint : cache.nearestIndexedPortals(destination.dimension().identifier(), near, PORTAL_SEARCH_RADIUS)) {
			if (!cache.isAvailableFor(endpoint, source.endpoint()) || !areEndpointChunksLoaded(destination, endpoint)) {
				continue;
			}
			PortalShape candidate = readPortalShape(destination, endpoint.anchor());
			if (candidate == null || !candidate.endpoint().equals(endpoint)) {
				cache.invalidate(endpoint);
				continue;
			}
			if (cache.link(source.endpoint(), candidate.endpoint())) {
				return candidate;
			}
		}
		return null;
	}

	private static List<PortalShape> findLoadedPortals(ServerLevel level, BlockPos center, int radius) {
		int minChunkX = (center.getX() - radius) >> 4;
		int maxChunkX = (center.getX() + radius) >> 4;
		int minChunkZ = (center.getZ() - radius) >> 4;
		int maxChunkZ = (center.getZ() + radius) >> 4;
		long radiusSquared = (long) radius * radius;
		Map<BlockPos, PortalShape> shapes = new HashMap<>();

		for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
				if (chunk == null) {
					continue;
				}
				LevelChunkSection[] sections = chunk.getSections();
				for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
					LevelChunkSection section = sections[sectionIndex];
					if (!section.maybeHas(TFTeleporter::isPortal)) {
						continue;
					}
					int baseY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;
					for (int localY = 0; localY < 16; localY++) {
						for (int localX = 0; localX < 16; localX++) {
							for (int localZ = 0; localZ < 16; localZ++) {
								if (!isPortal(section.getBlockState(localX, localY, localZ))) {
									continue;
								}
								BlockPos seed = new BlockPos((chunkX << 4) + localX, baseY + localY, (chunkZ << 4) + localZ);
								long dx = (long) seed.getX() - center.getX();
								long dz = (long) seed.getZ() - center.getZ();
								if (dx * dx + dz * dz > radiusSquared || !level.getWorldBorder().isWithinBounds(seed)) {
									continue;
								}
								PortalShape shape = readPortalShape(level, seed);
								if (shape != null) {
									shapes.putIfAbsent(shape.endpoint().anchor(), shape);
								}
							}
						}
					}
				}
			}
		}

		return shapes.values().stream()
			.sorted(Comparator.comparingDouble((PortalShape shape) -> shape.endpoint().anchor().distSqr(center))
				.thenComparing(shape -> shape.endpoint().anchor(), PORTAL_BLOCK_ORDER))
			.toList();
	}

	@Nullable
	static PortalShape resolvePortalShape(ServerLevel level, BlockPos hint) {
		for (int verticalOffset = -2; verticalOffset <= 1; verticalOffset++) {
			for (int radius = 0; radius <= 2; radius++) {
				for (int offsetX = -radius; offsetX <= radius; offsetX++) {
					for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
						if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) != radius) {
							continue;
						}
						BlockPos seed = hint.offset(offsetX, verticalOffset, offsetZ);
						BlockState state = getLoadedBlockState(level, seed);
						if (state != null && isPortal(state)) {
							return readPortalShape(level, seed);
						}
					}
				}
			}
		}
		return null;
	}

	@Nullable
	private static PortalShape readPortalShape(ServerLevel level, BlockPos seed) {
		BlockState seedState = getLoadedBlockState(level, seed);
		if (seedState == null || !isPortal(seedState)) {
			return null;
		}

		int limit = Math.min(4096, Math.max(4, TFConfig.maxPortalSize));
		Set<BlockPos> blocks = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		blocks.add(seed.immutable());
		queue.add(seed.immutable());
		while (!queue.isEmpty()) {
			BlockPos current = queue.removeFirst();
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				BlockPos adjacent = current.relative(direction);
				BlockState adjacentState = getLoadedBlockState(level, adjacent);
				if (adjacentState == null) {
					// Full-shape validation cannot be proven across an unloaded chunk edge.
					return null;
				}
				if (isPortal(adjacentState) && blocks.add(adjacent.immutable())) {
					if (blocks.size() > limit) {
						TwilightForestMod.LOGGER.warn("Ignoring oversized Twilight Forest portal near {} (configured maximum {})", seed.toShortString(), limit);
						return null;
					}
					queue.addLast(adjacent.immutable());
				}
			}
		}
		if (blocks.size() < MIN_PORTAL_BLOCKS) {
			return null;
		}

		List<BlockPos> ordered = blocks.stream().sorted(PORTAL_BLOCK_ORDER).toList();
		BlockPos anchor = ordered.getFirst();
		int minX = ordered.stream().mapToInt(BlockPos::getX).min().orElseThrow();
		int minY = ordered.stream().mapToInt(BlockPos::getY).min().orElseThrow();
		int minZ = ordered.stream().mapToInt(BlockPos::getZ).min().orElseThrow();
		int maxX = ordered.stream().mapToInt(BlockPos::getX).max().orElseThrow();
		int maxY = ordered.stream().mapToInt(BlockPos::getY).max().orElseThrow();
		int maxZ = ordered.stream().mapToInt(BlockPos::getZ).max().orElseThrow();
		long fingerprint = 0xcbf29ce484222325L;
		for (BlockPos block : ordered) {
			fingerprint = mixFingerprint(fingerprint, block.getX() - anchor.getX());
			fingerprint = mixFingerprint(fingerprint, block.getY() - anchor.getY());
			fingerprint = mixFingerprint(fingerprint, block.getZ() - anchor.getZ());
			BlockState state = getLoadedBlockState(level, block);
			fingerprint = mixFingerprint(fingerprint,
				state != null && state.getValue(TFPortalBlock.DISALLOW_RETURN) ? 1 : 0);
		}

		TeleporterCache.PortalEndpoint endpoint = new TeleporterCache.PortalEndpoint(
			level.dimension().identifier(), anchor, ordered.size(), fingerprint,
			new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
		return new PortalShape(endpoint, blocks);
	}

	private static long mixFingerprint(long hash, long value) {
		for (int shift = 0; shift < Long.SIZE; shift += Byte.SIZE) {
			hash ^= value >>> shift & 0xffL;
			hash *= 0x100000001b3L;
		}
		return hash;
	}

	@Nullable
	private static BlockState getLoadedBlockState(ServerLevel level, BlockPos pos) {
		LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
		return chunk == null ? null : chunk.getBlockState(pos);
	}

	private static boolean areEndpointChunksLoaded(ServerLevel level, TeleporterCache.PortalEndpoint endpoint) {
		// Shape validation inspects the horizontal neighbours of every portal block. Include that
		// one-block halo so a portal on a chunk edge is never rejected merely because getChunkNow
		// cannot see the adjacent chunk.
		for (int chunkX = (endpoint.min().getX() - 1) >> 4; chunkX <= (endpoint.max().getX() + 1) >> 4; chunkX++) {
			for (int chunkZ = (endpoint.min().getZ() - 1) >> 4; chunkZ <= (endpoint.max().getZ() + 1) >> 4; chunkZ++) {
				if (level.getChunkSource().getChunkNow(chunkX, chunkZ) == null) {
					return false;
				}
			}
		}
		return true;
	}

	private static void loadEndpointChunks(ServerLevel level, TeleporterCache.PortalEndpoint endpoint) {
		for (int chunkX = (endpoint.min().getX() - 1) >> 4; chunkX <= (endpoint.max().getX() + 1) >> 4; chunkX++) {
			for (int chunkZ = (endpoint.min().getZ() - 1) >> 4; chunkZ <= (endpoint.max().getZ() + 1) >> 4; chunkZ++) {
				level.getChunk(chunkX, chunkZ);
			}
		}
	}

	private static int getScanHeight(ServerLevel world, int x, int z) {
		LevelChunk chunk = world.getChunkSource().getChunkNow(x >> 4, z >> 4);
		if (chunk == null) {
			return world.getMinY();
		}
		@SuppressWarnings("removal")
		int chunkHeight = chunk.getHighestSectionPosition() + 15;
		return Math.min(world.getMaxY() - 1, chunkHeight);
	}

	private static boolean isPortal(BlockState state) {
		return state.is(TFBlocks.TWILIGHT_PORTAL);
	}

	// from the start point, builds a set of all directly adjacent non-portal blocks
	private static Set<BlockPos> getBoundaryPositions(ServerLevel world, Set<BlockPos> portalBlocks) {
		Set<BlockPos> result = new HashSet<>();
		for (BlockPos portal : portalBlocks) {
			for (Direction facing : Direction.Plane.HORIZONTAL) {
				BlockPos offset = portal.relative(facing);
				if (portalBlocks.contains(offset)) {
					continue;
				}
				BlockState checkState = world.getBlockState(offset);
				if (Block.isFaceFull(checkState.getCollisionShape(world, offset), Direction.UP)
					&& world.getBlockState(offset.above()).getCollisionShape(world, offset.above()).isEmpty()) {
					result.add(offset);
				}
			}
		}
		return result;
	}

	protected static boolean isPortalAt(ServerLevel world, BlockPos pos) {
		return isPortal(world.getBlockState(pos));
	}

	// Scale the coords based on the dimension type coordinate_scale
	protected static double getHorizontalScale(ServerLevel destination) {
		ServerLevel tfDim = destination.getServer().getLevel(TFDimension.DIMENSION_KEY);
		double scale = tfDim == null ? 0.125D : tfDim.dimensionType().coordinateScale();
		return destination.dimension().equals(TFDimension.DIMENSION_KEY) ? 1F / scale : scale;
	}

	protected static TeleportTransition moveToSafeCoords(ServerLevel level, Entity entity, BlockPos pos) {
		// if we're in enforced progression mode, check the biomes for safety
		boolean checkProgression = LandmarkUtil.isProgressionEnforced(level);

		if (isSafeAround(level, pos, entity, checkProgression)) {
			TwilightForestMod.LOGGER.debug("Portal destination looks safe!");
			return makePortalInfo(level, entity, Vec3.atCenterOf(pos));
		}

		TwilightForestMod.LOGGER.debug("Portal destination looks unsafe, rerouting!");

		BlockPos safeCoords = scanIntoSafeBiomes(level, pos, entity, checkProgression);
		if (safeCoords != null) {
			TwilightForestMod.LOGGER.debug("Safely rerouted!");
			return makePortalInfo(level, entity, Vec3.atCenterOf(safeCoords));
		}

		TwilightForestMod.LOGGER.warn("Did not find a safe portal spot.");

		return makePortalInfo(level, entity, Vec3.atCenterOf(pos));
	}

	@Nullable
	private static BlockPos scanIntoSafeBiomes(ServerLevel level, BlockPos pos, Entity entity, boolean checkProgression) {
		Iterable<BlockPos> biomeCenterGrid = new DiagonalSpiralIterator<>(pos.getX() >> 4, pos.getZ() >> 4, false, 128, 16, LegacyLandmarkPlacements::getNearestCenterXZ);

		for (BlockPos biomeCenter : biomeCenterGrid) {
			if (checkProgression && biomeUnsafe(level, biomeCenter, entity)) {
				continue;
			}

			Iterable<BlockPos> gridAroundLandmark = new XZQuadrantIterator<>(biomeCenter.getX(), biomeCenter.getZ(), true, 8, 16, (x, z) -> new BlockPos(x, 4, z));
			for (BlockPos posInBiome : gridAroundLandmark) {
				if (isSafeAround(level, posInBiome, entity, checkProgression)) {
					TwilightForestMod.LOGGER.debug("Found {} in biome-scanning for safe portal placement", posInBiome.toShortString());
					return posInBiome;
				}
			}
		}

		return null;
	}

	public static boolean isSafeAround(Level world, BlockPos pos, Entity entity, boolean checkProgression) {

		if (isUnsafe(world, pos, entity, checkProgression)) {
			return false;
		}

		for (Direction facing : Direction.Plane.HORIZONTAL) {
			if (isUnsafe(world, pos.relative(facing, 16), entity, checkProgression)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isUnsafe(Level world, BlockPos pos, Entity entity, boolean checkProgression) {
		if (!world.dimension().equals(TFDimension.DIMENSION_KEY)) {
			return false;
		}

		if (!world.getWorldBorder().isWithinBounds(pos)) {
			return true;
		}

		if (checkProgression && biomeUnsafe(world, pos, entity)) {
			return true;
		}

		return posOverlapsRestrictedStructureChunk(world, pos);
	}

	public static boolean posOverlapsRestrictedStructureChunk(Level destLevel, BlockPos pos) {
		Iterator<Holder<Structure>> landmarksInChunk = destLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE).getTagOrEmpty(TFStructureTags.LANDMARK).iterator();
		LevelChunk chunkAt = destLevel.getChunkAt(pos);

		while (landmarksInChunk.hasNext()) {
			Holder<Structure> structureHolder = landmarksInChunk.next();
			if (!chunkAt.getReferencesForStructure(structureHolder.value()).isEmpty()) {
				return true;
			}
		}

		return false;
	}

	private static boolean biomeUnsafe(Level world, BlockPos pos, Entity entity) {
		return !Restriction.isBiomeSafeFor(world.getBiome(pos).value(), entity);
	}

	protected static void makePortal(TeleporterCache cache, Entity entity, ServerLevel world, Vec3 pos, boolean locked) {
		ServerLevel src = entity.level() instanceof ServerLevel serverLevel ? serverLevel : null;

		// ensure area is populated first
		loadSurroundingArea(world, pos);

		BlockPos spot;
		String name = entity.getName().getString();
		spot = findPortalCoords(world, pos, blockpos -> isIdealForPortal(world, blockpos));

		if (spot != null) {
			TwilightForestMod.LOGGER.debug("Found ideal portal spot for {} at {}", name, spot);
			linkNewPortal(cache, src, entity.blockPosition(), world, makePortalAt(world, spot, locked));
			return;
		}

		TwilightForestMod.LOGGER.debug("Did not find ideal portal spot, shooting for okay one for {}", name);
		spot = findPortalCoords(world, pos, blockPos -> isOkayForPortal(world, blockPos));

		if (spot != null) {
			TwilightForestMod.LOGGER.debug("Found okay portal spot for {} at {}", name, spot);
			linkNewPortal(cache, src, entity.blockPosition(), world, makePortalAt(world, spot, locked));
			return;
		}

		TwilightForestMod.LOGGER.debug("Did not even find an okay portal spot, just making a fallback one for {}", name);

		spot = findPortalCoords(world, pos, blockpos -> isOkayForFallbackPortal(world, blockpos), true);
		if (spot != null) {
			TwilightForestMod.LOGGER.debug("Found fallback portal spot for {} at {}", name, spot);
			linkNewPortal(cache, src, entity.blockPosition(), world, makePortalAt(world, spot, locked));
			return;
		}

		// well I don't think we can actually just return and fail here
		TwilightForestMod.LOGGER.debug("Did not even find a fallback portal spot, just making a random one for {}", name);

		BlockPos horizontallyScaled = BlockPos.containing(entity.getX() * getHorizontalScale(world), entity.getY(), entity.getZ() * getHorizontalScale(world));
		spot = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, horizontallyScaled);
		linkNewPortal(cache, src, entity.blockPosition(), world, makePortalAt(world, spot, locked));
	}

	protected static void loadSurroundingArea(ServerLevel world, Vec3 pos) {

		int x = Mth.floor(pos.x()) >> 4;
		int z = Mth.floor(pos.z()) >> 4;

		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				world.getChunk(x + dx, z + dz);
			}
		}
	}

	@Nullable
	protected static BlockPos findPortalCoords(ServerLevel world, Vec3 loc, Predicate<BlockPos> predicate) {
		return findPortalCoords(world, loc, predicate, false);
	}

	@Nullable
	protected static BlockPos findPortalCoords(ServerLevel world, Vec3 loc, Predicate<BlockPos> predicate, boolean makePortalInAir) {
		// adjust the height based on what world we're traveling to
		double yFactor = getYFactor(world);
		// modified copy of base Teleporter method:
		int entityX = Mth.floor(loc.x());
		int entityZ = Mth.floor(loc.z());

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		double spotWeight = -1D;
		BlockPos spot = null;

		int range = 16;
		for (int rx = entityX - range; rx <= entityX + range; rx++) {
			double xWeight = (rx + 0.5D) - loc.x();
			for (int rz = entityZ - range; rz <= entityZ + range; rz++) {
				double zWeight = (rz + 0.5D) - loc.z();

				for (int ry = getScanHeight(world, rx, rz); ry >= world.getMinY(); ry--) {


					pos.set(rx, ry, rz);
					if (!makePortalInAir && !world.isEmptyBlock(pos)) {
						continue;
					}

					if (makePortalInAir) {
						while (ry > world.getMinY() && world.isEmptyBlock(pos.set(rx, ry - 1, rz)) && predicate.test(pos)) {
							ry--;
						}
						pos.set(rx, ry, rz);
					} else {
						while (ry > world.getMinY() && world.isEmptyBlock(pos.set(rx, ry - 1, rz))) {
							ry--;
						}
					}

					if (isTreeTopPortalCandidate(world, pos)) {
						continue;
					}

					double yWeight = (ry + 0.5D) - loc.y() * yFactor;
					double rPosWeight = xWeight * xWeight + yWeight * yWeight + zWeight * zWeight;

					if (spotWeight < 0.0D || rPosWeight < spotWeight) {
						// check from the "in ground" pos
						if (predicate.test(pos)) {
							spotWeight = rPosWeight;
							spot = pos.immutable();
						}
					}
				}
			}
		}

		return spot;
	}

	private static boolean isTreeTopPortalCandidate(ServerLevel world, BlockPos pos) {
		return isTreeSupport(world.getBlockState(pos.below()))
			|| isTreeSupport(world.getBlockState(pos.east().below()))
			|| isTreeSupport(world.getBlockState(pos.south().below()))
			|| isTreeSupport(world.getBlockState(pos.east().south().below()));
	}

	private static boolean isTreeSupport(BlockState state) {
		return state.is(BlockTags.LOGS)
			|| state.is(BlockTags.LEAVES)
			|| state.is(TFBlocks.HARDENED_DARK_LEAVES);
	}

	protected static double getYFactor(ServerLevel world) {
		return world.dimension().identifier().equals(Level.OVERWORLD.identifier()) ? 2.0 : 0.5;
	}

	private static void linkNewPortal(TeleporterCache cache, @Nullable ServerLevel sourceLevel, BlockPos sourceHint, ServerLevel destinationLevel, BlockPos destinationHint) {
		if (sourceLevel == null) {
			return;
		}
		PortalShape source = resolvePortalShape(sourceLevel, sourceHint);
		PortalShape destination = resolvePortalShape(destinationLevel, destinationHint);
		if (source == null || destination == null) {
			TwilightForestMod.LOGGER.warn("Created a portal near {} but could not resolve both portal shapes; no ambiguous link was persisted", destinationHint.toShortString());
			return;
		}
		if (!cache.link(source.endpoint(), destination.endpoint())) {
			TwilightForestMod.LOGGER.warn("Created portal {} could not be linked to {} because one endpoint already has a different exact partner",
				destination.endpoint().anchor().toShortString(), source.endpoint().anchor().toShortString());
		}
	}

	protected static boolean isIdealForPortal(ServerLevel world, BlockPos pos) {
		for (int potentialZ = 0; potentialZ < 4; potentialZ++) {
			for (int potentialX = 0; potentialX < 4; potentialX++) {
				for (int potentialY = 0; potentialY < 6; potentialY++) {
					BlockPos tPos = pos.offset(potentialX - 1, potentialY, potentialZ - 1);
					BlockState state = world.getBlockState(tPos);

					// all blocks mustn't be bedrock, end portal frame, etc.; and other conditions for layers >= 0
					if (state.is(BlockTags.FEATURES_CANNOT_REPLACE) || potentialY == 0 && !state.is(BlockTags.DIRT) || potentialY >= 1 && !state.canBeReplaced()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	protected static BlockPos makePortalAt(Level world, BlockPos pos, boolean locked) {
		// grass all around it
		BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();

		world.setBlockAndUpdate(pos.west().north(), grass);
		world.setBlockAndUpdate(pos.north(), grass);
		world.setBlockAndUpdate(pos.east().north(), grass);
		world.setBlockAndUpdate(pos.east(2).north(), grass);

		world.setBlockAndUpdate(pos.west(), grass);
		world.setBlockAndUpdate(pos.east(2), grass);

		world.setBlockAndUpdate(pos.west().south(), grass);
		world.setBlockAndUpdate(pos.east(2).south(), grass);

		world.setBlockAndUpdate(pos.west().south(2), grass);
		world.setBlockAndUpdate(pos.south(2), grass);
		world.setBlockAndUpdate(pos.east().south(2), grass);
		world.setBlockAndUpdate(pos.east(2).south(2), grass);

		BlockPos[] positions = new BlockPos[4];
		positions[0] = pos.below();
		positions[1] = pos.east().below();
		positions[2] = pos.south().below();
		positions[3] = pos.east().south().below();

		// dirt under it
		BlockState dirt = Blocks.DIRT.defaultBlockState();
		for (BlockPos blockpos : positions) {
			BlockState state = world.getBlockState(blockpos);
			if (state.is(BlockTags.DIRT) || state.is(BlockTags.REPLACEABLE) || state.is(BlockTags.AIR)) {
				world.setBlockAndUpdate(blockpos, dirt);
			}
		}

		// portal in it
		BlockState portal = TFBlocks.TWILIGHT_PORTAL.defaultBlockState().setValue(TFPortalBlock.DISALLOW_RETURN, (locked || !TFConfig.shouldReturnPortalBeUsable));

		world.setBlock(pos, portal, Block.UPDATE_CLIENTS);
		world.setBlock(pos.east(), portal, Block.UPDATE_CLIENTS);
		world.setBlock(pos.south(), portal, Block.UPDATE_CLIENTS);
		world.setBlock(pos.east().south(), portal, Block.UPDATE_CLIENTS);

		// meh, let's just make a bunch of air over it for 4 squares
		for (int dx = -1; dx <= 2; dx++) {
			for (int dz = -1; dz <= 2; dz++) {
				for (int dy = 1; dy <= 5; dy++) {
					world.removeBlock(pos.offset(dx, dy, dz), false);
				}
			}
		}

		// finally, "nature decorations"!
		world.setBlock(pos.west().north().above(), randNatureBlock(world.getRandom()), Block.UPDATE_CLIENTS);
		world.setBlock(pos.north().above(), randNatureBlock(world.getRandom()), Block.UPDATE_CLIENTS);
		world.setBlock(pos.east().north().above(), randNatureBlock(world.getRandom()), Block.UPDATE_CLIENTS);
		world.setBlock(pos.east(2).north().above(), randNatureBlock(world.getRandom()), Block.UPDATE_CLIENTS);

		world.setBlock(pos.west().above(), randNatureBlock(world.getRandom()), Block.UPDATE_CLIENTS);
		world.setBlock(pos.east(2).above(), randNatureBlock(world.getRandom()), Block.UPDATE_CLIENTS);

		world.setBlock(pos.west().south().above(), randNatureBlock(world.getRandom()), Block.UPDATE_CLIENTS);
		world.setBlock(pos.east(2).south().above(), randNatureBlock(world.getRandom()), Block.UPDATE_CLIENTS);

		world.setBlock(pos.west().south(2).above(), randNatureBlock(world.getRandom()), Block.UPDATE_CLIENTS);
		world.setBlock(pos.south(2).above(), randNatureBlock(world.getRandom()), Block.UPDATE_CLIENTS);
		world.setBlock(pos.east().south(2).above(), randNatureBlock(world.getRandom()), Block.UPDATE_CLIENTS);
		world.setBlock(pos.east(2).south(2).above(), randNatureBlock(world.getRandom()), Block.UPDATE_CLIENTS);

		return pos;
	}

	private static BlockState randNatureBlock(RandomSource random) {
		Optional<Block> optional = BuiltInRegistries.BLOCK
			.get(TFBlockTags.GENERATED_PORTAL_DECO)
			.flatMap(tag -> tag.getRandomElement(random))
			.map(Holder::value);
		return optional.map(Block::defaultBlockState).orElseGet(Blocks.SHORT_GRASS::defaultBlockState);
	}

	protected static boolean isOkayForPortal(ServerLevel world, BlockPos pos) {
		for (int potentialZ = 0; potentialZ < 4; potentialZ++) {
			for (int potentialX = 0; potentialX < 4; potentialX++) {
				for (int potentialY = 0; potentialY < 6; potentialY++) {
					BlockPos tPos = pos.offset(potentialX - 1, potentialY, potentialZ - 1);
					BlockState state = world.getBlockState(tPos);

					// all blocks mustn't be bedrock, end portal frame, etc.; and other conditions for layers >= 0
					if (state.is(BlockTags.FEATURES_CANNOT_REPLACE) || potentialY == 0 && !state.isSolid() && !state.liquid() || potentialY >= 1 && !state.canBeReplaced()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	protected static boolean isOkayForFallbackPortal(ServerLevel world, BlockPos pos) {
		for (int potentialZ = 0; potentialZ < 4; potentialZ++) {
			for (int potentialX = 0; potentialX < 4; potentialX++) {
				for (int potentialY = 0; potentialY < 6; potentialY++) {
					BlockPos tPos = pos.offset(potentialX - 1, potentialY, potentialZ - 1);
					BlockState state = world.getBlockState(tPos);

					// all blocks mustn't be bedrock, end portal frame, etc.;
					if (state.is(BlockTags.FEATURES_CANNOT_REPLACE) || potentialY >= 1 && !state.canBeReplaced()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	protected static TeleportTransition makePortalInfo(ServerLevel level, Entity entity, double x, double y, double z) {
		return makePortalInfo(level, entity, new Vec3(x, y, z));
	}

	protected static TeleportTransition makePortalInfo(ServerLevel level, Entity entity, Vec3 pos) {
		return new TeleportTransition(level, safePosInColumn(level, entity, pos), Vec3.ZERO, entity.getYRot(), entity.getXRot(), TeleportTransition.PLACE_PORTAL_TICKET);
	}

	protected static Vec3 safePosInColumn(ServerLevel level, Entity entity, Vec3 pos) {
		AABB aabb = entity.getDimensions(entity.getPose()).makeBoundingBox(pos);

		if (level.noCollision(aabb)) {
			return pos;
		}

		int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(pos.x), Mth.floor(pos.z));
		return pos.with(Direction.Axis.Y, height);
	}

	static class PortalPosition {
		public final BlockPos pos;
		long lastUpdateTime;

		PortalPosition(BlockPos pos, long time) {
			this.pos = pos;
			this.lastUpdateTime = time;
		}
	}

	static record PortalShape(TeleporterCache.PortalEndpoint endpoint, Set<BlockPos> blocks) {
		PortalShape {
			blocks = Set.copyOf(blocks);
		}

		Set<ColumnPos> columns() {
			Set<ColumnPos> columns = new HashSet<>();
			for (BlockPos block : this.blocks) {
				columns.add(new ColumnPos(block.getX(), block.getZ()));
			}
			return columns;
		}
	}
}
