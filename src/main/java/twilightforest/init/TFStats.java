package twilightforest.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import twilightforest.TwilightForestMod;

import java.util.ArrayList;
import java.util.List;

public class TFStats {

	private static final List<Identifier> CUSTOM_STATS = new ArrayList<>();
	private static boolean registered;

	public static final Identifier BUGS_SQUISHED = makeTFStat("bugs_squished");
	public static final Identifier UNCRAFTING_TABLE_INTERACTIONS = makeTFStat("uncrafting_table_interactions");
	public static final Identifier TROPHY_PEDESTALS_ACTIVATED = makeTFStat("trophy_pedestals_activated");
	public static final Identifier E115_SLICES_EATEN = makeTFStat("e115_slices_eaten");
	public static final Identifier TORCHBERRIES_HARVESTED = makeTFStat("torchberries_harvested");
	public static final Identifier BLOCKS_CRUMBLED = makeTFStat("blocks_crumbled");
	public static final Identifier LIFE_CHARMS_ACTIVATED = makeTFStat("life_charms_activated");
	public static final Identifier KEEPING_CHARMS_ACTIVATED = makeTFStat("keeping_charms_activated");
	public static final Identifier SKULL_CANDLES_MADE = makeTFStat("skull_candles_made");
	public static final Identifier TF_SHIELDS_BROKEN = makeTFStat("tf_shields_broken");

	private static Identifier makeTFStat(String key) {
		Identifier resourcelocation = TwilightForestMod.prefix(key);
		CUSTOM_STATS.add(resourcelocation);
		return resourcelocation;
	}

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		CUSTOM_STATS.forEach(stat -> Registry.register(BuiltInRegistries.CUSTOM_STAT, stat, stat));
	}

	public static void init() {
		CUSTOM_STATS.forEach(stat -> Stats.CUSTOM.get(stat, StatFormatter.DEFAULT));
	}
}
