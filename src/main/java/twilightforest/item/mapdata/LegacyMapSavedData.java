package twilightforest.item.mapdata;

import com.mojang.serialization.Codec;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
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
import java.util.LinkedHashSet;
import java.util.List;

final class LegacyMapSavedData {
	private LegacyMapSavedData() {
	}

	@Nullable
	static <T extends SavedData> T get(ServerLevel level, String legacyName, SavedDataType<T> type, Codec<T> codec) {
		ServerLevel overworld = level.getServer().overworld();
		SavedDataStorage storage = overworld.getDataStorage();
		SavedDataStorageAccessor accessor = (SavedDataStorageAccessor) storage;
		return get(
			storage,
			accessor.twilightforest$getDataFolder(),
			level.getServer().getWorldPath(LevelResource.DATA),
			accessor.twilightforest$getRegistries(),
			legacyName,
			type,
			codec);
	}

	@Nullable
	static <T extends SavedData> T get(
		SavedDataStorage storage,
		Path currentDataFolder,
		Path legacyWorldDataFolder,
		HolderLookup.Provider registries,
		String legacyName,
		SavedDataType<T> type,
		Codec<T> codec) {
		T current = storage.get(type);
		if (current != null) {
			return current;
		}

		Path currentFile = type.id().withSuffix(".dat").resolveAgainst(currentDataFolder);
		if (Files.exists(currentFile)) {
			throw new IllegalStateException("Current custom map data exists but could not be decoded: " + currentFile);
		}

		String legacyFileName = legacyName + ".dat";
		LinkedHashSet<Path> candidates = new LinkedHashSet<>();
		candidates.add(currentDataFolder.resolve(legacyFileName));
		candidates.add(legacyWorldDataFolder.resolve(legacyFileName));
		List<Path> existing = new ArrayList<>();
		for (Path candidate : candidates) {
			if (!Files.exists(candidate)) {
				continue;
			}
			if (!Files.isRegularFile(candidate)) {
				throw new IllegalStateException("Legacy custom map data path is not a regular file: " + candidate);
			}
			existing.add(candidate);
		}
		if (existing.isEmpty()) {
			return null;
		}

		Path source = existing.getFirst();
		for (int index = 1; index < existing.size(); index++) {
			Path duplicate = existing.get(index);
			try {
				if (Files.mismatch(source, duplicate) != -1L) {
					throw new IllegalStateException("Conflicting legacy custom map data files: " + existing);
				}
			} catch (IOException exception) {
				throw new IllegalStateException("Failed to compare legacy custom map data files: " + existing, exception);
			}
		}

		T migrated = readLegacy(storage, registries, source, type, codec);
		storage.set(type, migrated);
		TwilightForestMod.LOGGER.info("Imported legacy flat custom map data {} from {}; source files were left unchanged", type.id(), existing);
		return migrated;
	}

	private static <T extends SavedData> T readLegacy(
		SavedDataStorage storage,
		HolderLookup.Provider registries,
		Path legacyFile,
		SavedDataType<T> type,
		Codec<T> codec) {
		try {
			CompoundTag root = storage.readTagFromDisk(
				legacyFile,
				type.dataFixType(),
				SharedConstants.getCurrentVersion().dataVersion().version());
			Tag dataTag = root.get("data");
			if (!(dataTag instanceof CompoundTag)) {
				throw new IllegalArgumentException("Legacy custom map file has no compound data root");
			}
			return codec.parse(registries.createSerializationContext(NbtOps.INSTANCE), dataTag).getOrThrow();
		} catch (IOException | RuntimeException exception) {
			throw new IllegalStateException("Failed to import legacy custom map data from " + legacyFile + "; the source file was left unchanged", exception);
		}
	}
}
