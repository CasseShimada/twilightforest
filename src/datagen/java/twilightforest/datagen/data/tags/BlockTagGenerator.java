package twilightforest.datagen.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import twilightforest.init.TFBlocks;
import twilightforest.tags.TFBlockTags;
import twilightforest.util.TFBlockFamilies;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class BlockTagGenerator extends FabricTagsProvider.BlockTagsProvider {
	private static final TagKey<Block> FIRE = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("fire"));
	private static final TagKey<Block> LOGS_THAT_BURN = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("logs_that_burn"));
	private static final TagKey<Block> SAPLINGS = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("saplings"));

	public BlockTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.builder(BlockTags.ENCHANTMENT_POWER_PROVIDER)
			.add(key(TFBlocks.CANOPY_BOOKSHELF));

		this.builder(BlockTags.DIRT).add(key(TFBlocks.UBEROUS_SOIL));
		this.builder(BlockTags.OVERRIDES_MUSHROOM_LIGHT_REQUIREMENT).add(key(TFBlocks.UBEROUS_SOIL));
		this.builder(BlockTags.SUPPORTS_SMALL_DRIPLEAF).add(key(TFBlocks.UBEROUS_SOIL));

		this.builder(BlockTags.FROG_PREFER_JUMP_TO).add(key(TFBlocks.HUGE_LILY_PAD));
		this.builder(BlockTags.INSIDE_STEP_SOUND_BLOCKS).add(key(TFBlocks.HUGE_LILY_PAD));
		this.builder(BlockTags.SWORD_EFFICIENT).add(key(TFBlocks.HUGE_LILY_PAD));

		this.builder(BlockTags.INVALID_SPAWN_INSIDE).add(key(TFBlocks.TWILIGHT_PORTAL));
		this.builder(BlockTags.PORTALS).add(key(TFBlocks.TWILIGHT_PORTAL));

		this.builder(BlockTags.OCCLUDES_VIBRATION_SIGNALS).add(key(TFBlocks.ARCTIC_FUR_BLOCK));
		this.builder(BlockTags.STRIDER_WARM_BLOCKS).add(key(TFBlocks.FIERY_BLOCK));
		this.builder(BlockTags.WALLS).add(key(TFBlocks.WROUGHT_IRON_FENCE));
		this.builder(BlockTags.WOOL_CARPETS).add(key(TFBlocks.CORONATION_CARPET));

		this.builder(BlockTags.OVERWORLD_CARVER_REPLACEABLES).add(key(TFBlocks.TROLLSTEINN));
		this.builder(BlockTags.NEEDS_IRON_TOOL)
			.add(key(TFBlocks.FIERY_BLOCK), key(TFBlocks.KNIGHTMETAL_BLOCK));
		this.builder(BlockTags.NEEDS_STONE_TOOL).add(
			key(TFBlocks.UNDERBRICK),
			key(TFBlocks.CRACKED_UNDERBRICK),
			key(TFBlocks.MOSSY_UNDERBRICK),
			key(TFBlocks.UNDERBRICK_FLOOR),
			key(TFBlocks.IRON_LADDER)
		);
		this.builder(BlockTags.MOSS_REPLACEABLE)
			.add(key(TFBlocks.ROOT_BLOCK), key(TFBlocks.LIVEROOT_BLOCK), key(TFBlocks.TROLLSTEINN));
		this.builder(FIRE).add(key(TFBlocks.OMINOUS_FIRE));

		this.builder(TFBlockTags.TOWERWOOD)
			.add(key(TFBlocks.TOWERWOOD), key(TFBlocks.MOSSY_TOWERWOOD), key(TFBlocks.CRACKED_TOWERWOOD), key(TFBlocks.INFESTED_TOWERWOOD));
		this.addCloudTags();
		this.addProtectionTags();
		this.addProtectedMaterialTags();
		this.addPortalTags();
		this.addGameplayBehaviorTags();
		this.addWorldgenBehaviorTags();
		this.addTreeTags();
		this.addLogTags();
		this.addHollowLogTags();
		this.addWoodFamilyTags();
		this.addBanisterTags();
		this.addFunctionalBlockTags();
		this.addShapeTags();
		this.addStorageBlockTags();
		this.addConventionTags();
		this.addMiningTags();
	}

	private void addCloudTags() {
		this.builder(TFBlockTags.CLOUDS).add(
			key(TFBlocks.FLUFFY_CLOUD),
			key(TFBlocks.WISPY_CLOUD),
			key(TFBlocks.RAINY_CLOUD),
			key(TFBlocks.SNOWY_CLOUD)
		);
		this.builder(BlockTags.DAMPENS_VIBRATIONS)
			.addTag(TFBlockTags.CLOUDS)
			.add(key(TFBlocks.ARCTIC_FUR_BLOCK));
	}

	private void addProtectionTags() {
		this.builder(TFBlockTags.COMMON_PROTECTIONS).add(
			key(TFBlocks.NAGA_BOSS_SPAWNER),
			key(TFBlocks.LICH_BOSS_SPAWNER),
			key(TFBlocks.MINOSHROOM_BOSS_SPAWNER),
			key(TFBlocks.HYDRA_BOSS_SPAWNER),
			key(TFBlocks.KNIGHT_PHANTOM_BOSS_SPAWNER),
			key(TFBlocks.UR_GHAST_BOSS_SPAWNER),
			key(TFBlocks.ALPHA_YETI_BOSS_SPAWNER),
			key(TFBlocks.SNOW_QUEEN_BOSS_SPAWNER),
			key(TFBlocks.FINAL_BOSS_BOSS_SPAWNER),
			key(TFBlocks.STRONGHOLD_SHIELD),
			key(TFBlocks.UNBREAKABLE_VANISHING_BLOCK),
			key(TFBlocks.LOCKED_VANISHING_BLOCK),
			key(TFBlocks.PINK_FORCE_FIELD),
			key(TFBlocks.ORANGE_FORCE_FIELD),
			key(TFBlocks.GREEN_FORCE_FIELD),
			key(TFBlocks.BLUE_FORCE_FIELD),
			key(TFBlocks.VIOLET_FORCE_FIELD),
			key(TFBlocks.SKULL_CHEST),
			key(TFBlocks.KEEPSAKE_CASKET),
			key(TFBlocks.TROPHY_PEDESTAL),
			key(Blocks.BARRIER),
			key(Blocks.BEDROCK),
			key(Blocks.END_PORTAL),
			key(Blocks.END_PORTAL_FRAME),
			key(Blocks.END_GATEWAY),
			key(Blocks.COMMAND_BLOCK),
			key(Blocks.REPEATING_COMMAND_BLOCK),
			key(Blocks.CHAIN_COMMAND_BLOCK),
			key(Blocks.STRUCTURE_BLOCK),
			key(Blocks.JIGSAW),
			key(Blocks.MOVING_PISTON),
			key(Blocks.LIGHT),
			key(Blocks.REINFORCED_DEEPSLATE)
		);
		this.builder(TFBlockTags.ANTIBUILDER_IGNORES)
			.add(
				key(Blocks.REDSTONE_LAMP),
				key(Blocks.TNT),
				key(Blocks.WATER),
				key(TFBlocks.ANTIBUILDER),
				key(TFBlocks.CARMINITE_BUILDER),
				key(TFBlocks.BUILT_BLOCK),
				key(TFBlocks.REACTOR_DEBRIS),
				key(TFBlocks.CARMINITE_REACTOR),
				key(TFBlocks.REAPPEARING_BLOCK),
				key(TFBlocks.GHAST_TRAP),
				key(TFBlocks.FAKE_DIAMOND),
				key(TFBlocks.FAKE_GOLD)
			)
			.addTag(TFBlockTags.COMMON_PROTECTIONS)
			.addOptional(externalKey("gravestone", "gravestone"));
		this.builder(TFBlockTags.CARMINITE_REACTOR_IMMUNE).addTag(TFBlockTags.COMMON_PROTECTIONS);
		this.builder(BlockTags.DRAGON_IMMUNE)
			.addTag(TFBlockTags.COMMON_PROTECTIONS)
			.add(key(TFBlocks.GIANT_OBSIDIAN), key(TFBlocks.FAKE_DIAMOND), key(TFBlocks.FAKE_GOLD));
		this.builder(BlockTags.FEATURES_CANNOT_REPLACE)
			.addTag(TFBlockTags.COMMON_PROTECTIONS)
			.add(key(TFBlocks.LIVEROOT_BLOCK), key(TFBlocks.MANGROVE_ROOT), key(TFBlocks.SINISTER_SPAWNER));
		this.builder(BlockTags.WITHER_IMMUNE)
			.addTag(TFBlockTags.COMMON_PROTECTIONS)
			.add(key(TFBlocks.FAKE_DIAMOND), key(TFBlocks.FAKE_GOLD));
	}

	private void addProtectedMaterialTags() {
		this.builder(TFBlockTags.CASTLE_BLOCKS).add(
			key(TFBlocks.CASTLE_BRICK),
			key(TFBlocks.WORN_CASTLE_BRICK),
			key(TFBlocks.CRACKED_CASTLE_BRICK),
			key(TFBlocks.MOSSY_CASTLE_BRICK),
			key(TFBlocks.CASTLE_ROOF_TILE),
			key(TFBlocks.THICK_CASTLE_BRICK),
			key(TFBlocks.BOLD_CASTLE_BRICK_TILE),
			key(TFBlocks.BOLD_CASTLE_BRICK_PILLAR),
			key(TFBlocks.ENCASED_CASTLE_BRICK_TILE),
			key(TFBlocks.ENCASED_CASTLE_BRICK_PILLAR),
			key(TFBlocks.CASTLE_BRICK_STAIRS),
			key(TFBlocks.WORN_CASTLE_BRICK_STAIRS),
			key(TFBlocks.CRACKED_CASTLE_BRICK_STAIRS),
			key(TFBlocks.MOSSY_CASTLE_BRICK_STAIRS),
			key(TFBlocks.ENCASED_CASTLE_BRICK_STAIRS),
			key(TFBlocks.BOLD_CASTLE_BRICK_STAIRS),
			key(TFBlocks.PINK_CASTLE_RUNE_BRICK),
			key(TFBlocks.YELLOW_CASTLE_RUNE_BRICK),
			key(TFBlocks.BLUE_CASTLE_RUNE_BRICK),
			key(TFBlocks.VIOLET_CASTLE_RUNE_BRICK),
			key(TFBlocks.PINK_CASTLE_DOOR),
			key(TFBlocks.YELLOW_CASTLE_DOOR),
			key(TFBlocks.BLUE_CASTLE_DOOR),
			key(TFBlocks.VIOLET_CASTLE_DOOR)
		);
		this.builder(TFBlockTags.MAZESTONE).add(
			key(TFBlocks.MAZESTONE),
			key(TFBlocks.MAZESTONE_BRICK),
			key(TFBlocks.CRACKED_MAZESTONE),
			key(TFBlocks.MOSSY_MAZESTONE),
			key(TFBlocks.CUT_MAZESTONE),
			key(TFBlocks.DECORATIVE_MAZESTONE),
			key(TFBlocks.MAZESTONE_MOSAIC),
			key(TFBlocks.MAZESTONE_BORDER)
		);
		this.builder(TFBlockTags.DEADROCK).add(
			key(TFBlocks.DEADROCK),
			key(TFBlocks.CRACKED_DEADROCK),
			key(TFBlocks.WEATHERED_DEADROCK)
		);
		this.builder(BlockTags.NEEDS_DIAMOND_TOOL)
			.add(key(TFBlocks.AURORA_BLOCK))
			.addTag(TFBlockTags.CASTLE_BLOCKS)
			.addTag(TFBlockTags.MAZESTONE)
			.addTag(TFBlockTags.DEADROCK);
	}

	private void addPortalTags() {
		this.builder(TFBlockTags.PORTAL_POOL).add(key(Blocks.WATER));
		this.builder(TFBlockTags.PORTAL_EDGE)
			.add(key(Blocks.FARMLAND), key(Blocks.DIRT_PATH), key(Blocks.GRASS_BLOCK))
			.addTag(BlockTags.DIRT);
		this.builder(TFBlockTags.GENERATED_PORTAL_DECO).add(
			key(Blocks.BROWN_MUSHROOM),
			key(Blocks.RED_MUSHROOM),
			key(Blocks.SHORT_GRASS),
			key(Blocks.FERN),
			key(Blocks.BLUE_ORCHID),
			key(Blocks.AZURE_BLUET),
			key(Blocks.LILY_OF_THE_VALLEY),
			key(Blocks.OXEYE_DAISY),
			key(Blocks.ALLIUM),
			key(Blocks.CORNFLOWER),
			key(Blocks.WHITE_TULIP),
			key(Blocks.PINK_TULIP),
			key(Blocks.ORANGE_TULIP),
			key(Blocks.RED_TULIP),
			key(TFBlocks.MUSHGLOOM),
			key(TFBlocks.MAYAPPLE),
			key(TFBlocks.FIDDLEHEAD)
		);
		this.builder(TFBlockTags.PORTAL_DECO)
			.add(
				key(Blocks.BAMBOO),
				key(Blocks.SHORT_GRASS),
				key(Blocks.TALL_GRASS),
				key(Blocks.FERN),
				key(Blocks.LARGE_FERN),
				key(Blocks.DEAD_BUSH),
				key(Blocks.SUGAR_CANE),
				key(Blocks.CHORUS_PLANT),
				key(Blocks.CHORUS_FLOWER),
				key(Blocks.SWEET_BERRY_BUSH),
				key(Blocks.NETHER_WART),
				key(Blocks.COCOA),
				key(Blocks.VINE),
				key(Blocks.GLOW_LICHEN),
				key(Blocks.RED_MUSHROOM),
				key(Blocks.BROWN_MUSHROOM),
				key(Blocks.WARPED_FUNGUS),
				key(Blocks.CRIMSON_FUNGUS),
				key(Blocks.ATTACHED_MELON_STEM),
				key(Blocks.ATTACHED_PUMPKIN_STEM),
				key(Blocks.MOSS_CARPET),
				key(Blocks.PINK_PETALS),
				key(Blocks.BIG_DRIPLEAF),
				key(Blocks.BIG_DRIPLEAF_STEM),
				key(Blocks.SMALL_DRIPLEAF),
				key(TFBlocks.FIDDLEHEAD),
				key(TFBlocks.MOSS_PATCH),
				key(TFBlocks.MAYAPPLE),
				key(TFBlocks.CLOVER_PATCH),
				key(TFBlocks.MUSHGLOOM),
				key(TFBlocks.FALLEN_LEAVES),
				key(TFBlocks.GIANT_LEAVES),
				key(TFBlocks.STEELEAF_BLOCK),
				key(TFBlocks.HARDENED_DARK_LEAVES)
			)
			.addOptionalTag(BlockTags.FLOWERS)
			.addTag(BlockTags.LEAVES)
			.addTag(SAPLINGS)
			.addOptionalTag(BlockTags.CROPS);
	}

	private void addGameplayBehaviorTags() {
		this.builder(TFBlockTags.FIRE_JET_FUEL).add(key(Blocks.LAVA));
		this.builder(TFBlockTags.PENGUINS_SPAWNABLE_ON).addOptionalTag(BlockTags.ICE);
		this.builder(TFBlockTags.GIANTS_SPAWNABLE_ON).addTag(TFBlockTags.CLOUDS);
		this.builder(TFBlockTags.SUPPORTS_STALAGMITES)
			.add(key(Blocks.PACKED_ICE))
			.addTag(TFBlockTags.DEADROCK);
		this.builder(TFBlockTags.CANNOT_TROLL_CAVE_HOLLOW).add(
			key(Blocks.RED_MUSHROOM_BLOCK),
			key(Blocks.BROWN_MUSHROOM_BLOCK),
			key(TFBlocks.HUGE_MUSHGLOOM)
		);
		this.builder(TFBlockTags.TIME_CORE_EXCLUDED).add(key(Blocks.NETHER_PORTAL));
		this.builder(TFBlockTags.CARMINITE_REACTOR_ORES)
			.add(key(Blocks.NETHER_QUARTZ_ORE), key(Blocks.NETHER_GOLD_ORE));
	}

	private void addWorldgenBehaviorTags() {
		this.builder(TFBlockTags.WORLDGEN_REPLACEABLES)
			.addOptionalTag(BlockTags.LUSH_GROUND_REPLACEABLE)
			.addOptionalTag(BlockTags.REPLACEABLE_BY_TREES);
		this.builder(TFBlockTags.CARVER_REPLACEABLES)
			.addTag(BlockTags.OVERWORLD_CARVER_REPLACEABLES)
			.add(key(Blocks.SNOW_BLOCK));
		this.builder(TFBlockTags.ROOT_TRACE_SKIP)
			.add(
				key(TFBlocks.ROOT_BLOCK),
				key(TFBlocks.LIVEROOT_BLOCK),
				key(TFBlocks.MANGROVE_ROOT),
				key(TFBlocks.TIME_WOOD)
			)
			.addTag(BlockTags.LOGS)
			.addTag(BlockTags.FEATURES_CANNOT_REPLACE);
		this.builder(TFBlockTags.SMALL_LAKES_DONT_REPLACE)
			.add(key(TFBlocks.ROOT_BLOCK), key(TFBlocks.LIVEROOT_BLOCK), key(Blocks.MUSHROOM_STEM))
			.addTag(BlockTags.FEATURES_CANNOT_REPLACE)
			.addTag(BlockTags.LOGS)
			.addTag(BlockTags.LEAVES);
	}

	private void addTreeTags() {
		this.builder(BlockTags.LEAVES).add(
			key(TFBlocks.RAINBOW_OAK_LEAVES),
			key(TFBlocks.TWILIGHT_OAK_LEAVES),
			key(TFBlocks.CANOPY_LEAVES),
			key(TFBlocks.MANGROVE_LEAVES),
			key(TFBlocks.DARK_LEAVES),
			key(TFBlocks.HARDENED_DARK_LEAVES),
			key(TFBlocks.TIME_LEAVES),
			key(TFBlocks.TRANSFORMATION_LEAVES),
			key(TFBlocks.MINING_LEAVES),
			key(TFBlocks.SORTING_LEAVES),
			key(TFBlocks.THORN_LEAVES),
			key(TFBlocks.BEANSTALK_LEAVES)
		);
		this.builder(SAPLINGS).add(
			key(TFBlocks.TWILIGHT_OAK_SAPLING),
			key(TFBlocks.CANOPY_SAPLING),
			key(TFBlocks.MANGROVE_SAPLING),
			key(TFBlocks.DARKWOOD_SAPLING),
			key(TFBlocks.TIME_SAPLING),
			key(TFBlocks.TRANSFORMATION_SAPLING),
			key(TFBlocks.MINING_SAPLING),
			key(TFBlocks.SORTING_SAPLING),
			key(TFBlocks.HOLLOW_OAK_SAPLING),
			key(TFBlocks.RAINBOW_OAK_SAPLING)
		);
		this.builder(BlockTags.REPLACEABLE_BY_TREES).add(
			key(TFBlocks.HARDENED_DARK_LEAVES),
			key(TFBlocks.MAYAPPLE),
			key(TFBlocks.FIDDLEHEAD),
			key(TFBlocks.MOSS_PATCH),
			key(TFBlocks.CLOVER_PATCH),
			key(TFBlocks.MUSHGLOOM),
			key(TFBlocks.FIREFLY),
			key(TFBlocks.FALLEN_LEAVES),
			key(TFBlocks.TORCHBERRY_PLANT),
			key(TFBlocks.ROOT_STRAND),
			key(TFBlocks.ROOT_BLOCK)
		);
	}

	private void addLogTags() {
		this.addLogTag(TFBlockTags.TWILIGHT_OAK_LOGS, TFBlocks.TWILIGHT_OAK_LOG, TFBlocks.STRIPPED_TWILIGHT_OAK_LOG, TFBlocks.TWILIGHT_OAK_WOOD, TFBlocks.STRIPPED_TWILIGHT_OAK_WOOD);
		this.addLogTag(TFBlockTags.CANOPY_LOGS, TFBlocks.CANOPY_LOG, TFBlocks.STRIPPED_CANOPY_LOG, TFBlocks.CANOPY_WOOD, TFBlocks.STRIPPED_CANOPY_WOOD);
		this.addLogTag(TFBlockTags.MANGROVE_LOGS, TFBlocks.MANGROVE_LOG, TFBlocks.STRIPPED_MANGROVE_LOG, TFBlocks.MANGROVE_WOOD, TFBlocks.STRIPPED_MANGROVE_WOOD);
		this.addLogTag(TFBlockTags.DARKWOOD_LOGS, TFBlocks.DARK_LOG, TFBlocks.STRIPPED_DARK_LOG, TFBlocks.DARK_WOOD, TFBlocks.STRIPPED_DARK_WOOD);
		this.addLogTag(TFBlockTags.TIME_LOGS, TFBlocks.TIME_LOG, TFBlocks.STRIPPED_TIME_LOG, TFBlocks.TIME_WOOD, TFBlocks.STRIPPED_TIME_WOOD);
		this.addLogTag(TFBlockTags.TRANSFORMATION_LOGS, TFBlocks.TRANSFORMATION_LOG, TFBlocks.STRIPPED_TRANSFORMATION_LOG, TFBlocks.TRANSFORMATION_WOOD, TFBlocks.STRIPPED_TRANSFORMATION_WOOD);
		this.addLogTag(TFBlockTags.MINING_LOGS, TFBlocks.MINING_LOG, TFBlocks.STRIPPED_MINING_LOG, TFBlocks.MINING_WOOD, TFBlocks.STRIPPED_MINING_WOOD);
		this.addLogTag(TFBlockTags.SORTING_LOGS, TFBlocks.SORTING_LOG, TFBlocks.STRIPPED_SORTING_LOG, TFBlocks.SORTING_WOOD, TFBlocks.STRIPPED_SORTING_WOOD);

		this.builder(TFBlockTags.TF_LOGS)
			.addTag(TFBlockTags.TWILIGHT_OAK_LOGS)
			.addTag(TFBlockTags.CANOPY_LOGS)
			.addTag(TFBlockTags.MANGROVE_LOGS)
			.addTag(TFBlockTags.DARKWOOD_LOGS)
			.addTag(TFBlockTags.TIME_LOGS)
			.addTag(TFBlockTags.TRANSFORMATION_LOGS)
			.addTag(TFBlockTags.MINING_LOGS)
			.addTag(TFBlockTags.SORTING_LOGS);
		this.builder(BlockTags.LOGS).addTag(TFBlockTags.TF_LOGS);
		this.builder(LOGS_THAT_BURN).addTag(TFBlockTags.TF_LOGS);
	}

	private void addLogTag(TagKey<Block> tag, Block log, Block strippedLog, Block wood, Block strippedWood) {
		this.builder(tag).add(key(log), key(strippedLog), key(wood), key(strippedWood));
	}

	private void addHollowLogTags() {
		this.addHollowLog(TFBlocks.HOLLOW_OAK_LOG_HORIZONTAL, TFBlocks.HOLLOW_OAK_LOG_VERTICAL, TFBlocks.HOLLOW_OAK_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_SPRUCE_LOG_HORIZONTAL, TFBlocks.HOLLOW_SPRUCE_LOG_VERTICAL, TFBlocks.HOLLOW_SPRUCE_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_BIRCH_LOG_HORIZONTAL, TFBlocks.HOLLOW_BIRCH_LOG_VERTICAL, TFBlocks.HOLLOW_BIRCH_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_JUNGLE_LOG_HORIZONTAL, TFBlocks.HOLLOW_JUNGLE_LOG_VERTICAL, TFBlocks.HOLLOW_JUNGLE_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_ACACIA_LOG_HORIZONTAL, TFBlocks.HOLLOW_ACACIA_LOG_VERTICAL, TFBlocks.HOLLOW_ACACIA_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_DARK_OAK_LOG_HORIZONTAL, TFBlocks.HOLLOW_DARK_OAK_LOG_VERTICAL, TFBlocks.HOLLOW_DARK_OAK_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_CRIMSON_STEM_HORIZONTAL, TFBlocks.HOLLOW_CRIMSON_STEM_VERTICAL, TFBlocks.HOLLOW_CRIMSON_STEM_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_WARPED_STEM_HORIZONTAL, TFBlocks.HOLLOW_WARPED_STEM_VERTICAL, TFBlocks.HOLLOW_WARPED_STEM_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_VANGROVE_LOG_HORIZONTAL, TFBlocks.HOLLOW_VANGROVE_LOG_VERTICAL, TFBlocks.HOLLOW_VANGROVE_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_CHERRY_LOG_HORIZONTAL, TFBlocks.HOLLOW_CHERRY_LOG_VERTICAL, TFBlocks.HOLLOW_CHERRY_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_PALE_OAK_LOG_HORIZONTAL, TFBlocks.HOLLOW_PALE_OAK_LOG_VERTICAL, TFBlocks.HOLLOW_PALE_OAK_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_HORIZONTAL, TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_VERTICAL, TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_CANOPY_LOG_HORIZONTAL, TFBlocks.HOLLOW_CANOPY_LOG_VERTICAL, TFBlocks.HOLLOW_CANOPY_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_MANGROVE_LOG_HORIZONTAL, TFBlocks.HOLLOW_MANGROVE_LOG_VERTICAL, TFBlocks.HOLLOW_MANGROVE_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_DARK_LOG_HORIZONTAL, TFBlocks.HOLLOW_DARK_LOG_VERTICAL, TFBlocks.HOLLOW_DARK_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_TIME_LOG_HORIZONTAL, TFBlocks.HOLLOW_TIME_LOG_VERTICAL, TFBlocks.HOLLOW_TIME_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_TRANSFORMATION_LOG_HORIZONTAL, TFBlocks.HOLLOW_TRANSFORMATION_LOG_VERTICAL, TFBlocks.HOLLOW_TRANSFORMATION_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_MINING_LOG_HORIZONTAL, TFBlocks.HOLLOW_MINING_LOG_VERTICAL, TFBlocks.HOLLOW_MINING_LOG_CLIMBABLE);
		this.addHollowLog(TFBlocks.HOLLOW_SORTING_LOG_HORIZONTAL, TFBlocks.HOLLOW_SORTING_LOG_VERTICAL, TFBlocks.HOLLOW_SORTING_LOG_CLIMBABLE);

		this.builder(TFBlockTags.HOLLOW_LOGS)
			.addTag(TFBlockTags.HOLLOW_LOGS_HORIZONTAL)
			.addTag(TFBlockTags.HOLLOW_LOGS_VERTICAL)
			.addTag(TFBlockTags.HOLLOW_LOGS_CLIMBABLE);
		this.builder(BlockTags.CLIMBABLE)
			.add(key(TFBlocks.IRON_LADDER), key(TFBlocks.ROPE), key(TFBlocks.ROOT_STRAND))
			.addTag(TFBlockTags.HOLLOW_LOGS_CLIMBABLE);
	}

	private void addHollowLog(Block horizontal, Block vertical, Block climbable) {
		this.builder(TFBlockTags.HOLLOW_LOGS_HORIZONTAL).add(key(horizontal));
		this.builder(TFBlockTags.HOLLOW_LOGS_VERTICAL).add(key(vertical));
		this.builder(TFBlockTags.HOLLOW_LOGS_CLIMBABLE).add(key(climbable));
	}

	private void addWoodFamilyTags() {
		TFBlockFamilies.getAllFamilies().forEach(family -> {
			this.builder(BlockTags.PLANKS).add(key(family.getBaseBlock()));
			this.addFamilyVariant(BlockTags.WOODEN_BUTTONS, family, BlockFamily.Variant.BUTTON);
			this.addFamilyVariant(BlockTags.WOODEN_DOORS, family, BlockFamily.Variant.DOOR);
			this.addFamilyVariant(BlockTags.WOODEN_FENCES, family, BlockFamily.Variant.FENCE);
			this.addFamilyVariant(BlockTags.FENCE_GATES, family, BlockFamily.Variant.FENCE_GATE);
			this.addFamilyVariant(ConventionalBlockTags.WOODEN_FENCE_GATES, family, BlockFamily.Variant.FENCE_GATE);
			this.addFamilyVariant(BlockTags.WOODEN_PRESSURE_PLATES, family, BlockFamily.Variant.PRESSURE_PLATE);
			this.addFamilyVariant(BlockTags.WOODEN_SLABS, family, BlockFamily.Variant.SLAB);
			this.addFamilyVariant(BlockTags.WOODEN_STAIRS, family, BlockFamily.Variant.STAIRS);
			this.addFamilyVariant(BlockTags.WOODEN_TRAPDOORS, family, BlockFamily.Variant.TRAPDOOR);
			this.addFamilyVariant(BlockTags.STANDING_SIGNS, family, BlockFamily.Variant.SIGN);
			this.addFamilyVariant(BlockTags.WALL_SIGNS, family, BlockFamily.Variant.WALL_SIGN);
			this.addFamilyVariant(BlockTags.CEILING_HANGING_SIGNS, family, BlockFamily.Variant.HANGING_SIGN);
			this.addFamilyVariant(BlockTags.WALL_HANGING_SIGNS, family, BlockFamily.Variant.WALL_HANGING_SIGN);
		});
		this.builder(BlockTags.PLANKS).addTag(TFBlockTags.TOWERWOOD);
	}

	private void addBanisterTags() {
		this.builder(TFBlockTags.BANISTERS).add(
			key(TFBlocks.OAK_BANISTER),
			key(TFBlocks.SPRUCE_BANISTER),
			key(TFBlocks.BIRCH_BANISTER),
			key(TFBlocks.JUNGLE_BANISTER),
			key(TFBlocks.ACACIA_BANISTER),
			key(TFBlocks.DARK_OAK_BANISTER),
			key(TFBlocks.CRIMSON_BANISTER),
			key(TFBlocks.WARPED_BANISTER),
			key(TFBlocks.VANGROVE_BANISTER),
			key(TFBlocks.BAMBOO_BANISTER),
			key(TFBlocks.CHERRY_BANISTER),
			key(TFBlocks.PALE_OAK_BANISTER),
			key(TFBlocks.TWILIGHT_OAK_BANISTER),
			key(TFBlocks.CANOPY_BANISTER),
			key(TFBlocks.MANGROVE_BANISTER),
			key(TFBlocks.DARK_BANISTER),
			key(TFBlocks.TIME_BANISTER),
			key(TFBlocks.TRANSFORMATION_BANISTER),
			key(TFBlocks.MINING_BANISTER),
			key(TFBlocks.SORTING_BANISTER)
		);
	}

	private void addFunctionalBlockTags() {
		this.builder(TFBlockTags.TF_CHESTS).add(
			key(TFBlocks.TWILIGHT_OAK_CHEST),
			key(TFBlocks.CANOPY_CHEST),
			key(TFBlocks.MANGROVE_CHEST),
			key(TFBlocks.DARK_CHEST),
			key(TFBlocks.TIME_CHEST),
			key(TFBlocks.TRANSFORMATION_CHEST),
			key(TFBlocks.MINING_CHEST),
			key(TFBlocks.SORTING_CHEST)
		);
		this.builder(TFBlockTags.DRYING_RACKS).add(
			key(TFBlocks.OAK_DRYING_RACK),
			key(TFBlocks.SPRUCE_DRYING_RACK),
			key(TFBlocks.BIRCH_DRYING_RACK),
			key(TFBlocks.JUNGLE_DRYING_RACK),
			key(TFBlocks.ACACIA_DRYING_RACK),
			key(TFBlocks.DARK_OAK_DRYING_RACK),
			key(TFBlocks.CRIMSON_DRYING_RACK),
			key(TFBlocks.WARPED_DRYING_RACK),
			key(TFBlocks.VANGROVE_DRYING_RACK),
			key(TFBlocks.BAMBOO_DRYING_RACK),
			key(TFBlocks.CHERRY_DRYING_RACK),
			key(TFBlocks.TWILIGHT_OAK_DRYING_RACK),
			key(TFBlocks.CANOPY_DRYING_RACK),
			key(TFBlocks.MANGROVE_DRYING_RACK),
			key(TFBlocks.DARK_DRYING_RACK),
			key(TFBlocks.TIME_DRYING_RACK),
			key(TFBlocks.TRANSFORMATION_DRYING_RACK),
			key(TFBlocks.MINING_DRYING_RACK),
			key(TFBlocks.SORTING_DRYING_RACK)
		);
		this.builder(TFBlockTags.TROPHIES).add(
			key(TFBlocks.NAGA_TROPHY),
			key(TFBlocks.NAGA_WALL_TROPHY),
			key(TFBlocks.LICH_TROPHY),
			key(TFBlocks.LICH_WALL_TROPHY),
			key(TFBlocks.MINOSHROOM_TROPHY),
			key(TFBlocks.MINOSHROOM_WALL_TROPHY),
			key(TFBlocks.HYDRA_TROPHY),
			key(TFBlocks.HYDRA_WALL_TROPHY),
			key(TFBlocks.KNIGHT_PHANTOM_TROPHY),
			key(TFBlocks.KNIGHT_PHANTOM_WALL_TROPHY),
			key(TFBlocks.UR_GHAST_TROPHY),
			key(TFBlocks.UR_GHAST_WALL_TROPHY),
			key(TFBlocks.ALPHA_YETI_TROPHY),
			key(TFBlocks.ALPHA_YETI_WALL_TROPHY),
			key(TFBlocks.SNOW_QUEEN_TROPHY),
			key(TFBlocks.SNOW_QUEEN_WALL_TROPHY),
			key(TFBlocks.QUEST_RAM_TROPHY),
			key(TFBlocks.QUEST_RAM_WALL_TROPHY)
		);
	}

	private void addFamilyVariant(TagKey<Block> tag, BlockFamily family, BlockFamily.Variant variant) {
		Block block = Objects.requireNonNull(family.get(variant), () -> family.getBaseBlock() + " is missing " + variant);
		this.builder(tag).add(key(block));
	}

	private void addShapeTags() {
		this.builder(BlockTags.SLABS).add(key(TFBlocks.AURORA_SLAB));
		this.builder(BlockTags.STAIRS).add(
			key(TFBlocks.CASTLE_BRICK_STAIRS),
			key(TFBlocks.WORN_CASTLE_BRICK_STAIRS),
			key(TFBlocks.CRACKED_CASTLE_BRICK_STAIRS),
			key(TFBlocks.MOSSY_CASTLE_BRICK_STAIRS),
			key(TFBlocks.ENCASED_CASTLE_BRICK_STAIRS),
			key(TFBlocks.BOLD_CASTLE_BRICK_STAIRS),
			key(TFBlocks.NAGASTONE_STAIRS_LEFT),
			key(TFBlocks.NAGASTONE_STAIRS_RIGHT),
			key(TFBlocks.MOSSY_NAGASTONE_STAIRS_LEFT),
			key(TFBlocks.MOSSY_NAGASTONE_STAIRS_RIGHT),
			key(TFBlocks.CRACKED_NAGASTONE_STAIRS_LEFT),
			key(TFBlocks.CRACKED_NAGASTONE_STAIRS_RIGHT)
		);
		this.builder(BlockTags.FLOWER_POTS).add(
			key(TFBlocks.POTTED_TWILIGHT_OAK_SAPLING),
			key(TFBlocks.POTTED_CANOPY_SAPLING),
			key(TFBlocks.POTTED_MANGROVE_SAPLING),
			key(TFBlocks.POTTED_DARKWOOD_SAPLING),
			key(TFBlocks.POTTED_RAINBOW_OAK_SAPLING),
			key(TFBlocks.POTTED_HOLLOW_OAK_SAPLING),
			key(TFBlocks.POTTED_TIME_SAPLING),
			key(TFBlocks.POTTED_TRANSFORMATION_SAPLING),
			key(TFBlocks.POTTED_MINING_SAPLING),
			key(TFBlocks.POTTED_SORTING_SAPLING),
			key(TFBlocks.POTTED_MAYAPPLE),
			key(TFBlocks.POTTED_FIDDLEHEAD),
			key(TFBlocks.POTTED_MUSHGLOOM),
			key(TFBlocks.POTTED_THORN),
			key(TFBlocks.POTTED_GREEN_THORN),
			key(TFBlocks.POTTED_DEAD_THORN)
		);
	}

	private void addStorageBlockTags() {
		this.builder(TFBlockTags.STORAGE_BLOCKS_ARCTIC_FUR).add(key(TFBlocks.ARCTIC_FUR_BLOCK));
		this.builder(TFBlockTags.STORAGE_BLOCKS_CARMINITE).add(key(TFBlocks.CARMINITE_BLOCK));
		this.builder(TFBlockTags.STORAGE_BLOCKS_FIERY).add(key(TFBlocks.FIERY_BLOCK));
		this.builder(TFBlockTags.STORAGE_BLOCKS_IRONWOOD).add(key(TFBlocks.IRONWOOD_BLOCK));
		this.builder(TFBlockTags.STORAGE_BLOCKS_KNIGHTMETAL).add(key(TFBlocks.KNIGHTMETAL_BLOCK));
		this.builder(TFBlockTags.STORAGE_BLOCKS_STEELEAF).add(key(TFBlocks.STEELEAF_BLOCK));

		this.builder(ConventionalBlockTags.STORAGE_BLOCKS)
			.addTag(TFBlockTags.STORAGE_BLOCKS_ARCTIC_FUR)
			.addTag(TFBlockTags.STORAGE_BLOCKS_CARMINITE)
			.addTag(TFBlockTags.STORAGE_BLOCKS_FIERY)
			.addTag(TFBlockTags.STORAGE_BLOCKS_IRONWOOD)
			.addTag(TFBlockTags.STORAGE_BLOCKS_KNIGHTMETAL)
			.addTag(TFBlockTags.STORAGE_BLOCKS_STEELEAF);
		this.builder(BlockTags.BEACON_BASE_BLOCKS)
			.addTag(TFBlockTags.STORAGE_BLOCKS_FIERY)
			.addTag(TFBlockTags.STORAGE_BLOCKS_IRONWOOD)
			.addTag(TFBlockTags.STORAGE_BLOCKS_KNIGHTMETAL)
			.addTag(TFBlockTags.STORAGE_BLOCKS_STEELEAF);
	}

	private void addConventionTags() {
		this.builder(TFBlockTags.ROOT_GROUND).add(key(TFBlocks.ROOT_BLOCK));
		this.builder(TFBlockTags.ROOT_ORES).add(key(TFBlocks.LIVEROOT_BLOCK));
		this.builder(ConventionalBlockTags.BOOKSHELVES).add(key(TFBlocks.CANOPY_BOOKSHELF));
		this.builder(ConventionalBlockTags.TRAPPED_CHESTS).add(
			key(TFBlocks.TWILIGHT_OAK_TRAPPED_CHEST),
			key(TFBlocks.CANOPY_TRAPPED_CHEST),
			key(TFBlocks.MANGROVE_TRAPPED_CHEST),
			key(TFBlocks.DARK_TRAPPED_CHEST),
			key(TFBlocks.TIME_TRAPPED_CHEST),
			key(TFBlocks.TRANSFORMATION_TRAPPED_CHEST),
			key(TFBlocks.MINING_TRAPPED_CHEST),
			key(TFBlocks.SORTING_TRAPPED_CHEST)
		);
		this.builder(ConventionalBlockTags.WOODEN_CHESTS).add(
			key(TFBlocks.TWILIGHT_OAK_CHEST),
			key(TFBlocks.CANOPY_CHEST),
			key(TFBlocks.MANGROVE_CHEST),
			key(TFBlocks.DARK_CHEST),
			key(TFBlocks.TIME_CHEST),
			key(TFBlocks.TRANSFORMATION_CHEST),
			key(TFBlocks.MINING_CHEST),
			key(TFBlocks.SORTING_CHEST)
		);
		this.builder(ConventionalBlockTags.GLASS_BLOCKS).add(key(TFBlocks.AURORALIZED_GLASS));
		this.builder(ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES).add(key(TFBlocks.UNCRAFTING_TABLE));
		this.builder(ConventionalBlockTags.ROPES).add(key(TFBlocks.ROPE));
		this.builder(ConventionalBlockTags.RELOCATION_NOT_SUPPORTED).add(
			key(TFBlocks.TWILIGHT_PORTAL),
			key(TFBlocks.STRONGHOLD_SHIELD),
			key(TFBlocks.TIME_LOG_CORE),
			key(TFBlocks.TRANSFORMATION_LOG_CORE),
			key(TFBlocks.MINING_LOG_CORE),
			key(TFBlocks.SORTING_LOG_CORE),
			key(TFBlocks.ANTIBUILDER),
			key(TFBlocks.BUILT_BLOCK),
			key(TFBlocks.FAKE_DIAMOND),
			key(TFBlocks.FAKE_GOLD),
			key(TFBlocks.REACTOR_DEBRIS),
			key(TFBlocks.LOCKED_VANISHING_BLOCK),
			key(TFBlocks.VANISHING_BLOCK),
			key(TFBlocks.UNBREAKABLE_VANISHING_BLOCK),
			key(TFBlocks.REAPPEARING_BLOCK),
			key(TFBlocks.BEANSTALK_GROWER),
			key(TFBlocks.GIANT_COBBLESTONE),
			key(TFBlocks.GIANT_LOG),
			key(TFBlocks.GIANT_LEAVES),
			key(TFBlocks.GIANT_OBSIDIAN),
			key(TFBlocks.BROWN_THORNS),
			key(TFBlocks.GREEN_THORNS),
			key(TFBlocks.BURNT_THORNS),
			key(TFBlocks.PINK_FORCE_FIELD),
			key(TFBlocks.ORANGE_FORCE_FIELD),
			key(TFBlocks.GREEN_FORCE_FIELD),
			key(TFBlocks.BLUE_FORCE_FIELD),
			key(TFBlocks.VIOLET_FORCE_FIELD),
			key(TFBlocks.FINAL_BOSS_BOSS_SPAWNER),
			key(TFBlocks.NAGA_BOSS_SPAWNER),
			key(TFBlocks.LICH_BOSS_SPAWNER),
			key(TFBlocks.MINOSHROOM_BOSS_SPAWNER),
			key(TFBlocks.HYDRA_BOSS_SPAWNER),
			key(TFBlocks.KNIGHT_PHANTOM_BOSS_SPAWNER),
			key(TFBlocks.UR_GHAST_BOSS_SPAWNER),
			key(TFBlocks.ALPHA_YETI_BOSS_SPAWNER),
			key(TFBlocks.SNOW_QUEEN_BOSS_SPAWNER)
		);
	}

	private void addMiningTags() {
		this.builder(BlockTags.MINEABLE_WITH_AXE)
			.add(
				key(TFBlocks.HEDGE),
				key(TFBlocks.ROOT_BLOCK),
				key(TFBlocks.LIVEROOT_BLOCK),
				key(TFBlocks.MANGROVE_ROOT),
				key(TFBlocks.UNCRAFTING_TABLE),
				key(TFBlocks.ENCASED_SMOKER),
				key(TFBlocks.ENCASED_FIRE_JET),
				key(TFBlocks.TIME_LOG_CORE),
				key(TFBlocks.TRANSFORMATION_LOG_CORE),
				key(TFBlocks.MINING_LOG_CORE),
				key(TFBlocks.SORTING_LOG_CORE),
				key(TFBlocks.REAPPEARING_BLOCK),
				key(TFBlocks.VANISHING_BLOCK),
				key(TFBlocks.ANTIBUILDER),
				key(TFBlocks.CARMINITE_REACTOR),
				key(TFBlocks.CARMINITE_BUILDER),
				key(TFBlocks.GHAST_TRAP),
				key(TFBlocks.HUGE_STALK),
				key(TFBlocks.HUGE_MUSHGLOOM),
				key(TFBlocks.HUGE_MUSHGLOOM_STEM),
				key(TFBlocks.CINDER_LOG),
				key(TFBlocks.CINDER_WOOD),
				key(TFBlocks.IRONWOOD_BLOCK),
				key(TFBlocks.CHISELED_CANOPY_BOOKSHELF),
				key(TFBlocks.CANOPY_BOOKSHELF),
				key(TFBlocks.TWILIGHT_OAK_CHEST),
				key(TFBlocks.CANOPY_CHEST),
				key(TFBlocks.MANGROVE_CHEST),
				key(TFBlocks.DARK_CHEST),
				key(TFBlocks.TIME_CHEST),
				key(TFBlocks.TRANSFORMATION_CHEST),
				key(TFBlocks.MINING_CHEST),
				key(TFBlocks.SORTING_CHEST),
				key(TFBlocks.HUGE_LILY_PAD)
			)
			.addTag(TFBlockTags.BANISTERS)
			.addTag(TFBlockTags.HOLLOW_LOGS)
			.addTag(TFBlockTags.TOWERWOOD);
		this.builder(BlockTags.MINEABLE_WITH_PICKAXE)
			.add(
				key(TFBlocks.NAGASTONE),
				key(TFBlocks.NAGASTONE_HEAD),
				key(TFBlocks.STRONGHOLD_SHIELD),
				key(TFBlocks.TROPHY_PEDESTAL),
				key(TFBlocks.AURORA_PILLAR),
				key(TFBlocks.AURORA_SLAB),
				key(TFBlocks.UNDERBRICK),
				key(TFBlocks.MOSSY_UNDERBRICK),
				key(TFBlocks.CRACKED_UNDERBRICK),
				key(TFBlocks.UNDERBRICK_FLOOR),
				key(TFBlocks.TROLLSTEINN),
				key(TFBlocks.GIANT_LEAVES),
				key(TFBlocks.GIANT_OBSIDIAN),
				key(TFBlocks.GIANT_COBBLESTONE),
				key(TFBlocks.GIANT_LOG),
				key(TFBlocks.CINDER_FURNACE),
				key(TFBlocks.TWILIGHT_PORTAL_MINIATURE_STRUCTURE),
				key(TFBlocks.NAGA_COURTYARD_MINIATURE_STRUCTURE),
				key(TFBlocks.LICH_TOWER_MINIATURE_STRUCTURE),
				key(TFBlocks.KNIGHTMETAL_BLOCK),
				key(TFBlocks.IRONWOOD_BLOCK),
				key(TFBlocks.FIERY_BLOCK),
				key(TFBlocks.CARMINITE_BLOCK),
				key(TFBlocks.SPIRAL_BRICKS),
				key(TFBlocks.ETCHED_NAGASTONE),
				key(TFBlocks.NAGASTONE_PILLAR),
				key(TFBlocks.NAGASTONE_STAIRS_LEFT),
				key(TFBlocks.NAGASTONE_STAIRS_RIGHT),
				key(TFBlocks.MOSSY_ETCHED_NAGASTONE),
				key(TFBlocks.MOSSY_NAGASTONE_PILLAR),
				key(TFBlocks.MOSSY_NAGASTONE_STAIRS_LEFT),
				key(TFBlocks.MOSSY_NAGASTONE_STAIRS_RIGHT),
				key(TFBlocks.CRACKED_ETCHED_NAGASTONE),
				key(TFBlocks.CRACKED_NAGASTONE_PILLAR),
				key(TFBlocks.CRACKED_NAGASTONE_STAIRS_LEFT),
				key(TFBlocks.CRACKED_NAGASTONE_STAIRS_RIGHT),
				key(TFBlocks.IRON_LADDER),
				key(TFBlocks.TWISTED_STONE),
				key(TFBlocks.TWISTED_STONE_PILLAR),
				key(TFBlocks.SKULL_CHEST),
				key(TFBlocks.KEEPSAKE_CASKET),
				key(TFBlocks.BOLD_STONE_PILLAR),
				key(TFBlocks.TERRORCOTTA_CURVES),
				key(TFBlocks.TERRORCOTTA_LINES),
				key(TFBlocks.TERRORCOTTA_ARCS),
				key(TFBlocks.SINISTER_SPAWNER)
			)
			.addTag(TFBlockTags.MAZESTONE)
			.addTag(TFBlockTags.CASTLE_BLOCKS)
			.addTag(TFBlockTags.DEADROCK);
		this.builder(BlockTags.MINEABLE_WITH_HOE).add(
			key(TFBlocks.TWILIGHT_OAK_LEAVES),
			key(TFBlocks.CANOPY_LEAVES),
			key(TFBlocks.MANGROVE_LEAVES),
			key(TFBlocks.DARK_LEAVES),
			key(TFBlocks.RAINBOW_OAK_LEAVES),
			key(TFBlocks.TIME_LEAVES),
			key(TFBlocks.TRANSFORMATION_LEAVES),
			key(TFBlocks.MINING_LEAVES),
			key(TFBlocks.SORTING_LEAVES),
			key(TFBlocks.THORN_LEAVES),
			key(TFBlocks.THORN_ROSE),
			key(TFBlocks.BEANSTALK_LEAVES),
			key(TFBlocks.STEELEAF_BLOCK),
			key(TFBlocks.ARCTIC_FUR_BLOCK)
		);
		this.builder(BlockTags.MINEABLE_WITH_SHOVEL).add(
			key(TFBlocks.SMOKER),
			key(TFBlocks.FIRE_JET),
			key(TFBlocks.UBEROUS_SOIL)
		);
		this.builder(TFBlockTags.MINEABLE_WITH_BLOCK_AND_CHAIN)
			.addTag(BlockTags.MINEABLE_WITH_PICKAXE)
			.addTag(BlockTags.MINEABLE_WITH_AXE)
			.addTag(BlockTags.MINEABLE_WITH_SHOVEL)
			.addTag(BlockTags.MINEABLE_WITH_HOE);
		this.builder(TFBlockTags.BLOCK_AND_CHAIN_NEVER_BREAKS)
			.addTag(TFBlockTags.MAZESTONE)
			.addTag(TFBlockTags.CASTLE_BLOCKS)
			.addTag(TFBlockTags.DEADROCK)
			.addTag(BlockTags.WITHER_IMMUNE)
			.add(
				key(TFBlocks.TIME_LOG_CORE),
				key(TFBlocks.TRANSFORMATION_LOG_CORE),
				key(TFBlocks.MINING_LOG_CORE),
				key(TFBlocks.SORTING_LOG_CORE),
				key(TFBlocks.GIANT_OBSIDIAN)
			);
		this.builder(TFBlockTags.MAZEBREAKER_ACCELERATED)
			.addTag(TFBlockTags.MAZESTONE)
			.addTag(TFBlockTags.CASTLE_BLOCKS);
		this.builder(TFBlockTags.INCORRECT_FOR_GIANT_TOOL).addOptionalTag(BlockTags.INCORRECT_FOR_STONE_TOOL);
		this.builder(TFBlockTags.INCORRECT_FOR_GLASS_TOOL).addOptionalTag(BlockTags.INCORRECT_FOR_WOODEN_TOOL);
		this.builder(TFBlockTags.INCORRECT_FOR_ICE_TOOL).addOptionalTag(BlockTags.INCORRECT_FOR_WOODEN_TOOL);
		this.builder(TFBlockTags.INCORRECT_FOR_IRONWOOD_TOOL).addOptionalTag(BlockTags.INCORRECT_FOR_IRON_TOOL);
		this.builder(TFBlockTags.INCORRECT_FOR_FIERY_TOOL).addOptionalTag(BlockTags.INCORRECT_FOR_NETHERITE_TOOL);
		this.builder(TFBlockTags.INCORRECT_FOR_STEELEAF_TOOL).addOptionalTag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
		this.builder(TFBlockTags.INCORRECT_FOR_KNIGHTMETAL_TOOL).addOptionalTag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
	}

	private static ResourceKey<Block> key(Block block) {
		return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
	}

	private static ResourceKey<Block> externalKey(String namespace, String path) {
		return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(namespace, path));
	}
}
