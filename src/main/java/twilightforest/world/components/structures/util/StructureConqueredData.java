package twilightforest.world.components.structures.util;

import com.mojang.serialization.Codec;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.SavedDataStorage;
import twilightforest.TwilightForestMod;
import twilightforest.mixin.accessor.SavedDataStorageAccessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class StructureConqueredData extends SavedData {
	private static final Codec<StructureConqueredData> CODEC = CompoundTag.CODEC.xmap(StructureConqueredData::load, data -> data.save(new CompoundTag()));
	private static final SavedDataType<StructureConqueredData> TYPE = new SavedDataType<>(net.minecraft.resources.Identifier.withDefaultNamespace("twilightforest_structure_conquered"), StructureConqueredData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
	private static final String LEGACY_FLAT_FILE = "twilightforest_structure_conquered.dat";

	private final Map<StructureKey, Boolean> conquered = new HashMap<>();

	private StructureConqueredData() {
	}

	public static StructureConqueredData get(ServerLevel level) {
		ServerLevel server = level.getServer().overworld();
		SavedDataStorage storage = server.getDataStorage();
		Path currentDataFolder = ((SavedDataStorageAccessor) storage).twilightforest$getDataFolder();
		return get(storage, currentDataFolder, level.getServer().getWorldPath(LevelResource.DATA));
	}

	static StructureConqueredData get(SavedDataStorage storage, Path currentDataFolder, Path legacyWorldDataFolder) {
		StructureConqueredData current = storage.get(TYPE);
		if (current != null) {
			return current;
		}

		Path currentFile = TYPE.id().withSuffix(".dat").resolveAgainst(currentDataFolder);
		if (Files.exists(currentFile)) {
			throw new IllegalStateException("Current structure-conquered data exists but could not be decoded: " + currentFile);
		}

		LinkedHashSet<Path> candidates = new LinkedHashSet<>();
		candidates.add(currentDataFolder.resolve(LEGACY_FLAT_FILE));
		candidates.add(legacyWorldDataFolder.resolve(LEGACY_FLAT_FILE));
		StructureConqueredData migrated = null;
		List<Path> importedFiles = new ArrayList<>();
		for (Path candidate : candidates) {
			Optional<StructureConqueredData> legacy = readLegacyFile(storage, candidate);
			if (legacy.isEmpty()) {
				continue;
			}
			if (migrated == null) {
				migrated = legacy.orElseThrow();
			} else {
				migrated.merge(legacy.orElseThrow(), candidate);
			}
			importedFiles.add(candidate);
		}

		if (migrated != null) {
			storage.set(TYPE, migrated);
			TwilightForestMod.LOGGER.info("Imported legacy flat structure-conquered data from {}; source files were left unchanged", importedFiles);
			return migrated;
		}
		return storage.computeIfAbsent(TYPE);
	}

	public boolean isConquered(ResourceKey<Structure> structureKey, ChunkPos chunkPos) {
		return this.conquered.getOrDefault(new StructureKey(structureKey, chunkPos.x(), chunkPos.z()), false);
	}

	public boolean isConquered(ServerLevel level, ResourceKey<Structure> structureKey, StructureStart start) {
		StructureKey key = new StructureKey(structureKey, start.getChunkPos().x(), start.getChunkPos().z());
		Object mixedStart = start;
		LegacyConqueredStructureStart legacy = mixedStart instanceof LegacyConqueredStructureStart value ? value : null;
		if (this.conquered.containsKey(key)) {
			if (legacy != null && legacy.twilightforest$getLegacyConquered() != null) {
				clearLegacyField(level, start, legacy);
			}
			return this.conquered.get(key);
		}

		Boolean legacyValue = legacy == null ? null : legacy.twilightforest$getLegacyConquered();
		if (legacyValue != null) {
			boolean imported = !this.conquered.containsKey(key);
			boolean value = this.importLegacy(structureKey, start.getChunkPos(), legacyValue);
			clearLegacyField(level, start, legacy);
			if (imported) {
				TwilightForestMod.LOGGER.info("Imported Forge-era conquered state for structure {} at chunk [{}, {}]", structureKey.identifier(), key.chunkX(), key.chunkZ());
			}
			return value;
		}
		return false;
	}

	boolean importLegacy(ResourceKey<Structure> structureKey, ChunkPos chunkPos, boolean legacyValue) {
		StructureKey key = new StructureKey(structureKey, chunkPos.x(), chunkPos.z());
		Boolean current = this.conquered.putIfAbsent(key, legacyValue);
		if (current == null) {
			this.setDirty();
			return legacyValue;
		}
		return current;
	}

	public void setConquered(ResourceKey<Structure> structureKey, ChunkPos chunkPos, boolean value) {
		StructureKey key = new StructureKey(structureKey, chunkPos.x(), chunkPos.z());
		Boolean previous = this.conquered.put(key, value);
		if (!Objects.equals(previous, value)) {
			this.setDirty();
		}
	}

	public void setConquered(ServerLevel level, ResourceKey<Structure> structureKey, StructureStart start, boolean value) {
		Object mixedStart = start;
		if (mixedStart instanceof LegacyConqueredStructureStart legacy && legacy.twilightforest$getLegacyConquered() != null) {
			clearLegacyField(level, start, legacy);
		}
		this.setConquered(structureKey, start.getChunkPos(), value);
	}

	private CompoundTag save(CompoundTag tag) {
		ListTag entries = new ListTag();
		for (Map.Entry<StructureKey, Boolean> entry : this.conquered.entrySet()) {
			CompoundTag entryTag = new CompoundTag();
			entryTag.putString("structure", entry.getKey().structureKey().identifier().toString());
			entryTag.putInt("chunk_x", entry.getKey().chunkX());
			entryTag.putInt("chunk_z", entry.getKey().chunkZ());
			entryTag.putBoolean("conquered", entry.getValue());
			entries.add(entryTag);
		}
		tag.put("entries", entries);
		return tag;
	}

	private static StructureConqueredData load(CompoundTag tag) {
		StructureConqueredData data = new StructureConqueredData();
		for (Tag element : tag.getListOrEmpty("entries")) {
			if (!(element instanceof CompoundTag entryTag)) {
				throw new IllegalArgumentException("Structure-conquered data contains a non-compound entry");
			}
			String structureName = entryTag.getString("structure")
				.orElseThrow(() -> new IllegalArgumentException("Structure-conquered entry has no structure ID"));
			Identifier structureId = Identifier.parse(structureName);
			ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE, structureId);
			int chunkX = entryTag.getInt("chunk_x")
				.orElseThrow(() -> new IllegalArgumentException("Structure-conquered entry has no chunk_x"));
			int chunkZ = entryTag.getInt("chunk_z")
				.orElseThrow(() -> new IllegalArgumentException("Structure-conquered entry has no chunk_z"));
			boolean conquered = entryTag.getBoolean("conquered")
				.orElseThrow(() -> new IllegalArgumentException("Structure-conquered entry has no conquered flag"));
			data.conquered.put(new StructureKey(structureKey, chunkX, chunkZ), conquered);
		}
		return data;
	}

	private static Optional<StructureConqueredData> readLegacyFile(SavedDataStorage storage, Path legacyFile) {
		if (!Files.exists(legacyFile)) {
			return Optional.empty();
		}
		if (!Files.isRegularFile(legacyFile)) {
			throw new IllegalStateException("Legacy structure-conquered path is not a regular file: " + legacyFile);
		}
		try {
			CompoundTag root = storage.readTagFromDisk(
				legacyFile,
				DataFixTypes.SAVED_DATA_COMMAND_STORAGE,
				SharedConstants.getCurrentVersion().dataVersion().version());
			Tag dataTag = root.get("data");
			if (!(dataTag instanceof CompoundTag data)) {
				throw new IllegalArgumentException("Legacy structure-conquered file has no compound data root");
			}
			return Optional.of(load(data));
		} catch (IOException | RuntimeException exception) {
			throw new IllegalStateException("Failed to import legacy structure-conquered data from " + legacyFile + "; the source file was left unchanged", exception);
		}
	}

	private void merge(StructureConqueredData other, Path source) {
		for (Map.Entry<StructureKey, Boolean> entry : other.conquered.entrySet()) {
			Boolean existing = this.conquered.putIfAbsent(entry.getKey(), entry.getValue());
			if (existing != null && !existing.equals(entry.getValue())) {
				throw new IllegalStateException("Conflicting legacy conquered values for " + entry.getKey() + " in " + source);
			}
		}
	}

	private static void clearLegacyField(ServerLevel level, StructureStart start, LegacyConqueredStructureStart legacy) {
		legacy.twilightforest$setLegacyConquered(null);
		ChunkPos chunkPos = start.getChunkPos();
		LevelChunk chunk = level.getChunk(chunkPos.x(), chunkPos.z());
		chunk.markUnsaved();
	}

	private record StructureKey(ResourceKey<Structure> structureKey, int chunkX, int chunkZ) {
	}
}
