package twilightforest.world;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleporterCacheTests {
	private static final Identifier TWILIGHT_FOREST = Identifier.parse("twilightforest:twilight_forest");

	@TempDir
	Path temporaryDirectory;

	@Test
	void importsLegacyForgeCapabilitySavesCanonicalDataAndLeavesSourceUntouched() throws Exception {
		Path legacyDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("data"));
		Path currentDataFolder = Files.createDirectories(this.temporaryDirectory
			.resolve("dimensions").resolve("minecraft").resolve("overworld").resolve("data"));
		Path legacyFile = legacyDataFolder.resolve("capabilities.dat");
		CompoundTag legacyRoot = readForgeFixture();
		NbtIo.writeCompressed(legacyRoot, legacyFile);
		byte[] originalLegacyHash = sha256(legacyFile);

		try (SavedDataStorage storage = storage(currentDataFolder)) {
			TeleporterCache imported = TeleporterCache.get(storage, currentDataFolder, legacyDataFolder);
			assertPortal(imported, 12, -34, 123456789L, 987654321L);
			storage.saveAndJoin();
		}

		Path currentFile = currentDataFolder.resolve("twilightforest").resolve("teleporter_cache.dat");
		assertTrue(Files.isRegularFile(currentFile));
		assertArrayEquals(originalLegacyHash, sha256(legacyFile));

		CompoundTag changedLegacyRoot = readForgeFixture();
		changedLegacyRoot.getCompoundOrEmpty("data")
			.getCompoundOrEmpty("twilightforest:teleporter_cache")
			.getListOrEmpty("dest").getCompoundOrEmpty(0)
			.getListOrEmpty("links").getCompoundOrEmpty(0)
			.getCompoundOrEmpty("portal").putLong("time", 1L);
		NbtIo.writeCompressed(changedLegacyRoot, legacyFile);
		byte[] changedLegacyHash = sha256(legacyFile);

		try (SavedDataStorage restartedStorage = storage(currentDataFolder)) {
			TeleporterCache current = TeleporterCache.get(restartedStorage, currentDataFolder, legacyDataFolder);
			assertPortal(current, 12, -34, 123456789L, 987654321L);
		}
		assertArrayEquals(changedLegacyHash, sha256(legacyFile));
	}

	@Test
	void importsLegacyFlatSavedDataBeforeForgeAndCurrentWinsAfterRestart() throws Exception {
		Path legacyDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("flat-data"));
		Path currentDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("flat-current"));
		Path flatFile = legacyDataFolder.resolve("twilightforest_teleporter_cache.dat");
		Path forgeFile = legacyDataFolder.resolve("capabilities.dat");
		CompoundTag flatRoot = readFlatFixture();
		CompoundTag forgeRoot = readForgeFixture();
		NbtIo.writeCompressed(flatRoot, flatFile);
		NbtIo.writeCompressed(forgeRoot, forgeFile);
		byte[] originalFlatHash = sha256(flatFile);
		byte[] originalForgeHash = sha256(forgeFile);

		try (SavedDataStorage storage = storage(currentDataFolder)) {
			TeleporterCache imported = TeleporterCache.get(storage, currentDataFolder, legacyDataFolder);
			assertPortal(imported, -56, 78, 2233445566L, 1122334455L);
			assertNull(imported.getPortalPosition(TWILIGHT_FOREST, new ColumnPos(12, -34)),
				"the newer flat SavedData source must take priority over Forge capabilities");
			storage.saveAndJoin();
		}

		Path currentFile = currentDataFolder.resolve("twilightforest").resolve("teleporter_cache.dat");
		assertTrue(Files.isRegularFile(currentFile));
		assertArrayEquals(originalFlatHash, sha256(flatFile));
		assertArrayEquals(originalForgeHash, sha256(forgeFile));

		flatRoot.getCompoundOrEmpty("data").getListOrEmpty("dest").getCompoundOrEmpty(0)
			.getListOrEmpty("links").getCompoundOrEmpty(0).getCompoundOrEmpty("portal").putLong("time", 1L);
		forgeRoot.getCompoundOrEmpty("data").getCompoundOrEmpty("twilightforest:teleporter_cache")
			.getListOrEmpty("dest").getCompoundOrEmpty(0).getListOrEmpty("links").getCompoundOrEmpty(0)
			.getCompoundOrEmpty("portal").putLong("time", 2L);
		NbtIo.writeCompressed(flatRoot, flatFile);
		NbtIo.writeCompressed(forgeRoot, forgeFile);
		byte[] changedFlatHash = sha256(flatFile);
		byte[] changedForgeHash = sha256(forgeFile);

		try (SavedDataStorage restartedStorage = storage(currentDataFolder)) {
			TeleporterCache current = TeleporterCache.get(restartedStorage, currentDataFolder, legacyDataFolder);
			assertPortal(current, -56, 78, 2233445566L, 1122334455L);
		}
		assertArrayEquals(changedFlatHash, sha256(flatFile));
		assertArrayEquals(changedForgeHash, sha256(forgeFile));
	}

	@Test
	void returnsEmptyWhenForgeFileHasNoTwilightForestProvider() throws Exception {
		CompoundTag root = readForgeFixture();
		root.getCompoundOrEmpty("data").remove("twilightforest:teleporter_cache");
		assertFalse(TeleporterCache.decodeLegacyCapabilityFile(root).isPresent());
	}

	@Test
	void rejectsMalformedLegacyProviderInsteadOfSilentlyCreatingAnEmptyCache() throws Exception {
		CompoundTag root = readForgeFixture();
		root.getCompoundOrEmpty("data")
			.getCompoundOrEmpty("twilightforest:teleporter_cache")
			.remove("dest");
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> TeleporterCache.decodeLegacyCapabilityFile(root));
		assertTrue(exception.getMessage().contains("dest"));
	}

	@Test
	void malformedCurrentFailsWithoutFallingBackToEitherLegacySource() throws Exception {
		Path legacyDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("malformed-data"));
		Path currentDataFolder = Files.createDirectories(this.temporaryDirectory.resolve("malformed-current"));
		Path flatFile = legacyDataFolder.resolve("twilightforest_teleporter_cache.dat");
		Path forgeFile = legacyDataFolder.resolve("capabilities.dat");
		NbtIo.writeCompressed(readFlatFixture(), flatFile);
		NbtIo.writeCompressed(readForgeFixture(), forgeFile);
		byte[] flatHash = sha256(flatFile);
		byte[] forgeHash = sha256(forgeFile);

		CompoundTag malformedCurrent = readFlatFixture();
		malformedCurrent.getCompoundOrEmpty("data").remove("dest");
		Path currentFile = Files.createDirectories(currentDataFolder.resolve("twilightforest"))
			.resolve("teleporter_cache.dat");
		NbtIo.writeCompressed(malformedCurrent, currentFile);

		try (SavedDataStorage storage = storage(currentDataFolder)) {
			IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> TeleporterCache.get(storage, currentDataFolder, legacyDataFolder));
			assertTrue(exception.getMessage().contains("Current teleporter cache"));
		}
		assertArrayEquals(flatHash, sha256(flatFile));
		assertArrayEquals(forgeHash, sha256(forgeFile));
	}

	@Test
	void rejectsEveryMissingRequiredPayloadFieldInsteadOfUsingZeroCoordinates() throws Exception {
		assertMalformed(data -> data.remove("dest"), "dest");
		assertMalformed(data -> firstDestination(data).remove("name"), "name");
		assertMalformed(data -> firstDestination(data).remove("links"), "links");
		assertMalformed(data -> firstLink(data).remove("column"), "column");
		assertMalformed(data -> firstLink(data).remove("portal"), "portal");
		assertMalformed(data -> firstLink(data).getCompoundOrEmpty("column").remove("x"), "x");
		assertMalformed(data -> firstLink(data).getCompoundOrEmpty("column").remove("z"), "z");
		assertMalformed(data -> firstLink(data).getCompoundOrEmpty("portal").remove("pos"), "pos");
		assertMalformed(data -> firstLink(data).getCompoundOrEmpty("portal").remove("time"), "time");
	}

	@Test
	void declaresRequiredAccessorForTheActualCurrentSavedDataFolder() throws Exception {
		String mixinConfig = readResource("/twilightforest.mixins.json");
		String source = Files.readString(Path.of("src/main/java/twilightforest/world/TeleporterCache.java"));
		assertTrue(mixinConfig.contains("accessor.SavedDataStorageAccessor"));
		assertTrue(source.contains("twilightforest$getDataFolder"));
	}

	@Test
	void exactBidirectionalLinksKeepNearbyAndStackedPortalsSeparate() {
		TeleporterCache cache = new TeleporterCache();
		Identifier overworld = Identifier.parse("minecraft:overworld");
		TeleporterCache.PortalEndpoint lowerA = endpoint(overworld, 10, 64, 10, 11L);
		TeleporterCache.PortalEndpoint lowerB = endpoint(TWILIGHT_FOREST, 12, 32, 10, 12L);
		TeleporterCache.PortalEndpoint nearbyA = endpoint(overworld, 14, 64, 10, 13L);
		TeleporterCache.PortalEndpoint nearbyB = endpoint(TWILIGHT_FOREST, 16, 32, 10, 14L);
		TeleporterCache.PortalEndpoint stackedA = endpoint(overworld, 10, 112, 10, 15L);
		TeleporterCache.PortalEndpoint stackedB = endpoint(TWILIGHT_FOREST, 12, 80, 10, 16L);

		assertTrue(cache.link(lowerA, lowerB));
		assertTrue(cache.link(nearbyA, nearbyB));
		assertTrue(cache.link(stackedA, stackedB));
		assertEquals(lowerB, cache.getLinkedPortal(lowerA, TWILIGHT_FOREST));
		assertEquals(lowerA, cache.getLinkedPortal(lowerB, overworld));
		assertEquals(nearbyB, cache.getLinkedPortal(nearbyA, TWILIGHT_FOREST));
		assertEquals(stackedB, cache.getLinkedPortal(stackedA, TWILIGHT_FOREST));
		assertFalse(cache.link(nearbyA, lowerB), "an already paired target must never be stolen by a nearby portal");
	}

	@Test
	void invalidationRemovesBothDirectionsAndAllowsCleanRelink() {
		TeleporterCache cache = new TeleporterCache();
		Identifier overworld = Identifier.parse("minecraft:overworld");
		TeleporterCache.PortalEndpoint first = endpoint(overworld, 15, 64, 15, 21L);
		TeleporterCache.PortalEndpoint second = endpoint(TWILIGHT_FOREST, 31, 32, 31, 22L);
		TeleporterCache.PortalEndpoint replacement = endpoint(TWILIGHT_FOREST, 47, 32, 47, 23L);

		assertTrue(cache.link(first, second));
		cache.invalidateContaining(TWILIGHT_FOREST, second.anchor().east());
		assertNull(cache.getLinkedPortal(first));
		assertNull(cache.getLinkedPortal(second));
		assertTrue(cache.link(first, replacement));
		assertEquals(replacement, cache.getLinkedPortal(first));
	}

	@Test
	void versionTwoRoundTripIsIdempotentAndPreservesCrossChunkLinks() {
		Identifier overworld = Identifier.parse("minecraft:overworld");
		TeleporterCache cache = new TeleporterCache();
		TeleporterCache.PortalEndpoint source = endpoint(overworld, 15, 70, 15, 31L);
		TeleporterCache.PortalEndpoint destination = endpoint(TWILIGHT_FOREST, -17, 40, -17, 32L);
		assertTrue(cache.link(source, destination));

		CompoundTag once = cache.save(new CompoundTag());
		TeleporterCache restarted = TeleporterCache.load(once);
		CompoundTag twice = restarted.save(new CompoundTag());
		TeleporterCache restartedAgain = TeleporterCache.load(twice);

		assertEquals(destination, restartedAgain.getLinkedPortal(source, TWILIGHT_FOREST));
		assertEquals(source, restartedAgain.getLinkedPortal(destination, overworld));
		assertEquals(once, twice, "loading and saving the current format must be idempotent");
	}

	@Test
	void legacyColumnHintsAreOneShotAndCannotBecomeAliases() {
		TeleporterCache cache = new TeleporterCache();
		ColumnPos first = new ColumnPos(5, 6);
		ColumnPos second = new ColumnPos(6, 6);
		TFTeleporter.PortalPosition target = new TFTeleporter.PortalPosition(new net.minecraft.core.BlockPos(20, 30, 40), 50L);
		cache.addBlockToCache(TWILIGHT_FOREST, first, target);
		cache.addBlockToCache(TWILIGHT_FOREST, second, target);

		assertEquals(target, cache.consumeLegacyHint(TWILIGHT_FOREST, Set.of(first, second)));
		assertNull(cache.consumeLegacyHint(TWILIGHT_FOREST, Set.of(first, second)));
	}

	@Test
	void nearestIndexUsesHorizontalRadiusButKeepsFullEndpointIdentity() {
		TeleporterCache cache = new TeleporterCache();
		TeleporterCache.PortalEndpoint low = endpoint(TWILIGHT_FOREST, 0, 20, 0, 41L);
		TeleporterCache.PortalEndpoint high = endpoint(TWILIGHT_FOREST, 0, 120, 0, 42L);
		TeleporterCache.PortalEndpoint outside = endpoint(TWILIGHT_FOREST, 201, 20, 0, 43L);
		cache.registerPortal(low);
		cache.registerPortal(high);
		cache.registerPortal(outside);

		assertEquals(Set.of(low, high), Set.copyOf(cache.nearestIndexedPortals(TWILIGHT_FOREST, new net.minecraft.core.BlockPos(0, 64, 0), 200)));
	}

	@Test
	void endpointSnapshotsMutablePositionsAndRejectsPathologicalChunkBounds() {
		net.minecraft.core.BlockPos.MutableBlockPos mutable = new net.minecraft.core.BlockPos.MutableBlockPos(1, 64, 1);
		TeleporterCache.PortalEndpoint endpoint = new TeleporterCache.PortalEndpoint(
			TWILIGHT_FOREST, mutable, 1, 51L, mutable, mutable);
		mutable.set(200, 90, 200);
		assertEquals(new net.minecraft.core.BlockPos(1, 64, 1), endpoint.anchor());

		assertThrows(IllegalArgumentException.class, () -> new TeleporterCache.PortalEndpoint(
			TWILIGHT_FOREST,
			new net.minecraft.core.BlockPos(0, 64, 0),
			4096,
			52L,
			new net.minecraft.core.BlockPos(0, 64, 0),
			new net.minecraft.core.BlockPos(2047, 64, 2048)));
	}

	@Test
	void fallbackPortalSearchOnlyInspectsLoadedChunksWithSectionPaletteFiltering() throws IOException {
		String source = Files.readString(Path.of("src/main/java/twilightforest/world/TFTeleporter.java"));
		String search = source.substring(source.indexOf("private static List<PortalShape> findLoadedPortals"),
			source.indexOf("static PortalShape resolvePortalShape"));
		assertTrue(search.contains("getChunkNow"));
		assertTrue(search.contains("section.maybeHas"));
		assertFalse(search.contains("level.getChunk("), "fallback discovery must not create chunk tickets");
	}

	private static SavedDataStorage storage(Path dataFolder) {
		return new SavedDataStorage(dataFolder, DataFixers.getDataFixer(), RegistryAccess.EMPTY);
	}

	private static TeleporterCache.PortalEndpoint endpoint(Identifier dimension, int x, int y, int z, long fingerprint) {
		net.minecraft.core.BlockPos anchor = new net.minecraft.core.BlockPos(x, y, z);
		return new TeleporterCache.PortalEndpoint(dimension, anchor, 4, fingerprint, anchor, anchor.offset(1, 0, 1));
	}

	private static void assertPortal(TeleporterCache cache, int x, int z, long position, long time) {
		TFTeleporter.PortalPosition portal = cache.getPortalPosition(TWILIGHT_FOREST, new ColumnPos(x, z));
		assertNotNull(portal);
		assertEquals(position, portal.pos.asLong());
		assertEquals(time, portal.lastUpdateTime);
	}

	private static void assertMalformed(Consumer<CompoundTag> corrupt, String expectedField) throws Exception {
		CompoundTag data = readFlatFixture().getCompound("data").orElseThrow();
		corrupt.accept(data);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> TeleporterCache.load(data));
		assertTrue(exception.getMessage().contains(expectedField));
	}

	private static CompoundTag firstDestination(CompoundTag data) {
		return data.getListOrEmpty("dest").getCompoundOrEmpty(0);
	}

	private static CompoundTag firstLink(CompoundTag data) {
		return firstDestination(data).getListOrEmpty("links").getCompoundOrEmpty(0);
	}

	private static CompoundTag readForgeFixture() throws Exception {
		return TagParser.parseCompoundFully(readResource("/twilightforest/world/legacy-forge-capabilities.snbt"));
	}

	private static CompoundTag readFlatFixture() throws Exception {
		return TagParser.parseCompoundFully(readResource("/twilightforest/world/legacy-flat-teleporter-cache.snbt"));
	}

	private static byte[] sha256(Path path) throws Exception {
		return MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
	}

	private static String readResource(String path) throws IOException {
		try (InputStream input = Objects.requireNonNull(TeleporterCacheTests.class.getResourceAsStream(path))) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
