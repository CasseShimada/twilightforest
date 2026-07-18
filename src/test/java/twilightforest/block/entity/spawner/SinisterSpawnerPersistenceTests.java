package twilightforest.block.entity.spawner;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SinisterSpawnerPersistenceTests {
	@Test
	void readsHistoricalEntityScanRangeAndFallsBackToSpawnRange() throws Exception {
		CompoundTag fixture = TagParser.parseCompoundFully(readResource());
		assertEquals(11, SinisterSpawnerLogic.readEntityScanRange(
			input(fixture.getCompoundOrEmpty("sinister_spawner_26_1")), 4));
		assertEquals(7, SinisterSpawnerLogic.readEntityScanRange(input(new CompoundTag()), 7));
	}

	@Test
	void keepsTheCustomSpawnerAlgorithmAndStructureRangeConfiguration() throws IOException {
		String logic = Files.readString(Path.of(
			"src/main/java/twilightforest/block/entity/spawner/SinisterSpawnerLogic.java"));
		assertFalse(logic.contains("super.clientTick("), "configured particles must replace vanilla flame/smoke");
		assertFalse(logic.contains("super.serverTick("), "safe-position scanning must not regress to vanilla random spawning");
		assertTrue(logic.contains("scanSpawnPositions(level, spawnerPos"));
		assertTrue(logic.contains("output.putInt(TAG_ENTITY_SCAN_RANGE"));

		String structure = Files.readString(Path.of(
			"src/main/java/twilightforest/world/components/structures/lichtowerrevamp/LichTowerWingRoom.java"));
		assertTrue(structure.contains("setEntityScanRange(Mth.clamp(Integer.parseInt(parameters[3]), 1, 32))"));
	}

	private static ValueInput input(CompoundTag tag) {
		return TagValueInput.create(ProblemReporter.DISCARDING,
			RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), tag);
	}

	private static String readResource() throws IOException {
		try (InputStream input = Objects.requireNonNull(SinisterSpawnerPersistenceTests.class.getResourceAsStream(
			"/twilightforest/block/entity/spawner/legacy-sinister-spawner.snbt"))) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
