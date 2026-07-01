package twilightforest.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import twilightforest.TwilightForestMod;

import java.util.LinkedHashMap;
import java.util.Map;

public class TFMapDecorations {
	private static final Map<Identifier, MapDecorationType> DECORATIONS = new LinkedHashMap<>();
	private static boolean registered;

	public static final MapDecorationType HEDGE_MAZE = makeDecoration("hedge_maze");
	public static final MapDecorationType SMALL_HOLLOW_HILL = makeDecoration("small_hollow_hill");
	public static final MapDecorationType MEDIUM_HOLLOW_HILL = makeDecoration("medium_hollow_hill");
	public static final MapDecorationType LARGE_HOLLOW_HILL = makeDecoration("large_hollow_hill");
	public static final MapDecorationType QUEST_GROVE = makeDecoration("quest_grove");
	public static final MapDecorationType NAGA_COURTYARD = makeDecoration("naga_courtyard");
	public static final MapDecorationType LICH_TOWER = makeDecoration("lich_tower");
	public static final MapDecorationType LABYRINTH = makeDecoration("labyrinth");
	public static final MapDecorationType HYDRA_LAIR = makeDecoration("hydra_lair");
	public static final MapDecorationType KNIGHT_STRONGHOLD = makeDecoration("knight_stronghold");
	public static final MapDecorationType DARK_TOWER = makeDecoration("dark_tower");
	public static final MapDecorationType YETI_LAIR = makeDecoration("yeti_lair");
	public static final MapDecorationType AURORA_PALACE = makeDecoration("aurora_palace");
	public static final MapDecorationType TROLL_CAVES = makeDecoration("troll_caves");
	public static final MapDecorationType FINAL_CASTLE = makeDecoration("final_castle");

	private static MapDecorationType makeDecoration(String name) {
		Identifier id = TwilightForestMod.prefix(name);
		MapDecorationType decoration = new MapDecorationType(id, true, -1, false, true);
		DECORATIONS.put(id, decoration);
		return decoration;
	}

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		DECORATIONS.forEach((id, decoration) -> Registry.register(BuiltInRegistries.MAP_DECORATION_TYPE, id, decoration));
	}
}
