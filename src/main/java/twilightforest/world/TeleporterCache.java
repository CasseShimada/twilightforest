package twilightforest.world;

import com.mojang.serialization.Codec;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.mixin.accessor.SavedDataStorageAccessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Persistent, server-authoritative portal links.
 *
 * <p>Version 1 keyed a target by only the source X/Z column. Those entries are retained as
 * one-shot migration hints, but are never treated as an exact link. Version 2 links complete
 * portal identities in both directions.</p>
 */
public class TeleporterCache extends SavedData {

	private static final int FORMAT_VERSION = 2;
	private static final Codec<TeleporterCache> CODEC = CompoundTag.CODEC.xmap(TeleporterCache::load, cache -> cache.save(new CompoundTag()));
	private static final SavedDataType<TeleporterCache> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("twilightforest", "teleporter_cache"), TeleporterCache::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
	private static final String LEGACY_SAVED_DATA_FILE = "twilightforest_teleporter_cache.dat";
	private static final String LEGACY_CAPABILITY_FILE = "capabilities.dat";
	private static final String LEGACY_PROVIDER = "twilightforest:teleporter_cache";
	private static final Comparator<PortalEndpoint> ENDPOINT_ORDER = Comparator
		.comparing((PortalEndpoint endpoint) -> endpoint.dimension().toString())
		.thenComparingInt(endpoint -> endpoint.anchor().getY())
		.thenComparingInt(endpoint -> endpoint.anchor().getX())
		.thenComparingInt(endpoint -> endpoint.anchor().getZ())
		.thenComparingLong(PortalEndpoint::fingerprint)
		.thenComparingInt(PortalEndpoint::blockCount);

	private final Map<PortalEndpoint, PortalEndpoint> links = new HashMap<>();
	private final Map<Identifier, Set<PortalEndpoint>> portalIndex = new HashMap<>();
	// Version-1 migration hints: [destination dimension, source X/Z column] -> target position.
	private final Map<Identifier, Map<ColumnPos, TFTeleporter.PortalPosition>> legacyHints = new HashMap<>();

	TeleporterCache() {
		this.setDirty();
	}

	public static TeleporterCache get(ServerLevel level) {
		ServerLevel server = level.getServer().overworld();
		SavedDataStorage storage = server.getDataStorage();
		Path currentDataFolder = ((SavedDataStorageAccessor) storage).twilightforest$getDataFolder();
		return get(storage, currentDataFolder, level.getServer().getWorldPath(LevelResource.DATA));
	}

	static TeleporterCache get(SavedDataStorage storage, Path currentDataFolder, Path legacyDataFolder) {
		Path currentFile = TYPE.id().withSuffix(".dat").resolveAgainst(currentDataFolder);
		TeleporterCache current;
		try {
			current = storage.get(TYPE);
		} catch (RuntimeException exception) {
			if (Files.exists(currentFile)) {
				throw new IllegalStateException("Current teleporter cache exists but could not be decoded: " + currentFile, exception);
			}
			throw exception;
		}
		if (current != null) {
			return current;
		}

		if (Files.exists(currentFile)) {
			throw new IllegalStateException("Current teleporter cache exists but could not be decoded: " + currentFile);
		}

		Path legacySavedDataFile = legacyDataFolder.resolve(LEGACY_SAVED_DATA_FILE);
		Optional<TeleporterCache> legacy = readLegacySavedData(storage, legacySavedDataFile);
		if (legacy.isPresent()) {
			TeleporterCache migrated = legacy.orElseThrow();
			storage.set(TYPE, migrated);
			TwilightForestMod.LOGGER.info("Imported legacy flat teleporter cache from {}; entries will be shape-validated on first use and the source file was left unchanged", legacySavedDataFile);
			return migrated;
		}

		Path legacyCapabilityFile = legacyDataFolder.resolve(LEGACY_CAPABILITY_FILE);
		legacy = readLegacyCapability(storage, legacyCapabilityFile);
		if (legacy.isPresent()) {
			TeleporterCache migrated = legacy.orElseThrow();
			storage.set(TYPE, migrated);
			TwilightForestMod.LOGGER.info("Imported legacy Forge teleporter cache from {}; entries will be shape-validated on first use and the source file was left unchanged", legacyCapabilityFile);
			return migrated;
		}

		return storage.computeIfAbsent(TYPE);
	}

