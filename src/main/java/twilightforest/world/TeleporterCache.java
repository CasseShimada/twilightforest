package twilightforest.world;

import com.google.common.collect.Maps;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TeleporterCache extends SavedData {

	private static final Codec<TeleporterCache> CODEC = CompoundTag.CODEC.xmap(TeleporterCache::load, cache -> cache.save(new CompoundTag()));
	private static final SavedDataType<TeleporterCache> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("twilightforest", "teleporter_cache"), TeleporterCache::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
	private static final String LEGACY_SAVED_DATA_FILE = "twilightforest_teleporter_cache.dat";
	private static final String LEGACY_CAPABILITY_FILE = "capabilities.dat";
	private static final String LEGACY_PROVIDER = "twilightforest:teleporter_cache";

	// destinationCoordinateCache is (src -> dest) [DestWorld, [SrcPos, DestPos]]
	private final Map<Identifier, Map<ColumnPos, TFTeleporter.PortalPosition>> destinationCoordinateCache = new HashMap<>();

	private TeleporterCache() {
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
			TwilightForestMod.LOGGER.info("Imported legacy flat teleporter cache from {}; the source file was left unchanged", legacySavedDataFile);
			return migrated;
		}

		Path legacyCapabilityFile = legacyDataFolder.resolve(LEGACY_CAPABILITY_FILE);
		legacy = readLegacyCapability(storage, legacyCapabilityFile);
		if (legacy.isPresent()) {
			TeleporterCache migrated = legacy.orElseThrow();
			storage.set(TYPE, migrated);
			TwilightForestMod.LOGGER.info("Imported legacy Forge teleporter cache from {}; the source file was left unchanged", legacyCapabilityFile);
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
			CompoundTag fileTag = storage.readTagFromDisk(
				legacyFile,
				DataFixTypes.SAVED_DATA_COMMAND_STORAGE,
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
			CompoundTag fileTag = storage.readTagFromDisk(
				legacyFile,
				DataFixTypes.SAVED_DATA_COMMAND_STORAGE,
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

	public void addBlockToCache(Identifier dimension, ColumnPos columnPos, TFTeleporter.PortalPosition position) {
		this.destinationCoordinateCache.putIfAbsent(dimension, Maps.newHashMapWithExpectedSize(4096));
		this.destinationCoordinateCache.get(dimension).put(columnPos, position);
		this.setDirty();
	}

	@Nullable
	public TFTeleporter.PortalPosition getPortalPosition(Identifier dimension, ColumnPos pos) {
		if (this.destinationCoordinateCache.containsKey(dimension)) {
			return this.destinationCoordinateCache.get(dimension).get(pos);
		}
		return null;
	}

	public void removeInvalidPos(Identifier dimension, ColumnPos pos) {
		this.destinationCoordinateCache.get(dimension).remove(pos);
		this.setDirty();
	}

	public CompoundTag save(CompoundTag tag) {
		ListTag dcc = new ListTag();
		this.destinationCoordinateCache.forEach((rl, map) -> {
			CompoundTag ct = new CompoundTag();
			ListTag links = new ListTag();
			map.forEach((columnPos, portalPos) -> {
				CompoundTag link = new CompoundTag();
				CompoundTag column = new CompoundTag();
				column.putInt("x", columnPos.x());
				column.putInt("z", columnPos.z());
				link.put("column", column);
				CompoundTag portal = new CompoundTag();
				portal.putLong("time", portalPos.lastUpdateTime);
				portal.putLong("pos", portalPos.pos.asLong());
				link.put("portal", portal);
				links.add(link);
			});
			ct.put("links", links);
			ct.putString("name", rl.toString());
			dcc.add(ct);
		});
		tag.put("dest", dcc);
		return tag;
	}

	public static TeleporterCache load(CompoundTag tag) {
		TeleporterCache cache = new TeleporterCache();
		ListTag destinations = tag.getList("dest")
			.orElseThrow(() -> new IllegalArgumentException("Teleporter cache has no dest list"));
		for (Tag destinationTag : destinations) {
			if (!(destinationTag instanceof CompoundTag destination)) {
				throw new IllegalArgumentException("Teleporter cache contains a non-compound destination");
			}
			String destinationName = destination.getString("name")
				.orElseThrow(() -> new IllegalArgumentException("Teleporter-cache destination has no name"));
			Identifier name = Identifier.parse(destinationName);
			cache.destinationCoordinateCache.putIfAbsent(name, Maps.newHashMapWithExpectedSize(4096));
			ListTag links = destination.getList("links")
				.orElseThrow(() -> new IllegalArgumentException("Teleporter-cache destination has no links list"));
			for (Tag linkTag : links) {
				if (!(linkTag instanceof CompoundTag link)) {
					throw new IllegalArgumentException("Teleporter cache contains a non-compound link");
				}
				CompoundTag column = link.getCompound("column")
					.orElseThrow(() -> new IllegalArgumentException("Teleporter-cache link has no column"));
				CompoundTag portal = link.getCompound("portal")
					.orElseThrow(() -> new IllegalArgumentException("Teleporter-cache link has no portal"));
				int columnX = column.getInt("x")
					.orElseThrow(() -> new IllegalArgumentException("Teleporter-cache column has no x"));
				int columnZ = column.getInt("z")
					.orElseThrow(() -> new IllegalArgumentException("Teleporter-cache column has no z"));
				long portalPosition = portal.getLong("pos")
					.orElseThrow(() -> new IllegalArgumentException("Teleporter-cache portal has no pos"));
				long portalTime = portal.getLong("time")
					.orElseThrow(() -> new IllegalArgumentException("Teleporter-cache portal has no time"));
				cache.destinationCoordinateCache.get(name).put(
					new ColumnPos(columnX, columnZ),
					new TFTeleporter.PortalPosition(BlockPos.of(portalPosition), portalTime));
			}
		}
		return cache;
	}
}
