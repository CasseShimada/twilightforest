package twilightforest.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaleBreadResourceTests {
	private static final Path ASSETS = Path.of("src/generated/resources/assets/twilightforest");
	private static final Path DATA = Path.of("src/generated/resources/data/twilightforest");
	private static final Path FABRIC_DATA = Path.of("src/generated/fabric/data/twilightforest");

	@Test
	void keepsPersistentItemAndDamageTypeResources() throws IOException {
		JsonObject item = read(ASSETS.resolve("items/stale_bread.json"));
		assertEquals("twilightforest:item/stale_bread", item.getAsJsonObject("model").get("model").getAsString());

		JsonObject model = read(ASSETS.resolve("models/item/stale_bread.json"));
		assertEquals("minecraft:item/handheld", model.get("parent").getAsString());
		assertEquals("minecraft:item/bread", model.getAsJsonObject("textures").get("layer0").getAsString());

		JsonObject language = read(ASSETS.resolve("lang/en_us.json"));
		assertEquals("Stale Bread", language.get("item.twilightforest.stale_bread").getAsString());
		assertTrue(language.has("death.attack.twilightforest.stale_sandwich"));

		JsonObject damageType = read(FABRIC_DATA.resolve("damage_type/stale_sandwich.json"));
		assertEquals("twilightforest.stale_sandwich", damageType.get("message_id").getAsString());
		assertEquals(0.0F, damageType.get("exhaustion").getAsFloat());
	}

	@Test
	void dryingRecipePreservesInputOutputAndUnlockId() throws IOException {
		JsonObject recipe = read(DATA.resolve("recipe/stale_bread.json"));
		assertEquals("twilightforest:drying", recipe.get("type").getAsString());
		assertEquals(6000, recipe.get("filter_time").getAsInt());
		assertEquals("minecraft:bread", recipe.get("input").getAsString());
		assertEquals("twilightforest:stale_bread", recipe.getAsJsonObject("result").get("id").getAsString());
		assertEquals(1, recipe.getAsJsonObject("result").get("count").getAsInt());

		JsonObject advancement = read(DATA.resolve("advancement/recipes/drying/stale_bread.json"));
		assertTrue(advancement.toString().contains("twilightforest:stale_bread"));
	}

	private static JsonObject read(Path path) throws IOException {
		assertTrue(Files.isRegularFile(path), "Missing resource: " + path);
		try (Reader reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