	static Optional<TeleporterCache> readLegacySavedData(SavedDataStorage storage, Path legacyFile) {
		if (!Files.exists(legacyFile)) {
			return Optional.empty();
		}
		if (!Files.isRegularFile(legacyFile)) {
			throw new IllegalStateException("Legacy flat teleporter-cache path is not a regular file: " + legacyFile);
		}

		try {
			CompoundTag fileTag = storage.readTagFromDisk(legacyFile, DataFixTypes.SAVED_DATA_COMMAND_STORAGE,
				SharedConstants.getCurrentVersion().dataVersion().version());
			Tag dataTag = fileTag.get("data");
			if (!(dataTag instanceof CompoundTag data)) {
				throw new IllegalArgumentException("Legacy flat teleporter cache has no compound data root");
			}
			return Optional.of(load(data));
		} catch (IOException | RuntimeException exception) {
			throw new IllegalStateException("Failed to import legacy flat teleporter cache from " + legacyFile + "; the source file was left unchanged", exception);
		}
	}

	static Optional<TeleporterCache> readLegacyCapability(SavedDataStorage storage, Path legacyFile) {
		if (!Files.exists(legacyFile)) {
			return Optional.empty();
		}
		if (!Files.isRegularFile(legacyFile)) {
			throw new IllegalStateException("Legacy Forge capability path is not a regular file: " + legacyFile);
		}

		try {
			CompoundTag fileTag = storage.readTagFromDisk(legacyFile, DataFixTypes.SAVED_DATA_COMMAND_STORAGE,
				SharedConstants.getCurrentVersion().dataVersion().version());
			return decodeLegacyCapabilityFile(fileTag);
		} catch (IOException | RuntimeException exception) {
			throw new IllegalStateException("Failed to import legacy Forge teleporter cache from " + legacyFile + "; the source file was left unchanged", exception);
		}
	}

	static Optional<TeleporterCache> decodeLegacyCapabilityFile(CompoundTag fileTag) {
		Tag dataTag = fileTag.get("data");
		if (!(dataTag instanceof CompoundTag data)) {
			throw new IllegalArgumentException("Legacy Forge capability file has no compound data root");
		}

		Tag providerTag = data.get(LEGACY_PROVIDER);
		if (providerTag == null) {
			return Optional.empty();
		}
		if (!(providerTag instanceof CompoundTag provider)) {
			throw new IllegalArgumentException("Legacy teleporter-cache provider payload is not a compound");
		}

		return Optional.of(load(provider));
	}

	public synchronized void registerPortal(PortalEndpoint endpoint) {
		Set<PortalEndpoint> indexed = this.portalIndex.computeIfAbsent(endpoint.dimension(), ignored -> new HashSet<>());
		List<PortalEndpoint> stale = indexed.stream()
			.filter(existing -> existing.anchor().equals(endpoint.anchor()) && !existing.equals(endpoint))
			.toList();
		stale.forEach(this::invalidateInternal);
		this.portalIndex.computeIfAbsent(endpoint.dimension(), ignored -> new HashSet<>()).add(endpoint);
		this.setDirty();
	}

	/** Links two exact shapes without stealing either end from an existing pair. */
	public synchronized boolean link(PortalEndpoint first, PortalEndpoint second) {
		if (first.equals(second)) {
			return false;
		}
		this.registerPortal(first);
		this.registerPortal(second);
		PortalEndpoint firstTarget = this.links.get(first);
		PortalEndpoint secondTarget = this.links.get(second);
		if (second.equals(firstTarget) && first.equals(secondTarget)) {
			return true;
		}
		if (firstTarget != null || secondTarget != null) {
			return false;
		}
		this.links.put(first, second);
		this.links.put(second, first);
		this.setDirty();
		return true;
	}

	public synchronized @Nullable PortalEndpoint getLinkedPortal(PortalEndpoint source, Identifier destinationDimension) {
		PortalEndpoint target = this.links.get(source);
		return target != null && target.dimension().equals(destinationDimension) ? target : null;
	}

	public synchronized @Nullable PortalEndpoint getLinkedPortal(PortalEndpoint source) {
		return this.links.get(source);
	}

