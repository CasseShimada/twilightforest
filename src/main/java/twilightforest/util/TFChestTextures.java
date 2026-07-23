package twilightforest.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.ChestType;
import twilightforest.TwilightForestMod;

/** Canonical atlas-relative texture IDs for Twilight Forest chests. */
public final class TFChestTextures {
	private TFChestTextures() {
	}

	public static Identifier texture(Wood wood, boolean trapped, ChestType chestType) {
		String variant = trapped ? "trapped" : "normal";
		String suffix = switch (chestType) {
			case SINGLE -> "";
			case LEFT -> "_left";
			case RIGHT -> "_right";
		};
		return TwilightForestMod.prefix(wood.texturePath + "/" + variant + suffix);
	}

	public enum Wood {
		TWILIGHT_OAK("twilight_oak", "twilight_oak"),
		CANOPY("canopy", "canopy"),
		MANGROVE("mangrove", "mangrove"),
		DARK("dark", "darkwood"),
		TIME("time", "time"),
		TRANSFORMATION("transformation", "transformation"),
		MINING("mining", "mining"),
		SORTING("sorting", "sorting");

		private final String blockPath;
		private final String texturePath;

		Wood(String blockPath, String texturePath) {
			this.blockPath = blockPath;
			this.texturePath = texturePath;
		}

		public String blockPath() {
			return this.blockPath;
		}

		public String texturePath() {
			return this.texturePath;
		}

		public String blockId(boolean trapped) {
			return this.blockPath + (trapped ? "_trapped_chest" : "_chest");
		}
	}
}
