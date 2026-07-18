package twilightforest.world.components.structures.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureConqueredDataTests {
	private static final ResourceKey<Structure> NAGA = ResourceKey.create(Registries.STRUCTURE, Identifier.parse("twilightforest:naga_courtyard"));
	private static final ResourceKey<Structure> LICH = ResourceKey.create(Registries.STRUCTURE, Identifier.parse("twilightforest:lich_tower"));

	@TempDir
	Path temporaryDirectory;

	@Test
	void importsLegacyFlatSavedDataPreservesSourceAndUsesCurrentFileAfterRestart() throws Exception {
		Path legacyDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("data"));
		Path currentDataFolder = Files.createDirectories(this.temporaryDirectory
			.resolve("dimensions").resolve("minecraft").resolve("overworld").resolve("data"));
		Path legacyFile = legacyDataFolder.resolve("twilightforest_structure_conquered.dat");
		CompoundTag legacyRoot = readFixture();
		NbtIo.writeCompressed(legacyRoot, legacyFile);
		byte[] originalLegacyBytes = Files.readAllBytes(legacyFile);

		try (SavedDataStorage storage = storage(currentDataFolder)) {
			StructureConqueredData imported = StructureConqueredData.get(storage, currentDataFolder, legacyDataFolder);
			assertTrue(imported.isConquered(NAGA, new ChunkPos(7, -9)));
			assertFalse(imported.isConquered(LICH, new ChunkPos(-12, 18)));
			storage.saveAndJoin();
		}

		Path currentFile = currentDataFolder.resolve("minecraft").resolve("twilightforest_structure_conquered.dat");
		assertTrue(Files.isRegularFile(currentFile));
		assertArrayEquals(originalLegacyBytes, Files.readAllBytes(legacyFile));

		legacyRoot.getCompoundOrEmpty("data").getListOrEmpty("entries")
			.getCompoundOrEmpty(0).putBoolean("conquered", false);
		NbtIo.writeCompressed(legacyRoot, legacyFile);
		byte[] changedLegacyBytes = Files.readAllBytes(legacyFile);
		try (SavedDataStorage restartedStorage = storage(currentDataFolder)) {
			StructureConqueredData current = StructureConqueredData.get(restartedStorage, currentDataFolder, legacyDataFolder);
			assertTrue(current.isConquered(NAGA, new ChunkPos(7, -9)));
		}
		assertArrayEquals(changedLegacyBytes, Files.readAllBytes(legacyFile));
	}

	@Test
	void preservesForgeChunkFieldUntilItCanBeImported() {
		CompoundTag oldStart = new CompoundTag();
		oldStart.putBoolean("conquered", true);
		assertTrue(LegacyStructureStartData.read(oldStart).orElseThrow());

		CompoundTag pendingSave = new CompoundTag();
		LegacyStructureStartData.writePending(pendingSave, true);
		assertTrue(pendingSave.getBoolean("conquered").orElseThrow());
		CompoundTag migratedSave = new CompoundTag();
		LegacyStructureStartData.writePending(migratedSave, null);
		assertFalse(migratedSave.contains("conquered"));
	}

	@Test
	void currentConqueredEntryWinsOverAStaleForgeChunkField() throws Exception {
		Path legacyDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("priority-data"));
		Path currentDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("priority-current"));
		try (SavedDataStorage storage = storage(currentDataFolder)) {
			StructureConqueredData data = StructureConqueredData.get(storage, currentDataFolder, legacyDataFolder);
			ChunkPos chunk = new ChunkPos(7, -9);
			data.setConquered(NAGA, chunk, false);
			assertFalse(data.importLegacy(NAGA, chunk, true));
			assertFalse(data.isConquered(NAGA, chunk));
		}
	}

	@Test
	void rejectsMalformedLegacySavedDataAndRequiresTheChunkBridgeMixin() throws Exception {
		CompoundTag malformed = readFixture();
		malformed.getCompoundOrEmpty("data").getListOrEmpty("entries")
			.getCompoundOrEmpty(0).remove("conquered");
		Path legacyDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("malformed-data"));
		Path currentDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("malformed-current"));
		NbtIo.writeCompressed(malformed, legacyDataFolder.resolve("twilightforest_structure_conquered.dat"));
		try (SavedDataStorage storage = storage(currentDataFolder)) {
			assertThrows(IllegalStateException.class,
				() -> StructureConqueredData.get(storage, currentDataFolder, legacyDataFolder));
		}

		String mixinConfig = readResource("/twilightforest.mixins.json");
		String mixinSource = Files.readString(Path.of("src/main/java/twilightforest/mixin/StructureStartMixin.java"));
		String dataSource = Files.readString(Path.of("src/main/java/twilightforest/world/components/structures/util/StructureConqueredData.java"));
		assertTrue(mixinConfig.contains("StructureStartMixin"));
		assertTrue(mixinSource.contains("loadStaticStart"));
		assertTrue(mixinSource.contains("createTag"));
		assertTrue(dataSource.contains("clearLegacyField"));
	}

	private static SavedDataStorage storage(Path dataFolder) {
		return new SavedDataStorage(dataFolder, DataFixers.getDataFixer(), RegistryAccess.EMPTY);
	}

	private static CompoundTag readFixture() throws Exception {
		return TagParser.parseCompoundFully(readResource("/twilightforest/world/legacy-structure-conquered.snbt"));
	}

	private static String readResource(String path) throws IOException {
		try (InputStream input = Objects.requireNonNull(StructureConqueredDataTests.class.getResourceAsStream(path))) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
