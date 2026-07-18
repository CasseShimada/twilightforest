package twilightforest.block;

import com.google.gson.JsonElement;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BerryBushResourceTests {
	private static final Path ASSETS = Path.of("src/generated/resources/assets/twilightforest");
	private static final Path DATA = Path.of("src/generated/resources/data/twilightforest");
	private static final Map<String, String> BUSH_MODELS = createBushModels();

	@Test
	void everyPersistentBushIdHasCompleteResources() throws IOException {
		for (Map.Entry<String, String> entry : BUSH_MODELS.entrySet()) {
			String id = entry.getKey();
			String modelBase = entry.getValue();
			JsonObject blockstate = read(ASSETS.resolve("blockstates/" + id + ".json"));
			JsonObject variants = blockstate.getAsJsonObject("variants");
			assertEquals(36, variants.size(), id + " must cover every age/layers state");

			for (int age = 0; age <= 3; age++) {
				for (int layers = 0; layers <= 8; layers++) {
					String state = "age=" + age + ",layers=" + layers;
					assertTrue(variants.has(state), id + " is missing " + state);
					String model = variants.getAsJsonObject(state).get("model").getAsString();
					assertTrue(model.startsWith("twilightforest:block/"), id + " has an external model");
					assertTrue(Files.isRegularFile(ASSETS.resolve("models/block/" + model.substring("twilightforest:block/".length()) + ".json")), model);
				}
			}

			JsonObject item = read(ASSETS.resolve("items/" + id + ".json"));
			assertEquals("twilightforest:block/" + modelBase + "_0", item.getAsJsonObject("model").get("model").getAsString());

			Path lootPath = DATA.resolve("loot_table/blocks/" + id + ".json");
			JsonObject loot = read(lootPath);
			assertTrue(loot.toString().contains("twilightforest:" + id), id + " loot must use its persistent registry ID");
		}
	}

	@Test
	void modelsUseVanillaRenderingMetadata() throws IOException {
		for (String modelBase : BUSH_MODELS.values()) {
			for (Path model : Files.newDirectoryStream(ASSETS.resolve("models/block"), modelBase + "*.json")) {
				assertFalse(Files.readString(model).contains("neoforge_data"), model.toString());
			}
		}

		JsonObject essence = read(ASSETS.resolve("models/block/essence_oreberry_0.json"));
		assertEquals(3, essence.getAsJsonArray("elements").get(0).getAsJsonObject().get("light_emission").getAsInt());
		for (int i = 0; i <= 8; i++) {
			JsonObject parent = read(ASSETS.resolve("models/block/abstract_bush_" + i + ".json"));
			assertEquals("minecraft:cutout", parent.get("render_type").getAsString());
		}
	}

	private static JsonObject read(Path path) throws IOException {
		assertTrue(Files.isRegularFile(path), "Missing resource: " + path);
		try (Reader reader = Files.newBufferedReader(path)) {
			JsonElement parsed = JsonParser.parseReader(reader);
			assertTrue(parsed.isJsonObject(), "Expected JSON object: " + path);
			return parsed.getAsJsonObject();
		}
	}

	private static Map<String, String> createBushModels() {
		Map<String, String> ids = new LinkedHashMap<>();
		ids.put("blackberry_bush", "blackberry_bush");
		ids.put("blightberry_bush", "blightberry_bush");
		ids.put("blueberry_bush", "blueberry_bush");
		ids.put("copper_oreberry_bush", "copper_oreberry");
		ids.put("duskberry_bush", "duskberry_bush");
		ids.put("essence_oreberry_bush", "essence_oreberry");
		ids.put("gold_oreberry_bush", "gold_oreberry");
		ids.put("iron_oreberry_bush", "iron_oreberry");
		ids.put("maloberry_bush", "maloberry_bush");
		ids.put("raspberry_bush", "raspberry_bush");
		ids.put("skyberry_bush", "skyberry_bush");
		ids.put("stingberry_bush", "stingberry_bush");
		return Map.copyOf(ids);
	}
}
