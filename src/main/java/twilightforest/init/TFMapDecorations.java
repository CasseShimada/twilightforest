package twilightforest.init;

import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import twilightforest.TwilightForestMod;

public class TFMapDecorations {
	public static final Holder<MapDecorationType> HEDGE_MAZE = register("hedge_maze");
	public static final Holder<MapDecorationType> SMALL_HOLLOW_HILL = register("small_hollow_hill");
	public static final Holder<MapDecorationType> MEDIUM_HOLLOW_HILL = register("medium_hollow_hill");
	public static final Holder<MapDecorationType> LARGE_HOLLOW_HILL = register("large_hollow_hill");
	public static final Holder<MapDecorationType> QUEST_GROVE = register("quest_grove");
	public static final Holder<MapDecorationType> NAGA_COURTYARD = register("naga_courtyard");
	public static final Holder<MapDecorationType> LICH_TOWER = register("lich_tower");
	public static final Holder<MapDecorationType> LABYRINTH = register("labyrinth");
	public static final Holder<MapDecorationType> HYDRA_LAIR = register("hydra_lair");
	public static final Holder<MapDecorationType> KNIGHT_STRONGHOLD = register("knight_stronghold");
	public static final Holder<MapDecorationType> DARK_TOWER = register("dark_tower");
	public static final Holder<MapDecorationType> YETI_LAIR = register("yeti_lair");
	public static final Holder<MapDecorationType> AURORA_PALACE = register("aurora_palace");
	public static final Holder<MapDecorationType> TROLL_CAVES = register("troll_caves");
	public static final Holder<MapDecorationType> FINAL_CASTLE = register("final_castle");

	private static Holder<MapDecorationType> register(String name) {
		Identifier id = TwilightForestMod.prefix(name);
		MapDecorationType value = Registry.register(BuiltInRegistries.MAP_DECORATION_TYPE, id, new MapDecorationType(id, true, -1, false, true));
		return BuiltInRegistries.MAP_DECORATION_TYPE.wrapAsHolder(value);
	}

	public static void init() {
	}
}
