package twilightforest.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.junit.jupiter.api.Test;
import twilightforest.TwilightForestMod;
import twilightforest.client.renderer.TFChestSpriteIds;
import twilightforest.util.TFChestTextures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TFChestResourceTests {
	private static final Path ASSETS = Path.of("src/main/resources/assets/twilightforest");
	private static final Path GENERATED_ASSETS = Path.of("src/generated/resources/assets/twilightforest");

	@Test
	void everyChestVariantMapsExactlyOnceIntoTheChestAtlas() {
		int variants = 0;
		for (TFChestTextures.Wood wood : TFChestTextures.Wood.values()) {
			for (boolean trapped : new boolean[] {false, true}) {
				for (ChestType chestType : ChestType.values()) {
					Identifier relative = TFChestTextures.texture(wood, trapped, chestType);
					SpriteId sprite = TFChestSpriteIds.fromTexture(relative);
					Identifier expected = relative.withPrefix("entity/chest/");

					assertEquals(Sheets.CHEST_SHEET, sprite.atlasLocation(), wood + " " + trapped + " " + chestType);
					assertEquals(expected, sprite.texture(), wood + " " + trapped + " " + chestType);
					assertFalse(sprite.texture().getPath().contains("entity/chest/entity/chest/"), sprite.toString());
					assertTrue(Files.isRegularFile(ASSETS.resolve("textures/" + sprite.texture().getPath() + ".png")),
						() -> "Missing source texture for " + sprite);
					variants++;
				}
			}
		}
		assertEquals(48, variants);
	}

	@Test
	void itemDefinitionsUseTheSameCanonicalSingleChestTextures() throws IOException {
		for (TFChestTextures.Wood wood : TFChestTextures.Wood.values()) {
			for (boolean trapped : new boolean[] {false, true}) {
				Path definition = GENERATED_ASSETS.resolve("items/" + wood.blockId(trapped) + ".json");
				JsonObject special = JsonParser.parseString(Files.readString(definition)).getAsJsonObject()
					.getAsJsonObject("model").getAsJsonObject("model");

				assertEquals("twilightforest:tf_chest", special.get("type").getAsString(), definition.toString());
				assertEquals(TFChestTextures.texture(wood, trapped, ChestType.SINGLE).toString(),
					special.get("texture").getAsString(), definition.toString());
			}
		}
	}

	@Test
	void mapperRejectsAlreadyPrefixedIdsAndCustomChestsWinOverChristmasFallbacks() {
		assertThrows(IllegalArgumentException.class,
			() -> TFChestSpriteIds.fromTexture(TwilightForestMod.prefix("entity/chest/canopy/normal")));

		SpriteId custom = TFChestSpriteIds.fromTexture(TFChestTextures.texture(
			TFChestTextures.Wood.CANOPY, false, ChestType.SINGLE));
		assertSame(custom, TFChestSpriteIds.select(custom, ChestRenderState.ChestMaterialType.CHRISTMAS, ChestType.SINGLE));
		assertEquals(Sheets.chooseSprite(ChestRenderState.ChestMaterialType.CHRISTMAS, ChestType.LEFT),
			TFChestSpriteIds.select(null, ChestRenderState.ChestMaterialType.CHRISTMAS, ChestType.LEFT));
	}
}
