package twilightforest.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import twilightforest.TwilightForestMod;

import java.util.List;

public class TFStats {

	public static final Identifier BUGS_SQUISHED = register("bugs_squished");
	public static final Identifier UNCRAFTING_TABLE_INTERACTIONS = register("uncrafting_table_interactions");
	public static final Identifier TROPHY_PEDESTALS_ACTIVATED = register("trophy_pedestals_activated");
	public static final Identifier E115_SLICES_EATEN = register("e115_slices_eaten");
	public static final Identifier TORCHBERRIES_HARVESTED = register("torchberries_harvested");
	public static final Identifier BLOCKS_CRUMBLED = register("blocks_crumbled");
	public static final Identifier LIFE_CHARMS_ACTIVATED = register("life_charms_activated");
	public static final Identifier KEEPING_CHARMS_ACTIVATED = register("keeping_charms_activated");
	public static final Identifier SKULL_CANDLES_MADE = register("skull_candles_made");
	public static final Identifier TF_SHIELDS_BROKEN = register("tf_shields_broken");

	private static Identifier register(String key) {
		Identifier id = TwilightForestMod.prefix(key);
		return Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
	}

	public static void init() {
		List.of(
			BUGS_SQUISHED,
			UNCRAFTING_TABLE_INTERACTIONS,
			TROPHY_PEDESTALS_ACTIVATED,
			E115_SLICES_EATEN,
			TORCHBERRIES_HARVESTED,
			BLOCKS_CRUMBLED,
			LIFE_CHARMS_ACTIVATED,
			KEEPING_CHARMS_ACTIVATED,
			SKULL_CANDLES_MADE,
			TF_SHIELDS_BROKEN
		).forEach(stat -> Stats.CUSTOM.get(stat, StatFormatter.DEFAULT));
	}
}
