package twilightforest.util;

import net.minecraft.resources.Identifier;
import twilightforest.TwilightForestMod;
import twilightforest.init.*;

public final class TFRemapper {
	private final AliasTarget target;

	private TFRemapper(AliasTarget target) {
		this.target = target;
	}

	public static void addBlockAliases() {
		addAliases(AliasTarget.BLOCK);
	}

	public static void addItemAliases() {
		addAliases(AliasTarget.ITEM);
	}

	public static void addEntityAliases() {
		addAliases(AliasTarget.ENTITY_TYPE);
	}

	public static void addSpawnEggAliases() {
		addAliases(AliasTarget.SPAWN_EGG);
	}

	public static void addStructurePieceAliases() {
		addAliases(AliasTarget.STRUCTURE_PIECE);
	}

	public static void addStructureProcessorAliases() {
		addAliases(AliasTarget.STRUCTURE_PROCESSOR);
	}

	private static void addAliases(AliasTarget target) {
		new TFRemapper(target).addRegistryAliases();
	}

	private void addRegistryAliases() {

		remapBlockAndItem("yeti_trophy", "alpha_yeti_trophy");
		remapBlock("yeti_wall_trophy", "alpha_yeti_wall_trophy");
		remapBlockAndItem("boss_spawner_naga", "naga_boss_spawner");
		remapBlockAndItem("boss_spawner_lich", "lich_boss_spawner");
		remapBlockAndItem("boss_spawner_minoshroom", "minoshroom_boss_spawner");
		remapBlockAndItem("boss_spawner_hydra", "hydra_boss_spawner");
		remapBlockAndItem("boss_spawner_knight_phantom", "knight_phantom_boss_spawner");
		remapBlockAndItem("boss_spawner_ur_ghast", "ur_ghast_boss_spawner");
		remapBlockAndItem("boss_spawner_alpha_yeti", "alpha_yeti_boss_spawner");
		remapBlockAndItem("boss_spawner_snow_queen", "snow_queen_boss_spawner");
		remapBlockAndItem("boss_spawner_final_boss", "final_boss_boss_spawner");

		remapBlockAndItem("etched_nagastone_weathered", "cracked_etched_nagastone");
		remapBlockAndItem("etched_nagastone_mossy", "mossy_etched_nagastone");
		remapBlockAndItem("nagastone_pillar_weathered", "cracked_nagastone_pillar");
		remapBlockAndItem("nagastone_pillar_mossy", "mossy_nagastone_pillar");
		remapBlockAndItem("nagastone_stairs_weathered_left", "cracked_nagastone_stairs_left");
		remapBlockAndItem("nagastone_stairs_mossy_left", "mossy_nagastone_stairs_left");
		remapBlockAndItem("nagastone_stairs_weathered_right", "cracked_nagastone_stairs_right");
		remapBlockAndItem("nagastone_stairs_mossy_right", "mossy_nagastone_stairs_right");
		remapBlockAndItem("naga_stone_head", "nagastone_head");
		remapBlockAndItem("naga_stone", "nagastone");

		remapBlockAndItem("stone_twist", "twisted_stone");
		remapBlockAndItem("stone_twist_thin", "twisted_stone_pillar");
		remapBlockAndItem("stone_pillar_bold", "bold_stone_pillar");
		remapBlockAndItem("empty_bookshelf", "chiseled_canopy_bookshelf");
		remapBlockAndItem("empty_canopy_bookshelf", "chiseled_canopy_bookshelf");
		remapBlockAndItem("death_tome_spawner", "chiseled_canopy_bookshelf");
		remapBlockAndItem("royal_rags", "coronation_carpet");
		remapBlockAndItem("cursed_spawner", "sinister_spawner");

		remapBlockAndItem("huge_lilypad", "huge_lily_pad");
		remapBlockAndItem("huge_waterlily", "huge_water_lily");

		remapBlockAndItem("maze_stone", "mazestone");
		remapBlockAndItem("maze_stone_brick", "mazestone_brick");
		remapBlockAndItem("maze_stone_cracked", "cracked_mazestone");
		remapBlockAndItem("maze_stone_mossy", "mossy_mazestone");
		remapBlockAndItem("maze_stone_decorative", "decorative_mazestone");
		remapBlockAndItem("maze_stone_chiseled", "cut_mazestone");
		remapBlockAndItem("maze_stone_border", "mazestone_border");
		remapBlockAndItem("maze_stone_mosaic", "mazestone_mosaic");

		remapBlockAndItem("underbrick_cracked", "cracked_underbrick");
		remapBlockAndItem("underbrick_mossy", "mossy_underbrick");

		remapBlockAndItem("tower_wood", "towerwood");
		remapBlockAndItem("tower_wood_cracked", "cracked_towerwood");
		remapBlockAndItem("tower_wood_mossy", "mossy_towerwood");
		remapBlockAndItem("tower_wood_infested", "infested_towerwood");
		remapBlockAndItem("tower_wood_encased", "encased_towerwood");

		remapBlockAndItem("deadrock_cracked", "cracked_deadrock");
		remapBlockAndItem("deadrock_weathered", "weathered_deadrock");

		remapBlockAndItem("castle_brick_worn", "worn_castle_brick");
		remapBlockAndItem("castle_brick_cracked", "cracked_castle_brick");
		remapBlockAndItem("castle_brick_mossy", "mossy_castle_brick");
		remapBlockAndItem("castle_brick_frame", "thick_castle_brick");
		remapBlockAndItem("castle_brick_roof", "castle_roof_tile");
		remapBlockAndItem("castle_pillar_encased", "encased_castle_brick_pillar");
		remapBlockAndItem("castle_pillar_encased_tile", "encased_castle_brick_tile");
		remapBlockAndItem("castle_pillar_bold", "bold_castle_brick_pillar");
		remapBlockAndItem("castle_pillar_bold_tile", "bold_castle_brick_tile");
		remapBlockAndItem("castle_stairs_brick", "castle_brick_stairs");
		remapBlockAndItem("castle_stairs_worn", "worn_castle_brick_stairs");
		remapBlockAndItem("castle_stairs_cracked", "cracked_castle_brick_stairs");
		remapBlockAndItem("castle_stairs_mossy", "mossy_castle_brick_stairs");
		remapBlockAndItem("castle_stairs_encased", "encased_castle_brick_stairs");
		remapBlockAndItem("castle_stairs_bold", "bold_castle_brick_stairs");
		remapBlockAndItem("castle_rune_brick_pink", "pink_castle_rune_brick");
		remapBlockAndItem("castle_rune_brick_yellow", "yellow_castle_rune_brick");
		remapBlockAndItem("castle_rune_brick_blue", "blue_castle_rune_brick");
		remapBlockAndItem("castle_rune_brick_purple", "violet_castle_rune_brick");
		remapBlockAndItem("castle_door_pink", "pink_castle_door");
		remapBlockAndItem("castle_door_yellow", "yellow_castle_door");
		remapBlockAndItem("castle_door_blue", "blue_castle_door");
		remapBlockAndItem("castle_door_purple", "violet_castle_door");
		remapBlockAndItem("force_field_pink", "pink_force_field");
		remapBlockAndItem("force_field_orange", "orange_force_field");
		remapBlockAndItem("force_field_green", "green_force_field");
		remapBlockAndItem("force_field_blue", "blue_force_field");
		remapBlockAndItem("force_field_purple", "violet_force_field");

		remapBlockAndItem("rainboak_leaves", "rainbow_oak_leaves");
		remapBlockAndItem("rainboak_sapling", "rainbow_oak_sapling");
		remapBlock("potted_rainboak_sapling", "potted_rainbow_oak_sapling");

		remapBlockAndItem("dark_gate", "dark_fence_gate");
		remapBlockAndItem("dark_plate", "dark_pressure_plate");
		remapBlockAndItem("darkwood_sign", "dark_sign");
		remapBlock("darkwood_wall_sign", "dark_wall_sign");
		remapBlockAndItem("darkwood_banister", "dark_banister");

		remapBlockAndItem("trans_planks", "transformation_planks");
		remapBlockAndItem("trans_slab", "transformation_slab");
		remapBlockAndItem("trans_stairs", "transformation_stairs");
		remapBlockAndItem("trans_button", "transformation_button");
		remapBlockAndItem("trans_fence", "transformation_fence");
		remapBlockAndItem("trans_gate", "transformation_fence_gate");
		remapBlockAndItem("trans_plate", "transformation_pressure_plate");
		remapBlockAndItem("trans_door", "transformation_door");
		remapBlockAndItem("trans_trapdoor", "transformation_trapdoor");
		remapBlockAndItem("trans_sign", "transformation_sign");
		remapBlock("trans_wall_sign", "transformation_wall_sign");
		remapBlockAndItem("trans_banister", "transformation_banister");

		remapBlockAndItem("mine_planks", "mining_planks");
		remapBlockAndItem("mine_slab", "mining_slab");
		remapBlockAndItem("mine_stairs", "mining_stairs");
		remapBlockAndItem("mine_button", "mining_button");
		remapBlockAndItem("mine_fence", "mining_fence");
		remapBlockAndItem("mine_gate", "mining_fence_gate");
		remapBlockAndItem("mine_plate", "mining_pressure_plate");
		remapBlockAndItem("mine_door", "mining_door");
		remapBlockAndItem("mine_trapdoor", "mining_trapdoor");
		remapBlockAndItem("mine_sign", "mining_sign");
		remapBlock("mine_wall_sign", "mining_wall_sign");
		remapBlockAndItem("mine_banister", "mining_banister");

		remapBlockAndItem("sort_planks", "sorting_planks");
		remapBlockAndItem("sort_slab", "sorting_slab");
		remapBlockAndItem("sort_stairs", "sorting_stairs");
		remapBlockAndItem("sort_button", "sorting_button");
		remapBlockAndItem("sort_fence", "sorting_fence");
		remapBlockAndItem("sort_gate", "sorting_fence_gate");
		remapBlockAndItem("sort_plate", "sorting_pressure_plate");
		remapBlockAndItem("sort_door", "sorting_door");
		remapBlockAndItem("sort_trapdoor", "sorting_trapdoor");
		remapBlockAndItem("sort_sign", "sorting_sign");
		remapBlock("sort_wall_sign", "sorting_wall_sign");
		remapBlockAndItem("sort_banister", "sorting_banister");

		remapItem("shield_scepter", "fortification_scepter");
		remapItem("magic_map", "filled_magic_map");
		remapItem("maze_map", "filled_maze_map");
		remapItem("ore_map", "filled_ore_map");
		remapItem("magic_map_empty", "magic_map");
		remapItem("maze_map_empty", "maze_map");
		remapItem("ore_map_empty", "ore_map");
		remapItem("ironwood_raw", "raw_ironwood");
		remapItem("minotaur_axe_gold", "gold_minotaur_axe");
		remapItem("minotaur_axe", "diamond_minotaur_axe");
		remapItem("peacock_fan", "peacock_feather_fan");
		remapItem("alpha_fur", "alpha_yeti_fur");
		remapItem("questing_ram_banner_pattern", "quest_ram_banner_pattern");

		remapSpawnEgg("bunny_spawn_egg", "dwarf_rabbit_spawn_egg");
		remapSpawnEgg("goblin_knight_lower_spawn_egg", "lower_goblin_knight_spawn_egg");
		remapSpawnEgg("mini_ghast_spawn_egg", "carminite_ghastling_spawn_egg");
		remapSpawnEgg("tower_ghast_spawn_egg", "carminite_ghastguard_spawn_egg");
		remapSpawnEgg("tower_golem_spawn_egg", "carminite_golem_spawn_egg");
		remapSpawnEgg("tower_broodling_spawn_egg", "carminite_broodling_spawn_egg");
		remapSpawnEgg("tower_termite_spawn_egg", "towerwood_borer_spawn_egg");
		remapSpawnEgg("wild_boar_spawn_egg", "boar_spawn_egg");
		remapSpawnEgg("yeti_alpha_spawn_egg", "alpha_yeti_spawn_egg");

		remapEntity("wild_boar", "boar");
		remapEntity("bunny", "dwarf_rabbit");
		remapEntity("mini_ghast", "carminite_ghastling");
		remapEntity("tower_ghast", "carminite_ghastguard");
		remapEntity("tower_golem", "carminite_golem");
		remapEntity("tower_broodling", "carminite_broodling");
		remapEntity("tower_termite", "towerwood_borer");
		remapEntity("goblin_knight_upper", "upper_goblin_knight");
		remapEntity("goblin_knight_lower", "lower_goblin_knight");
		remapEntity("yeti_alpha", "alpha_yeti");

		remapStructurePiece("TFNCTr", "TFNCTe"); // Terrace Brazier
		remapStructurePiece("TFNCDu", "TFNCTe"); // Terrace Duct
		remapStructurePiece("TFNCSt", "TFNCTe"); // Terrace Statue

		remapStructureProcessor(TwilightForestMod.prefix("meta_block_processor"), Identifier.withDefaultNamespace("jigsaw_replacement"));
	}

