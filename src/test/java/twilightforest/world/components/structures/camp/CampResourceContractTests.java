package twilightforest.world.components.structures.camp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CampResourceContractTests {
	private static final Path STRUCTURES = Path.of("src/main/resources/data/twilightforest/structure/camp");
	private static final Path DEFINITIONS = Path.of("src/generated/resources/data/twilightforest/twilight/template_definition");
	private static final Path CAMP_DEFINITIONS = DEFINITIONS.resolve("camp");
	private static final Path LOOT_TABLES = Path.of("src/generated/resources/data/twilightforest/loot_table");
	private static final Path MARKER_HANDLERS = Path.of("src/generated/resources/data/twilightforest/twilight/template_marker_handler_list/camp_marker_handlers.json");

	private static final Set<String> CAMP_TEMPLATES = Set.of(
		"campfire_east", "campfire_south", "campfire_west",
		"deco/berries_staked", "deco/berries_support", "deco/berries_trellis",
		"deco/blackberry_wall", "deco/blueberry_wall", "deco/compost_large", "deco/compost_small",
		"deco/double_drying_rack", "deco/garden_1x3", "deco/garden_2x4", "deco/garden_straight",
		"deco/garden_u", "deco/long_drying_rack", "deco/lumber1", "deco/lumber2", "deco/lumber3",
		"deco/pen", "deco/raspberry_wall", "deco/repair_station",
		"path/intersection_left", "path/intersection_right", "path/intersection_short", "path/j_path",
		"path/l_path", "path/path_2x4", "path/path_2x6", "path/path_2x7", "path/path_3x4",
		"tent/duo_tent", "tent/luxury_tent", "tent/open_tent", "tent/solo_tent"
	);
	private static final Set<String> CAMP_LOOT_TABLES = Set.of(
		"camp_armor_rack.json", "camp_drying_rack.json", "camp_pot.json", "camp_tent.json"
	);

	@Test
	void importsTheCompleteUpstreamCampResourceInventory() throws IOException {
		assertEquals(withExtension(CAMP_TEMPLATES, ".nbt"), relativeFiles(STRUCTURES, ".nbt"));
		assertEquals(withExtension(CAMP_TEMPLATES, ".json"), relativeFiles(CAMP_DEFINITIONS, ".json"));
		assertTrue(Files.isRegularFile(DEFINITIONS.resolve("_empty.json")));
		assertEquals(CAMP_LOOT_TABLES, directFiles(LOOT_TABLES, "camp_", ".json"));
		assertTrue(Files.isRegularFile(MARKER_HANDLERS));
		assertEquals(35, CAMP_TEMPLATES.size());
		assertEquals(36, relativeFiles(CAMP_DEFINITIONS, ".json").size() + 1);
	}

	@Test
	void templateDefinitionsPreserveEveryPoolMemberAndWeight() throws IOException {
		Map<String, PoolStats> pools = new TreeMap<>();
		for (String template : CAMP_TEMPLATES) {
			JsonObject definition = read(CAMP_DEFINITIONS.resolve(template + ".json"));
			definition.entrySet().forEach(entry -> pools.computeIfAbsent(entry.getKey(), ignored -> new PoolStats())
				.add(weight(entry.getValue())));
		}
		JsonObject empty = read(DEFINITIONS.resolve("_empty.json"));
		empty.entrySet().forEach(entry -> pools.computeIfAbsent(entry.getKey(), ignored -> new PoolStats())
			.add(weight(entry.getValue())));

		assertEquals(Map.of(
			"twilightforest:camp/deco", new PoolStats(20, 2750),
			"twilightforest:camp/main_path", new PoolStats(10, 625),
			"twilightforest:camp/path", new PoolStats(5, 500),
			"twilightforest:camp/rack", new PoolStats(2, 200),
			"twilightforest:camp/rack_path", new PoolStats(2, 200),
			"twilightforest:camp/structure_start", new PoolStats(3, 300),
			"twilightforest:camp/tent", new PoolStats(4, 300)
		), pools);
		assertEquals(1000, empty.get("twilightforest:camp/deco").getAsInt());
		assertEquals(300, empty.get("twilightforest:camp/main_path").getAsInt());
		assertEquals(300, empty.get("twilightforest:camp/path").getAsInt());

		JsonObject start = pool(CAMP_DEFINITIONS.resolve("campfire_east.json"), "twilightforest:camp/structure_start");
		assertEquals("WORLD_SURFACE_WG", start.getAsJsonObject("height_adjustment").get("heightmap").getAsString());
		assertEquals(1, start.getAsJsonObject("height_adjustment").get("y_offset").getAsInt());
		assertEquals("twilightforest:camp_marker_handlers", start.get("marker_handlers").getAsString());
		assertEquals("beard_box", start.get("terrain_adaptation").getAsString());
		assertTrue(start.get("ignore_world_waterlog").getAsBoolean());

		JsonObject rackPath = pool(CAMP_DEFINITIONS.resolve("path/path_2x4.json"), "twilightforest:camp/rack_path");
		assertEquals("twilightforest:camp/rack", rackPath.getAsJsonObject("pool_aliases").get("twilightforest:camp/deco").getAsString());
		JsonObject gravity = rackPath.getAsJsonObject("processors").getAsJsonArray("processors").get(0).getAsJsonObject();
		assertEquals("minecraft:gravity", gravity.get("processor_type").getAsString());
		assertEquals(-2, gravity.get("offset").getAsInt());

		JsonObject luxuryTent = pool(CAMP_DEFINITIONS.resolve("tent/luxury_tent.json"), "twilightforest:camp/tent");
		assertEquals(2, luxuryTent.getAsJsonArray("randomized_processors").size());
	}

	@Test
	void nbtTemplatesPreserveDataVersionMarkersPoolsAndTentLoot() throws IOException {
		Map<String, Integer> markers = new TreeMap<>();
		Map<String, Integer> jigsawPools = new TreeMap<>();
		int tentLootTables = 0;

		for (String template : CAMP_TEMPLATES) {
			CompoundTag root = NbtIo.readCompressed(STRUCTURES.resolve(template + ".nbt"), NbtAccounter.unlimitedHeap());
			assertEquals(3955, root.getIntOr("DataVersion", -1), template + " changed its upstream DataVersion");
			ListTag palette = root.getListOrEmpty("palette");
			ListTag blocks = root.getListOrEmpty("blocks");
			for (int blockIndex = 0; blockIndex < blocks.size(); blockIndex++) {
				CompoundTag block = blocks.getCompoundOrEmpty(blockIndex);
				int stateIndex = block.getIntOr("state", -1);
				assertTrue(stateIndex >= 0 && stateIndex < palette.size(), template + " has an invalid palette index");
				String blockId = palette.getCompoundOrEmpty(stateIndex).getStringOr("Name", "");
				CompoundTag blockEntityData = block.getCompoundOrEmpty("nbt");
				if (blockId.equals("minecraft:structure_block")) {
					markers.merge(blockEntityData.getStringOr("metadata", ""), 1, Integer::sum);
				} else if (blockId.equals("minecraft:jigsaw")) {
					jigsawPools.merge(blockEntityData.getStringOr("pool", ""), 1, Integer::sum);
				}
				if (blockEntityData.getStringOr("LootTable", "").equals("twilightforest:camp_tent")) {
					tentLootTables++;
				}
			}
		}

		assertEquals(Map.of(
			"birch_drying_rack@north", 11,
			"birch_drying_rack@south", 4,
			"camp_armor_rack@north", 1,
			"painting@east", 1,
			"tent_pot@east", 1,
			"tent_pot@west", 1,
			"twilight_oak_slab", 41
		), markers);
		assertEquals(Map.of(
			"minecraft:empty", 27,
			"twilightforest:camp/deco", 30,
			"twilightforest:camp/main_path", 18,
			"twilightforest:camp/path", 6,
			"twilightforest:camp/rack_path", 3,
			"twilightforest:camp/tent", 3,
			"twilightforest:empty", 17
		), jigsawPools);
		assertEquals(4, tentLootTables);
	}

	@Test
	void markerHandlersAndLootTablesRetainTheirCrossResourceIds() throws IOException {
		JsonObject rotation;
		try (Reader reader = Files.newBufferedReader(MARKER_HANDLERS)) {
			rotation = JsonParser.parseReader(reader).getAsJsonArray().get(0).getAsJsonObject();
		}
		assertEquals("twilightforest:rotation", rotation.get("type").getAsString());
		JsonObject handlerSwitch = rotation.getAsJsonObject("rotated");
		assertEquals("twilightforest:handler_switch", handlerSwitch.get("type").getAsString());
		JsonObject handlers = handlerSwitch.getAsJsonObject("element_processor");
		assertEquals(Set.of("birch_drying_rack", "camp_armor_rack", "painting", "tent_pot", "twilight_oak_slab"), handlers.keySet());
		assertEquals("twilightforest:camp_drying_rack", handlers.getAsJsonObject("birch_drying_rack").get("loot_table").getAsString());
		assertEquals("twilightforest:camp_armor_rack", handlers.getAsJsonObject("camp_armor_rack").get("loot_table").getAsString());
		assertEquals("twilightforest:camp_pot", handlers.getAsJsonObject("tent_pot").get("loot_table").getAsString());

		for (String fileName : CAMP_LOOT_TABLES) {
			JsonObject loot = read(LOOT_TABLES.resolve(fileName));
			String id = fileName.substring(0, fileName.length() - ".json".length());
			assertEquals("twilightforest:" + id, loot.get("random_sequence").getAsString());
			assertEquals("minecraft:chest", loot.get("type").getAsString());
		}
		String dryingRackLoot = read(LOOT_TABLES.resolve("camp_drying_rack.json")).toString();
		assertTrue(dryingRackLoot.contains("\"type\":\"minecraft:empty\""));
		assertFalse(dryingRackLoot.contains("\"type\":\"minecraft:air\""));
	}

	private static Set<String> withExtension(Set<String> paths, String extension) {
		return paths.stream().map(path -> path + extension).collect(Collectors.toUnmodifiableSet());
	}

	private static Set<String> relativeFiles(Path root, String extension) throws IOException {
		try (Stream<Path> paths = Files.walk(root)) {
			return paths.filter(Files::isRegularFile)
				.map(root::relativize)
				.map(path -> path.toString().replace('\\', '/'))
				.filter(path -> path.endsWith(extension))
				.collect(Collectors.toUnmodifiableSet());
		}
	}

	private static Set<String> directFiles(Path root, String prefix, String suffix) throws IOException {
		try (Stream<Path> paths = Files.list(root)) {
			return paths.filter(Files::isRegularFile)
				.map(path -> path.getFileName().toString())
				.filter(path -> path.startsWith(prefix) && path.endsWith(suffix))
				.collect(Collectors.toUnmodifiableSet());
		}
	}

	private static JsonObject read(Path path) throws IOException {
		try (Reader reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}

	private static JsonObject pool(Path path, String poolId) throws IOException {
		return read(path).getAsJsonObject(poolId);
	}

	private static int weight(JsonElement element) {
		return element.isJsonPrimitive() ? element.getAsInt() : element.getAsJsonObject().get("weight").getAsInt();
	}

	private static final class PoolStats {
		private int members;
		private int totalWeight;

		private PoolStats() {
		}

		private PoolStats(int members, int totalWeight) {
			this.members = members;
			this.totalWeight = totalWeight;
		}

		private void add(int weight) {
			this.members++;
			this.totalWeight += weight;
		}

		@Override
		public boolean equals(Object object) {
			return object instanceof PoolStats stats && this.members == stats.members && this.totalWeight == stats.totalWeight;
		}

		@Override
		public int hashCode() {
			return 31 * this.members + this.totalWeight;
		}

		@Override
		public String toString() {
			return "PoolStats[members=" + this.members + ", totalWeight=" + this.totalWeight + ']';
		}
	}
}
