package twilightforest.world.components.structures.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplatePoolInstanceCodecTests {
	@Test
	void acceptsAndReemitsLegacyIntegerWeights() {
		TemplatePoolInstance decoded = TemplatePoolInstance.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("25")).getOrThrow();
		assertEquals(25, decoded.weight());
		assertTrue(decoded.poolAliases().isEmpty());

		JsonElement encoded = TemplatePoolInstance.CODEC.encodeStart(JsonOps.INSTANCE, decoded).getOrThrow();
		assertEquals(25, encoded.getAsInt());
	}

	@Test
	void decodesCampRichMetadataWithoutRegistryBackedFields() {
		JsonElement json = JsonParser.parseString("""
			{
			  "weight": 100,
			  "terrain_adaptation": "beard_box",
			  "height_adjustment": {
			    "heightmap": "WORLD_SURFACE_WG",
			    "y_offset": 2,
			    "ground_junction_diff_clamp": 0
			  },
			  "ignore_world_waterlog": true,
			  "pool_aliases": {
			    "twilightforest:camp/deco": "twilightforest:camp/rack"
			  }
			}
			""");

		TemplatePoolInstance decoded = TemplatePoolInstance.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
		assertEquals(100, decoded.weight());
		assertEquals(TerrainAdjustment.BEARD_BOX, decoded.terrainAdjustment());
		assertTrue(decoded.ignoreWorldWaterlog());
		assertEquals(2, decoded.beardifierGroundDelta().orElseThrow().beardifierGroundDelta());
		assertEquals(0, decoded.beardifierGroundDelta().orElseThrow().groundJunctionDiffLimit().orElseThrow());
		assertEquals("twilightforest:camp/rack", decoded.poolAliases().get("twilightforest:camp/deco"));

		JsonElement encoded = TemplatePoolInstance.CODEC.encodeStart(JsonOps.INSTANCE, decoded).getOrThrow();
		assertFalse(encoded.isJsonPrimitive());
		assertEquals("beard_box", encoded.getAsJsonObject().get("terrain_adaptation").getAsString());
	}
}