	private void remapBlockAndItem(String oldId, String newId) {
		remapBlock(oldId, newId);
		remapItem(oldId, newId);
	}

	private void remapBlock(String oldId, String newId) {
		if (this.target == AliasTarget.BLOCK) {
			TFBlocks.addAlias(TwilightForestMod.prefix(oldId), TwilightForestMod.prefix(newId));
		}
	}

	private void remapItem(String oldId, String newId) {
		if (this.target == AliasTarget.ITEM) {
			TFItems.addAlias(TwilightForestMod.prefix(oldId), TwilightForestMod.prefix(newId));
		}
	}

	private void remapEntity(String oldId, String newId) {
		if (this.target == AliasTarget.ENTITY_TYPE) {
			TFEntities.addEntityAlias(TwilightForestMod.prefix(oldId), TwilightForestMod.prefix(newId));
		}
	}

	private void remapSpawnEgg(String oldId, String newId) {
		if (this.target == AliasTarget.SPAWN_EGG) {
			TFEntities.addSpawnEggAlias(TwilightForestMod.prefix(oldId), TwilightForestMod.prefix(newId));
		}
	}

	private void remapStructurePiece(String oldId, String newId) {
		if (this.target == AliasTarget.STRUCTURE_PIECE) {
			TFStructurePieceTypes.addAlias(TwilightForestMod.prefix(oldId), TwilightForestMod.prefix(newId));
		}
	}

	private void remapStructureProcessor(Identifier oldId, Identifier newId) {
		if (this.target == AliasTarget.STRUCTURE_PROCESSOR) {
			TFStructureProcessors.addAlias(oldId, newId);
		}
	}

	private enum AliasTarget {
		BLOCK,
		ITEM,
		ENTITY_TYPE,
		SPAWN_EGG,
		STRUCTURE_PIECE,
		STRUCTURE_PROCESSOR
	}
}