	public synchronized boolean isAvailableFor(PortalEndpoint candidate, PortalEndpoint source) {
		PortalEndpoint linked = this.links.get(candidate);
		return linked == null || linked.equals(source);
	}

	public synchronized List<PortalEndpoint> nearestIndexedPortals(Identifier dimension, BlockPos pos, int radius) {
		long maxDistance = (long) radius * radius;
		return this.portalIndex.getOrDefault(dimension, Set.of()).stream()
			.filter(endpoint -> horizontalDistanceSquared(endpoint.anchor(), pos) <= maxDistance)
			.sorted(Comparator
				.comparingDouble((PortalEndpoint endpoint) -> endpoint.anchor().distSqr(pos))
				.thenComparing(ENDPOINT_ORDER))
			.toList();
	}

	public synchronized void invalidate(PortalEndpoint endpoint) {
		this.invalidateInternal(endpoint);
		this.setDirty();
	}

	public synchronized void invalidateContaining(Identifier dimension, BlockPos pos) {
		List<PortalEndpoint> invalid = this.portalIndex.getOrDefault(dimension, Set.of()).stream()
			.filter(endpoint -> endpoint.mayContain(pos))
			.toList();
		invalid.forEach(this::invalidateInternal);
		if (!invalid.isEmpty()) {
			this.setDirty();
		}
	}

	private void invalidateInternal(PortalEndpoint endpoint) {
		PortalEndpoint reverse = this.links.remove(endpoint);
		if (reverse != null) {
			this.links.remove(reverse);
		}
		Set<PortalEndpoint> indexed = this.portalIndex.get(endpoint.dimension());
		if (indexed != null) {
			indexed.remove(endpoint);
			if (indexed.isEmpty()) {
				this.portalIndex.remove(endpoint.dimension());
			}
		}
	}

	public synchronized @Nullable TFTeleporter.PortalPosition consumeLegacyHint(Identifier destinationDimension, Collection<ColumnPos> sourceColumns) {
		Map<ColumnPos, TFTeleporter.PortalPosition> hints = this.legacyHints.get(destinationDimension);
		if (hints == null) {
			return null;
		}
		TFTeleporter.PortalPosition found = null;
		for (ColumnPos column : sourceColumns) {
			TFTeleporter.PortalPosition candidate = hints.remove(column);
			if (found == null && candidate != null) {
				found = candidate;
			}
		}
		if (hints.isEmpty()) {
			this.legacyHints.remove(destinationDimension);
		}
		if (found != null) {
			this.setDirty();
		}
		return found;
	}

	// Kept for version-1 migration tests and old import code. Runtime exact links do not use it.
	public synchronized void addBlockToCache(Identifier dimension, ColumnPos columnPos, TFTeleporter.PortalPosition position) {
		this.legacyHints.computeIfAbsent(dimension, ignored -> new HashMap<>()).put(columnPos, position);
		this.setDirty();
	}

	@Nullable
	public synchronized TFTeleporter.PortalPosition getPortalPosition(Identifier dimension, ColumnPos pos) {
		return this.legacyHints.getOrDefault(dimension, Map.of()).get(pos);
	}

	public synchronized void removeInvalidPos(Identifier dimension, ColumnPos pos) {
		Map<ColumnPos, TFTeleporter.PortalPosition> hints = this.legacyHints.get(dimension);
		if (hints != null && hints.remove(pos) != null) {
			if (hints.isEmpty()) {
				this.legacyHints.remove(dimension);
			}
			this.setDirty();
		}
	}

	public synchronized CompoundTag save(CompoundTag tag) {
		tag.putInt("version", FORMAT_VERSION);
		ListTag portals = new ListTag();
		this.portalIndex.values().stream().flatMap(Set::stream).sorted(ENDPOINT_ORDER)
			.forEach(endpoint -> portals.add(writeEndpoint(endpoint)));
		tag.put("portals", portals);

		ListTag serializedLinks = new ListTag();
		this.links.entrySet().stream()
			.filter(entry -> ENDPOINT_ORDER.compare(entry.getKey(), entry.getValue()) < 0)
			.sorted(Map.Entry.comparingByKey(ENDPOINT_ORDER))
			.forEach(entry -> {
				CompoundTag pair = new CompoundTag();
				pair.put("first", writeEndpoint(entry.getKey()));
				pair.put("second", writeEndpoint(entry.getValue()));
				serializedLinks.add(pair);
			});
		tag.put("links", serializedLinks);
		writeLegacyHints(tag);
		return tag;
	}

