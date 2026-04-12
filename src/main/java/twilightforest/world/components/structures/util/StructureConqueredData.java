package twilightforest.world.components.structures.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StructureConqueredData extends SavedData {
	private static final Codec<StructureConqueredData> CODEC = CompoundTag.CODEC.xmap(StructureConqueredData::load, data -> data.save(new CompoundTag()));
	private static final SavedDataType<StructureConqueredData> TYPE = new SavedDataType<>(net.minecraft.resources.Identifier.withDefaultNamespace("twilightforest_structure_conquered"), StructureConqueredData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private final Map<StructureKey, Boolean> conquered = new HashMap<>();

	private StructureConqueredData() {
	}

	public static StructureConqueredData get(ServerLevel level) {
		ServerLevel server = level.getServer().overworld();
		SavedDataStorage storage = server.getDataStorage();
		return storage.computeIfAbsent(TYPE);
	}

	public boolean isConquered(ResourceKey<Structure> structureKey, ChunkPos chunkPos) {
		return this.conquered.getOrDefault(new StructureKey(structureKey, chunkPos.x(), chunkPos.z()), false);
	}

	public void setConquered(ResourceKey<Structure> structureKey, ChunkPos chunkPos, boolean value) {
		StructureKey key = new StructureKey(structureKey, chunkPos.x(), chunkPos.z());
		Boolean previous = this.conquered.put(key, value);
		if (!Objects.equals(previous, value)) {
			this.setDirty();
		}
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
			CompoundTag entryTag = (CompoundTag) element;
			Identifier structureId = Identifier.tryParse(entryTag.getStringOr("structure", ""));
			if (structureId == null) {
				continue;
			}
			ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE, structureId);
			int chunkX = entryTag.getIntOr("chunk_x", 0);
			int chunkZ = entryTag.getIntOr("chunk_z", 0);
			boolean conquered = entryTag.getBooleanOr("conquered", false);
			data.conquered.put(new StructureKey(structureKey, chunkX, chunkZ), conquered);
		}
		return data;
	}

	private record StructureKey(ResourceKey<Structure> structureKey, int chunkX, int chunkZ) {
	}
}
