package twilightforest.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import twilightforest.util.registry.DeferredRegister;
import twilightforest.TwilightForestMod;
import twilightforest.init.*;

public class TFRemapper {

	public static void addRegistryAliases() {
		DeferredRegister<Block> blockReg = TFBlocks.BLOCKS;
		DeferredRegister<EntityType<?>> entityReg = TFEntities.ENTITY_TYPES;
		DeferredRegister<Item> spawnEggReg = TFEntities.SPAWN_EGGS;

		remapBlockAndItem(blockReg, "yeti_trophy", "alpha_yeti_trophy");
		remapBlockAndItem(blockReg, "yeti_wall_trophy", "alpha_yeti_wall_trophy");
		remapBlockAndItem(blockReg, "boss_spawner_naga", "naga_boss_spawner");
		remapBlockAndItem(blockReg, "boss_spawner_lich", "lich_boss_spawner");
		remapBlockAndItem(blockReg, "boss_spawner_minoshroom", "minoshroom_boss_spawner");
		remapBlockAndItem(blockReg, "boss_spawner_hydra", "hydra_boss_spawner");
		remapBlockAndItem(blockReg, "boss_spawner_knight_phantom", "knight_phantom_boss_spawner");
		remapBlockAndItem(blockReg, "boss_spawner_ur_ghast", "ur_ghast_boss_spawner");
		remapBlockAndItem(blockReg, "boss_spawner_alpha_yeti", "alpha_yeti_boss_spawner");
		remapBlockAndItem(blockReg, "boss_spawner_snow_queen", "snow_queen_boss_spawner");
		remapBlockAndItem(blockReg, "boss_spawner_final_boss", "final_boss_boss_spawner");

		remapBlockAndItem(blockReg, "etched_nagastone_weathered", "cracked_etched_nagastone");
		remapBlockAndItem(blockReg, "etched_nagastone_mossy", "mossy_etched_nagastone");
		remapBlockAndItem(blockReg, "nagastone_pillar_weathered", "cracked_nagastone_pillar");
		remapBlockAndItem(blockReg, "nagastone_pillar_mossy", "mossy_nagastone_pillar");
		remapBlockAndItem(blockReg, "nagastone_stairs_weathered_left", "cracked_nagastone_stairs_left");
		remapBlockAndItem(blockReg, "nagastone_stairs_mossy_left", "mossy_nagastone_stairs_left");
		remapBlockAndItem(blockReg, "nagastone_stairs_weathered_right", "cracked_nagastone_stairs_right");
		remapBlockAndItem(blockReg, "nagastone_stairs_mossy_right", "mossy_nagastone_stairs_right");
		remapBlockAndItem(blockReg, "naga_stone_head", "nagastone_head");
		remapBlockAndItem(blockReg, "naga_stone", "nagastone");

		remapBlockAndItem(blockReg, "stone_twist", "twisted_stone");
		remapBlockAndItem(blockReg, "stone_twist_thin", "twisted_stone_pillar");
		remapBlockAndItem(blockReg, "stone_pillar_bold", "bold_stone_pillar");
		remapBlockAndItem(blockReg, "empty_bookshelf", "empty_canopy_bookshelf");
		remapBlockAndItem(blockReg, "royal_rags", "coronation_carpet");
		remapBlockAndItem(blockReg, "cursed_spawner", "sinister_spawner");

		remapBlockAndItem(blockReg, "huge_lilypad", "huge_lily_pad");
		remapBlockAndItem(blockReg, "huge_waterlily", "huge_water_lily");

		remapBlockAndItem(blockReg, "maze_stone", "mazestone");
		remapBlockAndItem(blockReg, "maze_stone_brick", "mazestone_brick");
		remapBlockAndItem(blockReg, "maze_stone_cracked", "cracked_mazestone");
		remapBlockAndItem(blockReg, "maze_stone_mossy", "mossy_mazestone");
		remapBlockAndItem(blockReg, "maze_stone_decorative", "decorative_mazestone");
		remapBlockAndItem(blockReg, "maze_stone_chiseled", "cut_mazestone");
		remapBlockAndItem(blockReg, "maze_stone_border", "mazestone_border");
		remapBlockAndItem(blockReg, "maze_stone_mosaic", "mazestone_mosaic");

		remapBlockAndItem(blockReg, "underbrick_cracked", "cracked_underbrick");
		remapBlockAndItem(blockReg, "underbrick_mossy", "mossy_underbrick");

		remapBlockAndItem(blockReg, "tower_wood", "towerwood");
		remapBlockAndItem(blockReg, "tower_wood_cracked", "cracked_towerwood");
		remapBlockAndItem(blockReg, "tower_wood_mossy", "mossy_towerwood");
		remapBlockAndItem(blockReg, "tower_wood_infested", "infested_towerwood");
		remapBlockAndItem(blockReg, "tower_wood_encased", "encased_towerwood");

		remapBlockAndItem(blockReg, "deadrock_cracked", "cracked_deadrock");
		remapBlockAndItem(blockReg, "deadrock_weathered", "weathered_deadrock");

		remapBlockAndItem(blockReg, "castle_brick_worn", "worn_castle_brick");
		remapBlockAndItem(blockReg, "castle_brick_cracked", "cracked_castle_brick");
		remapBlockAndItem(blockReg, "castle_brick_mossy", "mossy_castle_brick");
		remapBlockAndItem(blockReg, "castle_brick_frame", "thick_castle_brick");
		remapBlockAndItem(blockReg, "castle_brick_roof", "castle_roof_tile");
		remapBlockAndItem(blockReg, "castle_pillar_encased", "encased_castle_brick_pillar");
		remapBlockAndItem(blockReg, "castle_pillar_encased_tile", "encased_castle_brick_tile");
		remapBlockAndItem(blockReg, "castle_pillar_bold", "bold_castle_brick_pillar");
		remapBlockAndItem(blockReg, "castle_pillar_bold_tile", "bold_castle_brick_tile");
		remapBlockAndItem(blockReg, "castle_stairs_brick", "castle_brick_stairs");
		remapBlockAndItem(blockReg, "castle_stairs_worn", "worn_castle_brick_stairs");
		remapBlockAndItem(blockReg, "castle_stairs_cracked", "cracked_castle_brick_stairs");
		remapBlockAndItem(blockReg, "castle_stairs_mossy", "mossy_castle_brick_stairs");
		remapBlockAndItem(blockReg, "castle_stairs_encased", "encased_castle_brick_stairs");
		remapBlockAndItem(blockReg, "castle_stairs_bold", "bold_castle_brick_stairs");
		remapBlockAndItem(blockReg, "castle_rune_brick_pink", "pink_castle_rune_brick");
		remapBlockAndItem(blockReg, "castle_rune_brick_yellow", "yellow_castle_rune_brick");
		remapBlockAndItem(blockReg, "castle_rune_brick_blue", "blue_castle_rune_brick");
		remapBlockAndItem(blockReg, "castle_rune_brick_purple", "violet_castle_rune_brick");
		remapBlockAndItem(blockReg, "castle_door_pink", "pink_castle_door");
		remapBlockAndItem(blockReg, "castle_door_yellow", "yellow_castle_door");
		remapBlockAndItem(blockReg, "castle_door_blue", "blue_castle_door");
		remapBlockAndItem(blockReg, "castle_door_purple", "violet_castle_door");
		remapBlockAndItem(blockReg, "force_field_pink", "pink_force_field");
		remapBlockAndItem(blockReg, "force_field_orange", "orange_force_field");
		remapBlockAndItem(blockReg, "force_field_green", "green_force_field");
		remapBlockAndItem(blockReg, "force_field_blue", "blue_force_field");
		remapBlockAndItem(blockReg, "force_field_purple", "violet_force_field");

		remapBlockAndItem(blockReg, "rainboak_leaves", "rainbow_oak_leaves");
		remapBlockAndItem(blockReg, "rainboak_sapling", "rainbow_oak_sapling");
		remapBlockAndItem(blockReg, "potted_rainboak_sapling", "potted_rainbow_oak_sapling");

		remapBlockAndItem(blockReg, "dark_gate", "dark_fence_gate");
		remapBlockAndItem(blockReg, "dark_plate", "dark_pressure_plate");
		remapBlockAndItem(blockReg, "darkwood_sign", "dark_sign");
		remapBlockAndItem(blockReg, "darkwood_wall_sign", "dark_wall_sign");
		remapBlockAndItem(blockReg, "darkwood_banister", "dark_banister");

		remapBlockAndItem(blockReg, "trans_planks", "transformation_planks");
		remapBlockAndItem(blockReg, "trans_slab", "transformation_slab");
		remapBlockAndItem(blockReg, "trans_stairs", "transformation_stairs");
		remapBlockAndItem(blockReg, "trans_button", "transformation_button");
		remapBlockAndItem(blockReg, "trans_fence", "transformation_fence");
		remapBlockAndItem(blockReg, "trans_gate", "transformation_fence_gate");
		remapBlockAndItem(blockReg, "trans_plate", "transformation_pressure_plate");
		remapBlockAndItem(blockReg, "trans_door", "transformation_door");
		remapBlockAndItem(blockReg, "trans_trapdoor", "transformation_trapdoor");
		remapBlockAndItem(blockReg, "trans_sign", "transformation_sign");
		remapBlockAndItem(blockReg, "trans_wall_sign", "transformation_wall_sign");
		remapBlockAndItem(blockReg, "trans_banister", "transformation_banister");

		remapBlockAndItem(blockReg, "mine_planks", "mining_planks");
		remapBlockAndItem(blockReg, "mine_slab", "mining_slab");
		remapBlockAndItem(blockReg, "mine_stairs", "mining_stairs");
		remapBlockAndItem(blockReg, "mine_button", "mining_button");
		remapBlockAndItem(blockReg, "mine_fence", "mining_fence");
		remapBlockAndItem(blockReg, "mine_gate", "mining_fence_gate");
		remapBlockAndItem(blockReg, "mine_plate", "mining_pressure_plate");
		remapBlockAndItem(blockReg, "mine_door", "mining_door");
		remapBlockAndItem(blockReg, "mine_trapdoor", "mining_trapdoor");
		remapBlockAndItem(blockReg, "mine_sign", "mining_sign");
		remapBlockAndItem(blockReg, "mine_wall_sign", "mining_wall_sign");
		remapBlockAndItem(blockReg, "mine_banister", "mining_banister");

		remapBlockAndItem(blockReg, "sort_planks", "sorting_planks");
		remapBlockAndItem(blockReg, "sort_slab", "sorting_slab");
		remapBlockAndItem(blockReg, "sort_stairs", "sorting_stairs");
		remapBlockAndItem(blockReg, "sort_button", "sorting_button");
		remapBlockAndItem(blockReg, "sort_fence", "sorting_fence");
		remapBlockAndItem(blockReg, "sort_gate", "sorting_fence_gate");
		remapBlockAndItem(blockReg, "sort_plate", "sorting_pressure_plate");
		remapBlockAndItem(blockReg, "sort_door", "sorting_door");
		remapBlockAndItem(blockReg, "sort_trapdoor", "sorting_trapdoor");
		remapBlockAndItem(blockReg, "sort_sign", "sorting_sign");
		remapBlockAndItem(blockReg, "sort_wall_sign", "sorting_wall_sign");
		remapBlockAndItem(blockReg, "sort_banister", "sorting_banister");

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

		remapEntry(spawnEggReg, "bunny_spawn_egg", "dwarf_rabbit_spawn_egg");
		remapEntry(spawnEggReg, "goblin_knight_lower_spawn_egg", "lower_goblin_knight_spawn_egg");
		remapEntry(spawnEggReg, "mini_ghast_spawn_egg", "carminite_ghastling_spawn_egg");
		remapEntry(spawnEggReg, "tower_ghast_spawn_egg", "carminite_ghastguard_spawn_egg");
		remapEntry(spawnEggReg, "tower_golem_spawn_egg", "carminite_golem_spawn_egg");
		remapEntry(spawnEggReg, "tower_broodling_spawn_egg", "carminite_broodling_spawn_egg");
		remapEntry(spawnEggReg, "tower_termite_spawn_egg", "towerwood_borer_spawn_egg");
		remapEntry(spawnEggReg, "wild_boar_spawn_egg", "boar_spawn_egg");
		remapEntry(spawnEggReg, "yeti_alpha_spawn_egg", "alpha_yeti_spawn_egg");

		remapEntry(entityReg, "wild_boar", "boar");
		remapEntry(entityReg, "bunny", "dwarf_rabbit");
		remapEntry(entityReg, "mini_ghast", "carminite_ghastling");
		remapEntry(entityReg, "tower_ghast", "carminite_ghastguard");
		remapEntry(entityReg, "tower_golem", "carminite_golem");
		remapEntry(entityReg, "tower_broodling", "carminite_broodling");
		remapEntry(entityReg, "tower_termite", "towerwood_borer");
		remapEntry(entityReg, "goblin_knight_upper", "upper_goblin_knight");
		remapEntry(entityReg, "goblin_knight_lower", "lower_goblin_knight");
		remapEntry(entityReg, "yeti_alpha", "alpha_yeti");

		remapStructurePiece("TFNCTr", "TFNCTe"); // Terrace Brazier
		remapStructurePiece("TFNCDu", "TFNCTe"); // Terrace Duct
		remapStructurePiece("TFNCSt", "TFNCTe"); // Terrace Statue

		TFStructureProcessors.addAlias(TwilightForestMod.prefix("meta_block_processor"), Identifier.withDefaultNamespace("jigsaw_replacement"));
	}

	private static void remapEntry(DeferredRegister<?> registry, String oldId, String newId) {
		registry.addAlias(TwilightForestMod.prefix(oldId), TwilightForestMod.prefix(newId));
	}

	private static void remapBlockAndItem(DeferredRegister<Block> blockRegistry, String oldId, String newId) {
		remapEntry(blockRegistry, oldId, newId);
		remapItem(oldId, newId);
	}

	private static void remapItem(String oldId, String newId) {
		TFItems.addAlias(TwilightForestMod.prefix(oldId), TwilightForestMod.prefix(newId));
	}

	private static void remapStructurePiece(String oldId, String newId) {
		TFStructurePieceTypes.addAlias(TwilightForestMod.prefix(oldId), TwilightForestMod.prefix(newId));
	}
}