	private void writeLegacyHints(CompoundTag tag) {
		ListTag destinations = new ListTag();
		this.legacyHints.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
			.forEach(entry -> {
				CompoundTag destination = new CompoundTag();
				destination.putString("name", entry.getKey().toString());
				ListTag hints = new ListTag();
				entry.getValue().entrySet().stream()
					.sorted(Comparator.comparingInt((Map.Entry<ColumnPos, TFTeleporter.PortalPosition> hint) -> hint.getKey().x())
						.thenComparingInt(hint -> hint.getKey().z()))
					.forEach(hint -> {
						CompoundTag link = new CompoundTag();
						CompoundTag column = new CompoundTag();
						column.putInt("x", hint.getKey().x());
						column.putInt("z", hint.getKey().z());
						link.put("column", column);
						CompoundTag portal = new CompoundTag();
						portal.putLong("time", hint.getValue().lastUpdateTime);
						portal.putLong("pos", hint.getValue().pos.asLong());
						link.put("portal", portal);
						hints.add(link);
					});
				destination.put("links", hints);
				destinations.add(destination);
			});
		tag.put("dest", destinations);
	}

	public static TeleporterCache load(CompoundTag tag) {
		TeleporterCache cache = new TeleporterCache();
		int version = tag.getInt("version").orElse(1);
		if (version > FORMAT_VERSION) {
			throw new IllegalArgumentException("Unsupported teleporter-cache version " + version);
		}
		if (version >= 2) {
			for (Tag portalTag : requiredList(tag, "portals")) {
				if (!(portalTag instanceof CompoundTag portal)) {
					throw new IllegalArgumentException("Teleporter cache contains a non-compound portal");
				}
				cache.registerPortal(readEndpoint(portal));
			}
			for (Tag pairTag : requiredList(tag, "links")) {
				if (!(pairTag instanceof CompoundTag pair)) {
					throw new IllegalArgumentException("Teleporter cache contains a non-compound exact link");
				}
				PortalEndpoint first = readEndpoint(requiredCompound(pair, "first"));
				PortalEndpoint second = readEndpoint(requiredCompound(pair, "second"));
				if (!cache.link(first, second)) {
					throw new IllegalArgumentException("Teleporter cache contains conflicting exact links");
				}
			}
		}
		readLegacyHints(tag, cache);
		return cache;
	}

	private static void readLegacyHints(CompoundTag tag, TeleporterCache cache) {
		ListTag destinations = requiredList(tag, "dest");
		for (Tag destinationTag : destinations) {
			if (!(destinationTag instanceof CompoundTag destination)) {
				throw new IllegalArgumentException("Teleporter cache contains a non-compound destination");
			}
			String destinationName = destination.getString("name")
				.orElseThrow(() -> new IllegalArgumentException("Teleporter-cache destination has no name"));
			Identifier name = Identifier.parse(destinationName);
			for (Tag linkTag : requiredList(destination, "links")) {
				if (!(linkTag instanceof CompoundTag link)) {
					throw new IllegalArgumentException("Teleporter cache contains a non-compound legacy link");
				}
				CompoundTag column = requiredCompound(link, "column");
				CompoundTag portal = requiredCompound(link, "portal");
				int columnX = column.getInt("x").orElseThrow(() -> new IllegalArgumentException("Teleporter-cache column has no x"));
				int columnZ = column.getInt("z").orElseThrow(() -> new IllegalArgumentException("Teleporter-cache column has no z"));
				long portalPosition = portal.getLong("pos").orElseThrow(() -> new IllegalArgumentException("Teleporter-cache portal has no pos"));
				long portalTime = portal.getLong("time").orElseThrow(() -> new IllegalArgumentException("Teleporter-cache portal has no time"));
				cache.addBlockToCache(name, new ColumnPos(columnX, columnZ),
					new TFTeleporter.PortalPosition(BlockPos.of(portalPosition), portalTime));
			}
		}
	}

