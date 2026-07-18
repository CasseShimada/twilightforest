package twilightforest.init;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrySnapshotTests {
	private static final String SNAPSHOT = "/data/twilightforest/migration/registry_snapshot.json";

	@Test
	void snapshotIsSortedCompleteAndNamespaceSafe() throws IOException {
		JsonObject snapshot = readSnapshot();
		JsonObject staticRegistries = snapshot.getAsJsonObject("static_registries");
		JsonObject datapackRegistries = snapshot.getAsJsonObject("datapack_registries");

		assertEquals(1, snapshot.get("schema").getAsInt());
		assertEquals("26.2", snapshot.get("minecraft_version").getAsString());
		assertEquals(43, staticRegistries.size());
		assertEquals(27, datapackRegistries.size());
		assertEquals(2118, validateSection(staticRegistries));
		assertEquals(444, validateSection(datapackRegistries));
		assertEquals(2118, snapshot.get("static_entry_count").getAsInt());
		assertEquals(444, snapshot.get("datapack_entry_count").getAsInt());

		Set<String> overlap = new HashSet<>(staticRegistries.keySet());
		overlap.retainAll(datapackRegistries.keySet());
		assertTrue(overlap.isEmpty(), () -> "Static registries leaked into the datapack section: " + overlap);
	}

	@Test
	void preservesHighRiskRegistryIds() throws IOException {
		JsonObject snapshot = readSnapshot();
		JsonObject staticRegistries = snapshot.getAsJsonObject("static_registries");
		JsonObject datapackRegistries = snapshot.getAsJsonObject("datapack_registries");

		Map.ofEntries(
			Map.entry("minecraft:block", 528),
			Map.entry("minecraft:item", 660),
			Map.entry("minecraft:entity_type", 96),
			Map.entry("minecraft:block_entity_type", 36),
			Map.entry("minecraft:data_component_type", 47),
			Map.entry("minecraft:particle_type", 29),
			Map.entry("minecraft:recipe_serializer", 13),
			Map.entry("minecraft:sound_event", 328),
			Map.entry("minecraft:game_rule", 4),
			Map.entry("minecraft:worldgen/structure_piece", 175),
			Map.entry("twilight:item_display_type", 4),
			Map.entry("twilight:travellers_modifier_type", 4)
		).forEach((registry, count) -> assertEquals(count, entries(staticRegistries, registry).size(), registry));

		assertContainsAll(staticRegistries, "minecraft:item",
			"twilightforest:magic_map", "twilightforest:filled_magic_map",
			"twilightforest:maze_map", "twilightforest:filled_maze_map",
			"twilightforest:ore_map", "twilightforest:filled_ore_map",
			"twilightforest:travellers_goggles", "twilightforest:travellers_vest",
			"twilightforest:travellers_gloves", "twilightforest:travellers_wings",
			"twilightforest:travellers_belt", "twilightforest:travellers_boots");
		assertContainsAll(staticRegistries, "minecraft:block",
			"twilightforest:dark_tower_miniature_structure",
			"twilightforest:minotaur_labyrinth_miniature_structure");
		assertFalse(entries(staticRegistries, "minecraft:item").contains("twilightforest:dark_tower_miniature_structure"));
		assertFalse(entries(staticRegistries, "minecraft:item").contains("twilightforest:minotaur_labyrinth_miniature_structure"));
		assertContainsAll(staticRegistries, "minecraft:block_entity_type",
			"twilightforest:chest", "twilightforest:trapped_chest", "twilightforest:drying_rack");
		assertContainsAll(staticRegistries, "minecraft:data_component_type",
			"twilightforest:item_display", "twilightforest:stored_broken_attributes",
			"twilightforest:travellers_armor");
		assertContainsAll(staticRegistries, "minecraft:game_rule",
			"twilightforest:tf_enforced_progression",
			"twilightforest:twilightforest_enforced_progression",
			"twilightforest:players_twilight_portal_default_delay",
			"twilightforest:players_twilight_portal_creative_delay");
		assertContainsAll(staticRegistries, "twilight:item_display_type",
			"twilightforest:clock", "twilightforest:compass", "twilightforest:map", "twilightforest:moon_dial");
		assertContainsAll(staticRegistries, "minecraft:worldgen/structure_type", "twilightforest:camp");
		assertContainsAll(staticRegistries, "twilightforest:template_marker_handler_type",
			"twilightforest:block_placement", "twilightforest:handler_switch", "twilightforest:rotation",
			"twilightforest:drying_rack", "twilightforest:painting", "twilightforest:loot");
		assertContainsAll(datapackRegistries, "minecraft:dimension", "twilightforest:twilight_forest");
		assertContainsAll(datapackRegistries, "minecraft:dimension_type", "twilightforest:twilight_forest_type");
		assertContainsAll(datapackRegistries, "minecraft:worldgen/structure", "twilightforest:camp");
		assertContainsAll(datapackRegistries, "minecraft:worldgen/structure_set", "twilightforest:camp");
		assertContainsAll(datapackRegistries, "twilight:template_marker_handler_list", "twilightforest:camp_marker_handlers");
		assertTrue(entries(datapackRegistries, "twilight:template_marker_handler").isEmpty(),
			"Camp currently stores its handlers inline; the reserved handler registry must remain present and empty");
		assertEquals(24, entries(datapackRegistries, "twilight:travellers_modifiers").size());
	}

	@Test
	void preservesLegacyBlockOnlyMiniatureModels() throws IOException {
		assertMiniatureBlockstate("dark_tower_miniature_structure", "twilightforest:block/miniature/dark_tower");
		assertMiniatureBlockstate("minotaur_labyrinth_miniature_structure", "twilightforest:block/miniature/labyrinth");
	}

	private static int validateSection(JsonObject registries) {
		List<String> registryIds = new ArrayList<>(registries.keySet());
		assertEquals(registryIds.stream().sorted().toList(), registryIds, "Registry keys must be sorted");
		int count = 0;
		for (Map.Entry<String, JsonElement> registry : registries.entrySet()) {
			JsonArray jsonEntries = registry.getValue().getAsJsonArray();
			List<String> ids = new ArrayList<>(jsonEntries.size());
			jsonEntries.forEach(id -> ids.add(id.getAsString()));
			if (registry.getKey().equals("twilight:template_marker_handler")) {
				assertTrue(ids.isEmpty(), "The reserved inline-handler registry unexpectedly gained entries");
			} else {
				assertFalse(ids.isEmpty(), () -> "Snapshot includes empty registry " + registry.getKey());
			}
			assertEquals(ids.stream().sorted().toList(), ids, registry.getKey() + " entries must be sorted");
			assertEquals(ids.size(), new HashSet<>(ids).size(), registry.getKey() + " contains duplicate IDs");
			assertTrue(ids.stream().allMatch(id -> id.startsWith("twilightforest:") || id.startsWith("twilight:")),
				registry.getKey() + " contains an unrelated namespace");
			count += ids.size();
		}
		return count;
	}

	private static void assertContainsAll(JsonObject registries, String registry, String... ids) {
		List<String> entries = entries(registries, registry);
		for (String id : ids) {
			assertTrue(entries.contains(id), () -> registry + " is missing " + id);
		}
	}

	private static List<String> entries(JsonObject registries, String registry) {
		JsonArray entries = Objects.requireNonNull(registries.getAsJsonArray(registry), "Missing registry " + registry);
		List<String> ids = new ArrayList<>(entries.size());
		entries.forEach(id -> ids.add(id.getAsString()));
		return ids;
	}

	private static void assertMiniatureBlockstate(String name, String model) throws IOException {
		JsonObject variants = readJson("/assets/twilightforest/blockstates/" + name + ".json").getAsJsonObject("variants");
		assertEquals(4, variants.size(), name);
		assertEquals(model, variants.getAsJsonObject("facing=north").get("model").getAsString());
		assertEquals(model, variants.getAsJsonObject("facing=east").get("model").getAsString());
		assertEquals(90, variants.getAsJsonObject("facing=east").get("y").getAsInt());
		assertEquals(model, variants.getAsJsonObject("facing=south").get("model").getAsString());
		assertEquals(180, variants.getAsJsonObject("facing=south").get("y").getAsInt());
		assertEquals(model, variants.getAsJsonObject("facing=west").get("model").getAsString());
		assertEquals(270, variants.getAsJsonObject("facing=west").get("y").getAsInt());
	}

	private static JsonObject readSnapshot() throws IOException {
		return readJson(SNAPSHOT);
	}

	private static JsonObject readJson(String resource) throws IOException {
		try (InputStream input = Objects.requireNonNull(RegistrySnapshotTests.class.getResourceAsStream(resource));
			 InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
