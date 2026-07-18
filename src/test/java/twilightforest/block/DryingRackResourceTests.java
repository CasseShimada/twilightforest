package twilightforest.block;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DryingRackResourceTests {
	private static final Path ASSETS = Path.of("src/generated/resources/assets/twilightforest");
	private static final Path DATA = Path.of("src/generated/resources/data/twilightforest");
	private static final Map<String, String> RACK_MODELS = createRackModels();

	@Test
	void everyDryingRackHasCompletePersistentResources() throws IOException {
		JsonObject language = read(ASSETS.resolve("lang/en_us.json"));
		for (Map.Entry<String, String> entry : RACK_MODELS.entrySet()) {
			String id = entry.getKey();
			String blockModel = entry.getValue();
			JsonObject blockstate = read(ASSETS.resolve("blockstates/" + id + ".json"));
			assertTrue(blockstate.toString().contains(blockModel), id + " blockstate must reference its model");

			String modelPath = blockModel.substring("twilightforest:block/".length());
			read(ASSETS.resolve("models/block/" + modelPath + ".json"));
			JsonObject item = read(ASSETS.resolve("items/" + id + ".json"));
			assertEquals(blockModel, item.getAsJsonObject("model").get("model").getAsString());

			String persistentId = "twilightforest:" + id;
			assertTrue(language.has("block.twilightforest." + id), id + " is missing its English fallback name");
			assertTrue(read(DATA.resolve("loot_table/blocks/" + id + ".json")).toString().contains(persistentId));
			assertTrue(read(DATA.resolve("recipe/wood/" + id + ".json")).toString().contains(persistentId));
		}
	}

	@Test
	void paleOakDryingRackUsesPaleOakResources() throws IOException {
		JsonObject model = read(ASSETS.resolve("models/block/wood/rack/pale_oak/pale_oak_drying_rack.json"));
		assertEquals("minecraft:block/pale_oak_planks", model.getAsJsonObject("textures").get("texture").getAsString());
		JsonObject recipe = read(DATA.resolve("recipe/wood/pale_oak_drying_rack.json"));
		assertEquals("minecraft:pale_oak_slab", recipe.getAsJsonObject("key").get("-").getAsString());
	}

	private static JsonObject read(Path path) throws IOException {
		assertTrue(Files.isRegularFile(path), "Missing resource: " + path);
		try (Reader reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}

	private static Map<String, String> createRackModels() {
		Map<String, String> models = new LinkedHashMap<>();
		models.put("oak_drying_rack", "twilightforest:block/wood/rack/oak/oak_drying_rack");
		models.put("spruce_drying_rack", "twilightforest:block/wood/rack/spruce/spruce_drying_rack");
		models.put("birch_drying_rack", "twilightforest:block/wood/rack/birch/birch_drying_rack");
		models.put("jungle_drying_rack", "twilightforest:block/wood/rack/jungle/jungle_drying_rack");
		models.put("acacia_drying_rack", "twilightforest:block/wood/rack/acacia/acacia_drying_rack");
		models.put("dark_oak_drying_rack", "twilightforest:block/wood/rack/dark_oak/dark_oak_drying_rack");
		models.put("crimson_drying_rack", "twilightforest:block/wood/rack/crimson/crimson_drying_rack");
		models.put("warped_drying_rack", "twilightforest:block/wood/rack/warped/warped_drying_rack");
		models.put("vangrove_drying_rack", "twilightforest:block/wood/rack/vanilla_mangrove/vangrove_drying_rack");
		models.put("bamboo_drying_rack", "twilightforest:block/wood/rack/bamboo/bamboo_drying_rack");
		models.put("cherry_drying_rack", "twilightforest:block/wood/rack/cherry/cherry_drying_rack");
		models.put("pale_oak_drying_rack", "twilightforest:block/wood/rack/pale_oak/pale_oak_drying_rack");
		models.put("twilight_oak_drying_rack", "twilightforest:block/wood/rack/twilight_oak/twilight_oak_drying_rack");
		models.put("canopy_drying_rack", "twilightforest:block/wood/rack/canopy/canopy_drying_rack");
		models.put("mangrove_drying_rack", "twilightforest:block/wood/rack/mangrove/mangrove_drying_rack");
		models.put("dark_drying_rack", "twilightforest:block/wood/rack/darkwood/dark_drying_rack");
		models.put("time_drying_rack", "twilightforest:block/wood/rack/time/time_drying_rack");
		models.put("transformation_drying_rack", "twilightforest:block/wood/rack/trans/transformation_drying_rack");
		models.put("mining_drying_rack", "twilightforest:block/wood/rack/mine/mining_drying_rack");
		models.put("sorting_drying_rack", "twilightforest:block/wood/rack/sort/sorting_drying_rack");
		return Map.copyOf(models);
	}
}
