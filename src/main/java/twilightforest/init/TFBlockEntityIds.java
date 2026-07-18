package twilightforest.init;

import net.minecraft.resources.Identifier;
import twilightforest.TwilightForestMod;

import java.util.Map;

final class TFBlockEntityIds {
	static final String CHEST = "chest";
	static final String TRAPPED_CHEST = "trapped_chest";
	static final Map<Identifier, Identifier> ALIASES = Map.of(
		TwilightForestMod.prefix("tf_chest"), TwilightForestMod.prefix(CHEST),
		TwilightForestMod.prefix("tf_trapped_chest"), TwilightForestMod.prefix(TRAPPED_CHEST),
		TwilightForestMod.prefix("tome_spawner"), TwilightForestMod.prefix("chiseled_canopy_bookshelf")
	);

	private TFBlockEntityIds() {
	}
}
