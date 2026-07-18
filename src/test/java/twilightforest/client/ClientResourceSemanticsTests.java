package twilightforest.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientResourceSemanticsTests {
	private static final Path GENERATED_ASSETS = Path.of("src/generated/resources/assets/twilightforest");
	private static final Path MAIN_ASSETS = Path.of("src/main/resources/assets/twilightforest");
	private static final List<String> BISECTED_STAIRS = List.of(
		"encased_castle_brick_stairs",
		"encased_castle_brick_stairs_inner_inner",
		"encased_castle_brick_stairs_outer_outer",
		"nagastone_stairs_left",
		"nagastone_stairs_left_inner_inner",
		"nagastone_stairs_left_outer_outer",
		"nagastone_stairs_right",
		"nagastone_stairs_right_inner_inner",
		"nagastone_stairs_right_outer_outer",
		"mossy_nagastone_stairs_left",
		"mossy_nagastone_stairs_left_inner_inner",
		"mossy_nagastone_stairs_left_outer_outer",
		"mossy_nagastone_stairs_right",
		"mossy_nagastone_stairs_right_inner_inner",
		"mossy_nagastone_stairs_right_outer_outer",
		"cracked_nagastone_stairs_left",
		"cracked_nagastone_stairs_left_inner_inner",
		"cracked_nagastone_stairs_left_outer_outer",
		"cracked_nagastone_stairs_right",
		"cracked_nagastone_stairs_right_inner_inner",
		"cracked_nagastone_stairs_right_outer_outer"
	);
	private static final Map<String, Map<String, String>> POTTED_THORNS = Map.of(
		"potted_green_thorn", Map.of(
			"thorn_side", "twilightforest:block/green_thorns_side",
			"thorn_top", "twilightforest:block/green_thorns_top"),
		"potted_thorn", Map.of(
			"thorn_side", "twilightforest:block/brown_thorns_side",
			"thorn_top", "twilightforest:block/brown_thorns_top"),
		"potted_dead_thorn", Map.of(
			"thorn_side", "twilightforest:block/burnt_thorns_side",
			"thorn_top", "twilightforest:block/burnt_thorns_top")
	);
	private static final Map<String, String> PARTICLE_MODELS = Map.of(
		"models/item/wrought_iron_fence.json", "twilightforest:block/wrought_iron",
		"models/block/wrought_iron_fence_top.json", "twilightforest:block/wrought_iron",
		"models/block/twilight_portal_barrier.json", "twilightforest:block/portal_barrier"
	);

	@Test
	void deerEatingSoundsAndWarningTargetModelsResolveTheirResources() throws IOException {
		JsonArray deerEatingSounds = read(GENERATED_ASSETS.resolve("sounds.json"))
			.getAsJsonObject("entity.twilightforest.deer.eat")
			.getAsJsonArray("sounds");
		assertEquals(5, deerEatingSounds.size());
		for (int sound = 1; sound <= 5; sound++) {
			assertEquals("minecraft:mob/horse/eat" + sound, deerEatingSounds.get(sound - 1).getAsString());
		}

		assertEquals(27, BISECTED_STAIRS.size() + POTTED_THORNS.size() + PARTICLE_MODELS.size());
		for (String modelId : BISECTED_STAIRS) {
			JsonObject model = read(GENERATED_ASSETS.resolve("models/block/" + modelId + ".json"));
			String expectedParent = modelId.endsWith("_inner_inner")
				? "twilightforest:block/util/bisected_inner_stairs"
				: modelId.endsWith("_outer_outer")
					? "twilightforest:block/util/bisected_outer_stairs"
					: "twilightforest:block/util/bisected_stairs";
			assertEquals(expectedParent, model.get("parent").getAsString(), modelId);

			JsonObject textures = model.getAsJsonObject("textures");
			assertEquals(Set.of("end", "middle", "side"), textures.keySet(), modelId);
			assertParentReferences(expectedParent, Set.of("end", "middle", "side"));
			for (String textureVariable : textures.keySet()) {
				assertTwilightTextureExists(textures.get(textureVariable).getAsString(), modelId + " #" + textureVariable);
			}
		}

		for (Map.Entry<String, Map<String, String>> pottedThorn : POTTED_THORNS.entrySet()) {
			String modelId = pottedThorn.getKey();
			JsonObject model = read(GENERATED_ASSETS.resolve("models/block/" + modelId + ".json"));
			assertEquals("twilightforest:block/potted_thorn_template", model.get("parent").getAsString(), modelId);
			JsonObject textures = model.getAsJsonObject("textures");
			assertEquals(pottedThorn.getValue().keySet(), textures.keySet(), modelId);
			assertParentReferences("twilightforest:block/potted_thorn_template", pottedThorn.getValue().keySet());
			for (Map.Entry<String, String> texture : pottedThorn.getValue().entrySet()) {
				assertEquals(texture.getValue(), textures.get(texture.getKey()).getAsString(), modelId + " #" + texture.getKey());
				assertTwilightTextureExists(texture.getValue(), modelId + " #" + texture.getKey());
			}
		}

		for (Map.Entry<String, String> particleModel : PARTICLE_MODELS.entrySet()) {
			JsonObject textures = read(MAIN_ASSETS.resolve(particleModel.getKey())).getAsJsonObject("textures");
			assertEquals(particleModel.getValue(), textures.get("all").getAsString(), particleModel.getKey());
			assertEquals("#all", textures.get("particle").getAsString(), particleModel.getKey());
			assertTwilightTextureExists(particleModel.getValue(), particleModel.getKey() + " #particle");
		}
	}

	private static void assertParentReferences(String parentId, Set<String> textureVariables) throws IOException {
		Path parent = MAIN_ASSETS.resolve("models/" + parentId.substring("twilightforest:".length()) + ".json");
		String parentJson = Files.readString(parent);
		for (String textureVariable : textureVariables) {
			assertTrue(parentJson.contains("\"#" + textureVariable + "\""), parentId + " does not reference #" + textureVariable);
		}
	}

	private static void assertTwilightTextureExists(String textureId, String context) {
		assertTrue(textureId.startsWith("twilightforest:"), context + " is not a Twilight Forest texture: " + textureId);
		String relativeTexture = textureId.substring("twilightforest:".length());
		assertTrue(Files.isRegularFile(MAIN_ASSETS.resolve("textures/" + relativeTexture + ".png")),
			context + " is missing texture " + textureId);
	}

	private static JsonObject read(Path path) throws IOException {
		try (Reader reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