	private static CompoundTag writeEndpoint(PortalEndpoint endpoint) {
		CompoundTag tag = new CompoundTag();
		tag.putString("dimension", endpoint.dimension().toString());
		tag.putLong("anchor", endpoint.anchor().asLong());
		tag.putInt("count", endpoint.blockCount());
		tag.putLong("fingerprint", endpoint.fingerprint());
		tag.putLong("min", endpoint.min().asLong());
		tag.putLong("max", endpoint.max().asLong());
		return tag;
	}

	private static PortalEndpoint readEndpoint(CompoundTag tag) {
		String dimension = tag.getString("dimension")
			.orElseThrow(() -> new IllegalArgumentException("Teleporter endpoint has no dimension"));
		long anchor = tag.getLong("anchor").orElseThrow(() -> new IllegalArgumentException("Teleporter endpoint has no anchor"));
		int count = tag.getInt("count").orElseThrow(() -> new IllegalArgumentException("Teleporter endpoint has no count"));
		long fingerprint = tag.getLong("fingerprint").orElseThrow(() -> new IllegalArgumentException("Teleporter endpoint has no fingerprint"));
		long min = tag.getLong("min").orElseThrow(() -> new IllegalArgumentException("Teleporter endpoint has no min bounds"));
		long max = tag.getLong("max").orElseThrow(() -> new IllegalArgumentException("Teleporter endpoint has no max bounds"));
		return new PortalEndpoint(Identifier.parse(dimension), BlockPos.of(anchor), count, fingerprint, BlockPos.of(min), BlockPos.of(max));
	}

	private static ListTag requiredList(CompoundTag tag, String name) {
		return tag.getList(name).orElseThrow(() -> new IllegalArgumentException("Teleporter cache has no " + name + " list"));
	}

	private static CompoundTag requiredCompound(CompoundTag tag, String name) {
		return tag.getCompound(name).orElseThrow(() -> new IllegalArgumentException("Teleporter-cache entry has no " + name));
	}

	private static long horizontalDistanceSquared(BlockPos first, BlockPos second) {
		long x = (long) first.getX() - second.getX();
		long z = (long) first.getZ() - second.getZ();
		return x * x + z * z;
	}

	public record PortalEndpoint(Identifier dimension, BlockPos anchor, int blockCount, long fingerprint, BlockPos min, BlockPos max) {
		public PortalEndpoint {
			dimension = java.util.Objects.requireNonNull(dimension, "dimension");
			anchor = java.util.Objects.requireNonNull(anchor, "anchor").immutable();
			min = java.util.Objects.requireNonNull(min, "min").immutable();
			max = java.util.Objects.requireNonNull(max, "max").immutable();
			if (blockCount <= 0 || blockCount > 4096) {
				throw new IllegalArgumentException("Portal endpoint block count must be between 1 and 4096");
			}
			if (min.getX() > max.getX() || min.getY() > max.getY() || min.getZ() > max.getZ()) {
				throw new IllegalArgumentException("Portal endpoint bounds are inverted");
			}
			if (!within(anchor, min, max)) {
				throw new IllegalArgumentException("Portal endpoint anchor is outside its bounds");
			}
			long width = (long) max.getX() - min.getX() + 1;
			long depth = (long) max.getZ() - min.getZ() + 1;
			if (min.getY() != max.getY() || width > blockCount || depth > blockCount
				|| width + depth - 1 > blockCount) {
				throw new IllegalArgumentException("Portal endpoint bounds exceed its connected block count");
			}
			long chunkWidth = ((max.getX() + 1L) >> 4) - ((min.getX() - 1L) >> 4) + 1;
			long chunkDepth = ((max.getZ() + 1L) >> 4) - ((min.getZ() - 1L) >> 4) + 1;
			if (chunkWidth * chunkDepth > 64) {
				throw new IllegalArgumentException("Portal endpoint spans too many chunks");
			}
		}

		boolean mayContain(BlockPos pos) {
			return within(pos, this.min, this.max);
		}

		private static boolean within(BlockPos pos, BlockPos min, BlockPos max) {
			return pos.getX() >= min.getX() && pos.getX() <= max.getX()
				&& pos.getY() >= min.getY() && pos.getY() <= max.getY()
				&& pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
		}
	}
}
