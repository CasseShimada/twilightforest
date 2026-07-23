package twilightforest.client.renderer;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.Nullable;
import twilightforest.util.TFChestTextures;

import java.util.EnumMap;

/** Maps canonical atlas-relative chest texture IDs through Minecraft's chest sprite mapper. */
public final class TFChestSpriteIds {
	private static final String MAPPER_PREFIX = "entity/chest/";

	private TFChestSpriteIds() {
	}

	public static SpriteId fromTexture(Identifier texture) {
		if (texture.getPath().startsWith(MAPPER_PREFIX)) {
			throw new IllegalArgumentException("Chest textures must be atlas-relative; the mapper adds " + MAPPER_PREFIX + ": " + texture);
		}
		return Sheets.CHEST_MAPPER.apply(texture);
	}

	public static EnumMap<ChestType, SpriteId> forWood(TFChestTextures.Wood wood, boolean trapped) {
		EnumMap<ChestType, SpriteId> sprites = new EnumMap<>(ChestType.class);
		for (ChestType chestType : ChestType.values()) {
			sprites.put(chestType, fromTexture(TFChestTextures.texture(wood, trapped, chestType)));
		}
		return sprites;
	}

	public static SpriteId select(@Nullable SpriteId customSprite, ChestRenderState.ChestMaterialType fallback, ChestType chestType) {
		return customSprite != null ? customSprite : Sheets.chooseSprite(fallback, chestType);
	}
}
