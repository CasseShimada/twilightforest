package twilightforest.block.entity.bookshelf;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.ProblemReporter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import twilightforest.test.MinecraftBootstrapExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MinecraftBootstrapExtension.class)
class BookshelfSpawnerPersistenceTests {
	@Test
	void readsForgeTomeSpawnerFieldsAndPreservesTenRemainingSpawns() throws Exception {
		CompoundTag fixture = readFixture();
		assertEquals(new BookshelfSpawner.LegacyTomeSpawnerState(
			"twilightforest:death_tome", 5, 400, 8, false),
			BookshelfSpawner.readLegacyState(input(fixture.getCompoundOrEmpty("forge_1_20_1"))).orElseThrow());
		assertEquals(new BookshelfSpawner.LegacyTomeSpawnerState(
			"twilightforest:death_tome", 10, 240, 12, false),
			BookshelfSpawner.readLegacyState(input(fixture.getCompoundOrEmpty("forge_clamped_remaining"))).orElseThrow());
		assertEquals(6, ChiseledCanopyShelfBlockEntity.legacyVisibleBookCount(10));
		assertEquals(4, ChiseledCanopyShelfBlockEntity.legacyOverflowCount(10));
		assertEquals(5, ChiseledCanopyShelfBlockEntity.legacyVisibleBookCount(5));
		assertEquals(0, ChiseledCanopyShelfBlockEntity.legacyOverflowCount(5));

		BookshelfSpawner spawner = new BookshelfSpawner() {
			@Override
			public void broadcastEvent(Level level, BlockPos pos, int id) {
			}
		};
		spawner.loadLegacyState(null, BlockPos.ZERO, new BookshelfSpawner.LegacyTomeSpawnerState(
			"twilightforest:death_tome", 5, 400, 8, false));
		assertEquals(new BookshelfSpawner.PersistentState(
			400, 400, 400, Short.MAX_VALUE, 8, "twilightforest:death_tome"), spawner.persistentState());
	}

	@Test
	void transitionFormatWithoutCountUsesHistoricalTenBookDefault() throws Exception {
		BookshelfSpawner.LegacyTomeSpawnerState state = BookshelfSpawner.readLegacyState(
			input(readFixture().getCompoundOrEmpty("transition_missing_count"))).orElseThrow();
		assertEquals(10, state.remainingSpawns());
		assertTrue(state.remainingWasInferred());
	}

	@Test
	void currentDataWinsAndIncompleteOrUnrepresentableLegacyDataFailsFast() throws Exception {
		CompoundTag fixture = readFixture();
		assertFalse(BookshelfSpawner.readLegacyState(
			input(fixture.getCompoundOrEmpty("current_keys_take_precedence"))).isPresent());
		assertThrows(IllegalStateException.class, () -> BookshelfSpawner.readLegacyState(
			input(fixture.getCompoundOrEmpty("incomplete_legacy"))));
		assertThrows(IllegalStateException.class, () -> BookshelfSpawner.readLegacyState(
			input(fixture.getCompoundOrEmpty("invalid_delay"))));
	}

	@Test
	void fireRemovalKeepsTheRemovedBookshelfFacing() throws Exception {
		String spawnerSource = Files.readString(Path.of(
			"src/main/java/twilightforest/block/entity/bookshelf/BookshelfSpawner.java"));
		String shelfSource = Files.readString(Path.of(
			"src/main/java/twilightforest/block/entity/bookshelf/ChiseledCanopyShelfBlockEntity.java"));
		assertFalse(spawnerSource.contains("BlockState shelf = level.getBlockState(pos)"),
			"Fire has already replaced the shelf when setRemoved runs");
		assertTrue(spawnerSource.contains("BlockPos pos, Direction facing, boolean fire"));
		assertTrue(shelfSource.contains("oldState.getValue(HorizontalDirectionalBlock.FACING)"));
	}

	private static ValueInput input(CompoundTag tag) {
		return TagValueInput.create(ProblemReporter.DISCARDING,
			RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), tag);
	}

	private static CompoundTag readFixture() throws Exception {
		return TagParser.parseCompoundFully(readResource());
	}

	private static String readResource() throws IOException {
		try (InputStream input = Objects.requireNonNull(BookshelfSpawnerPersistenceTests.class.getResourceAsStream(
			"/twilightforest/block/entity/bookshelf/legacy-bookshelf-spawner.snbt"))) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
