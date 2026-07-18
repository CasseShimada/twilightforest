package twilightforest.item.mapdata;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import twilightforest.init.TFMapDecorations;
import twilightforest.item.MagicMapItem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyMapSavedDataTests {
	private static final byte[] LEGACY_FEATURES = new byte[]{-123, 16, -8, 13, -32, 24, 99, 1, 2};

	@TempDir
	Path temporaryDirectory;

	@Test
	void importsFlatMagicAndMazeDataPreservesPixelsAndMarkersAndRestartsFromCurrentFiles() throws Exception {
		Path legacyDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("data"));
		Path currentDataFolder = Files.createDirectories(this.temporaryDirectory
			.resolve("dimensions").resolve("minecraft").resolve("overworld").resolve("data"));
		Path magicFile = legacyDataFolder.resolve("magicmap_42.dat");
		Path mazeFile = legacyDataFolder.resolve("mazemap_43.dat");

		CompoundTag magicRoot = fixtureRoot("magic");
		byte[] magicColors = colors((byte) 17, (byte) 91);
		magicRoot.getCompoundOrEmpty("data").putByteArray("colors", magicColors);
		NbtIo.writeCompressed(magicRoot, magicFile);
		CompoundTag mazeRoot = fixtureRoot("maze");
		byte[] mazeColors = colors((byte) 23, (byte) 101);
		mazeRoot.getCompoundOrEmpty("data").putByteArray("colors", mazeColors);
		NbtIo.writeCompressed(mazeRoot, mazeFile);
		byte[] originalMagicBytes = Files.readAllBytes(magicFile);
		byte[] originalMazeBytes = Files.readAllBytes(mazeFile);

		try (SavedDataStorage storage = storage(currentDataFolder)) {
			TFMagicMapData magic = TFMagicMapData.getMagicMapData(
				storage, currentDataFolder, legacyDataFolder, RegistryAccess.EMPTY, "magicmap_42");
			TFMazeMapData maze = TFMazeMapData.getMazeMapData(
				storage, currentDataFolder, legacyDataFolder, RegistryAccess.EMPTY, "mazemap_43");
			assertNotNull(magic);
			assertNotNull(maze);
			assertEquals(1024, magic.centerX);
			assertEquals(-1024, magic.centerZ);
			assertArrayEquals(magicColors, magic.colors);
			assertEquals(31, maze.yCenter);
			assertTrue(maze.ore);
			assertArrayEquals(mazeColors, maze.colors);
			assertLegacyDecorations(magic);
			storage.saveAndJoin();
		}

		Path currentMagicFile = currentDataFolder.resolve("minecraft").resolve("magicmap_42.dat");
		Path currentMazeFile = currentDataFolder.resolve("minecraft").resolve("mazemap_43.dat");
		assertTrue(Files.isRegularFile(currentMagicFile));
		assertTrue(Files.isRegularFile(currentMazeFile));
		assertArrayEquals(originalMagicBytes, Files.readAllBytes(magicFile));
		assertArrayEquals(originalMazeBytes, Files.readAllBytes(mazeFile));

		CompoundTag savedMagic = readCompressed(currentMagicFile).getCompoundOrEmpty("data");
		assertArrayEquals(LEGACY_FEATURES, savedMagic.getByteArray("features").orElseThrow());
		assertEquals(2, savedMagic.getListOrEmpty("decorations").size());
		assertEquals("twilightforest:naga_courtyard_1088_-1056",
			savedMagic.getListOrEmpty("decorations").getCompoundOrEmpty(0).getStringOr("id", ""));
		assertEquals("twilightforest:labyrinth_896_-928",
			savedMagic.getListOrEmpty("decorations").getCompoundOrEmpty(1).getStringOr("id", ""));

		mazeRoot.getCompoundOrEmpty("data").putInt("yCenter", 99);
		NbtIo.writeCompressed(mazeRoot, mazeFile);
		try (SavedDataStorage restartedStorage = storage(currentDataFolder)) {
			TFMagicMapData magic = TFMagicMapData.getMagicMapData(
				restartedStorage, currentDataFolder, legacyDataFolder, RegistryAccess.EMPTY, "magicmap_42");
			TFMazeMapData maze = TFMazeMapData.getMazeMapData(
				restartedStorage, currentDataFolder, legacyDataFolder, RegistryAccess.EMPTY, "mazemap_43");
			assertNotNull(magic);
			assertNotNull(maze);
			assertLegacyDecorations(magic);
			assertArrayEquals(magicColors, magic.colors);
			assertEquals(31, maze.yCenter, "current namespaced data must win over a changed legacy source");
			assertArrayEquals(mazeColors, maze.colors);
		}
	}

	@Test
	void rejectsUndecodableCurrentFileInsteadOfFallingBackToLegacyData() throws Exception {
		Path legacyDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("data"));
		Path currentDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("current"));
		NbtIo.writeCompressed(fixtureRoot("magic"), legacyDataFolder.resolve("magicmap_1.dat"));
		Path currentFile = Files.createDirectories(currentDataFolder.resolve("minecraft")).resolve("magicmap_1.dat");
		NbtIo.writeCompressed(new CompoundTag(), currentFile);

		try (SavedDataStorage storage = storage(currentDataFolder)) {
			IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
				TFMagicMapData.getMagicMapData(storage, currentDataFolder, legacyDataFolder, RegistryAccess.EMPTY, "magicmap_1"));
			assertTrue(exception.getMessage().contains("Current custom map data"));
		}
	}

	@Test
	void rejectsConflictingFlatFilesAcrossTheTwoHistoricalLocations() throws Exception {
		Path legacyDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("data"));
		Path currentDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("current"));
		CompoundTag first = fixtureRoot("maze");
		CompoundTag second = fixtureRoot("maze");
		second.getCompoundOrEmpty("data").putInt("yCenter", 88);
		NbtIo.writeCompressed(first, currentDataFolder.resolve("mazemap_9.dat"));
		NbtIo.writeCompressed(second, legacyDataFolder.resolve("mazemap_9.dat"));

		try (SavedDataStorage storage = storage(currentDataFolder)) {
			IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
				TFMazeMapData.getMazeMapData(storage, currentDataFolder, legacyDataFolder, RegistryAccess.EMPTY, "mazemap_9"));
			assertTrue(exception.getMessage().contains("Conflicting legacy custom map data"));
		}
	}

	@Test
	void rejectsMalformedLegacyFeatureTripletsWithoutChangingTheSource() throws Exception {
		Path legacyDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("data"));
		Path currentDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("current"));
		CompoundTag malformed = fixtureRoot("magic");
		malformed.getCompoundOrEmpty("data").putByteArray("features", new byte[]{5, 1});
		Path legacyFile = legacyDataFolder.resolve("magicmap_7.dat");
		NbtIo.writeCompressed(malformed, legacyFile);
		byte[] original = Files.readAllBytes(legacyFile);

		try (SavedDataStorage storage = storage(currentDataFolder)) {
			IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
				TFMagicMapData.getMagicMapData(storage, currentDataFolder, legacyDataFolder, RegistryAccess.EMPTY, "magicmap_7"));
			assertTrue(exception.getMessage().contains("Failed to import legacy custom map data"));
		}
		assertArrayEquals(original, Files.readAllBytes(legacyFile));
		assertFalse(Files.exists(currentDataFolder.resolve("minecraft").resolve("magicmap_7.dat")));
	}

	private static void assertLegacyDecorations(TFMagicMapData magic) {
		List<MapDecoration> decorations = new ArrayList<>();
		magic.getDecorations().forEach(decorations::add);
		assertEquals(2, decorations.size());
		assertEquals(List.of(
			Identifier.parse("twilightforest:naga_courtyard"),
			Identifier.parse("twilightforest:labyrinth")),
			decorations.stream().map(decoration -> BuiltInRegistries.MAP_DECORATION_TYPE.getKey(decoration.type().value())).toList());
		assertEquals(16, decorations.getFirst().x());
		assertEquals(-8, decorations.getFirst().y());
		assertTrue(magic.conqueredStructures.contains(MagicMapItem.makeName(TFMapDecorations.NAGA_COURTYARD, 16, -8)));
	}

	private static byte[] colors(byte first, byte last) {
		byte[] colors = new byte[128 * 128];
		colors[0] = first;
		colors[colors.length - 1] = last;
		return colors;
	}

	private static SavedDataStorage storage(Path dataFolder) {
		return new SavedDataStorage(dataFolder, DataFixers.getDataFixer(), RegistryAccess.EMPTY);
	}

	private static CompoundTag fixtureRoot(String key) throws Exception {
		return TagParser.parseCompoundFully(readFixture()).getCompoundOrEmpty(key);
	}

	private static CompoundTag readCompressed(Path file) throws IOException {
		return NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
	}

	private static String readFixture() throws IOException {
		try (InputStream input = Objects.requireNonNull(LegacyMapSavedDataTests.class.getResourceAsStream(
			"/twilightforest/item/mapdata/legacy-custom-map-saved-data.snbt"))) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
