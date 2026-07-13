package twilightforest.datagen.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;
import twilightforest.tags.TFBlockTags;
import twilightforest.tags.TFItemTags;

import java.util.concurrent.CompletableFuture;

public final class ItemTagGenerator extends FabricTagsProvider.ItemTagsProvider {
	private static final TagKey<Item> DYEABLE = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("dyeable"));
	private static final TagKey<Item> SLABS = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("slabs"));
	private static final TagKey<Item> SMALL_FLOWERS = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("small_flowers"));
	private static final TagKey<Item> STAIRS = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("stairs"));
	private static final TagKey<Block> SAPLINGS_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("saplings"));

	public ItemTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries, BlockTagGenerator blockTags) {
		super(output, registries, blockTags);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.copy(TFBlockTags.TWILIGHT_OAK_LOGS, TFItemTags.TWILIGHT_OAK_LOGS);
		this.copy(TFBlockTags.CANOPY_LOGS, TFItemTags.CANOPY_LOGS);
		this.copy(TFBlockTags.MANGROVE_LOGS, TFItemTags.MANGROVE_LOGS);
		this.copy(TFBlockTags.DARKWOOD_LOGS, TFItemTags.DARKWOOD_LOGS);
		this.copy(TFBlockTags.TIME_LOGS, TFItemTags.TIME_LOGS);
		this.copy(TFBlockTags.TRANSFORMATION_LOGS, TFItemTags.TRANSFORMATION_LOGS);
		this.copy(TFBlockTags.MINING_LOGS, TFItemTags.MINING_LOGS);
		this.copy(TFBlockTags.SORTING_LOGS, TFItemTags.SORTING_LOGS);
		this.copy(TFBlockTags.TF_LOGS, TFItemTags.TWILIGHT_LOGS);
		this.copy(TFBlockTags.TOWERWOOD, TFItemTags.TOWERWOOD);
		this.copy(TFBlockTags.BANISTERS, TFItemTags.BANISTERS);
		this.copy(TFBlockTags.DRYING_RACKS, TFItemTags.DRYING_RACKS);

		this.copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
		this.copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
		this.copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
		this.copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
		this.copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
		this.copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
		this.copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
		this.copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
		this.copy(BlockTags.PLANKS, ItemTags.PLANKS);
		this.copy(BlockTags.LOGS, ItemTags.LOGS);
		this.builder(ItemTags.LEAVES).add(
			key(TFBlocks.RAINBOW_OAK_LEAVES.asItem()),
			key(TFBlocks.TWILIGHT_OAK_LEAVES.asItem()),
			key(TFBlocks.CANOPY_LEAVES.asItem()),
			key(TFBlocks.MANGROVE_LEAVES.asItem()),
			key(TFBlocks.DARK_LEAVES.asItem()),
			key(TFBlocks.TIME_LEAVES.asItem()),
			key(TFBlocks.TRANSFORMATION_LEAVES.asItem()),
			key(TFBlocks.MINING_LEAVES.asItem()),
			key(TFBlocks.SORTING_LEAVES.asItem()),
			key(TFBlocks.THORN_LEAVES.asItem()),
			key(TFBlocks.BEANSTALK_LEAVES.asItem())
		);
		this.copy(BlockTags.STANDING_SIGNS, ItemTags.SIGNS);
		this.copy(BlockTags.CEILING_HANGING_SIGNS, ItemTags.HANGING_SIGNS);
		this.copy(SAPLINGS_BLOCKS, ItemTags.SAPLINGS);
		this.copy(BlockTags.SLABS, SLABS);
		this.copy(BlockTags.STAIRS, STAIRS);
		this.builder(SMALL_FLOWERS).add(key(TFBlocks.THORN_ROSE.asItem()));

		this.copy(TFBlockTags.STORAGE_BLOCKS_ARCTIC_FUR, TFItemTags.STORAGE_BLOCKS_ARCTIC_FUR);
		this.copy(TFBlockTags.STORAGE_BLOCKS_CARMINITE, TFItemTags.STORAGE_BLOCKS_CARMINITE);
		this.copy(TFBlockTags.STORAGE_BLOCKS_FIERY, TFItemTags.STORAGE_BLOCKS_FIERY);
		this.copy(TFBlockTags.STORAGE_BLOCKS_IRONWOOD, TFItemTags.STORAGE_BLOCKS_IRONWOOD);
		this.copy(TFBlockTags.STORAGE_BLOCKS_KNIGHTMETAL, TFItemTags.STORAGE_BLOCKS_KNIGHTMETAL);
		this.copy(TFBlockTags.STORAGE_BLOCKS_STEELEAF, TFItemTags.STORAGE_BLOCKS_STEELEAF);
		this.copy(ConventionalBlockTags.STORAGE_BLOCKS, ConventionalItemTags.STORAGE_BLOCKS);
		this.copy(ConventionalBlockTags.WOODEN_CHESTS, ConventionalItemTags.WOODEN_CHESTS);
		this.copy(ConventionalBlockTags.WOODEN_FENCE_GATES, ConventionalItemTags.WOODEN_FENCE_GATES);
		this.copy(ConventionalBlockTags.ROPES, ConventionalItemTags.ROPES);

		this.builder(TFItemTags.FIERY_INGOTS).add(key(TFItems.FIERY_INGOT));
		this.builder(TFItemTags.IRONWOOD_INGOTS).add(key(TFItems.IRONWOOD_INGOT));
		this.builder(TFItemTags.KNIGHTMETAL_INGOTS).add(key(TFItems.KNIGHTMETAL_INGOT));
		this.builder(TFItemTags.STEELEAF_INGOTS).add(key(TFItems.STEELEAF_INGOT));
		this.builder(TFItemTags.WROUGHT_IRON_INGOTS).add(key(TFItems.WROUGHT_IRON_BAR));
		this.builder(ConventionalItemTags.INGOTS)
			.addTag(TFItemTags.IRONWOOD_INGOTS)
			.addTag(TFItemTags.FIERY_INGOTS)
			.addTag(TFItemTags.KNIGHTMETAL_INGOTS)
			.addTag(TFItemTags.STEELEAF_INGOTS);
		this.builder(TFItemTags.CARMINITE_GEMS).add(key(TFItems.CARMINITE));
		this.builder(ConventionalItemTags.GEMS).addTag(TFItemTags.CARMINITE_GEMS);
		this.builder(TFItemTags.RAW_MATERIALS_IRONWOOD).add(key(TFItems.RAW_IRONWOOD));
		this.builder(TFItemTags.RAW_MATERIALS_KNIGHTMETAL).add(key(TFItems.ARMOR_SHARD_CLUSTER));
		this.builder(ConventionalItemTags.RAW_MATERIALS)
			.addTag(TFItemTags.RAW_MATERIALS_IRONWOOD)
			.addTag(TFItemTags.RAW_MATERIALS_KNIGHTMETAL);

		this.builder(TFItemTags.REPAIRS_FIERY_TOOLS).addTag(TFItemTags.FIERY_INGOTS);
		this.builder(TFItemTags.REPAIRS_IRONWOOD_TOOLS).addTag(TFItemTags.IRONWOOD_INGOTS);
		this.builder(TFItemTags.REPAIRS_KNIGHTMETAL_TOOLS).addTag(TFItemTags.KNIGHTMETAL_INGOTS);
		this.builder(TFItemTags.REPAIRS_STEELEAF_TOOLS).addTag(TFItemTags.STEELEAF_INGOTS);
		this.builder(TFItemTags.REPAIRS_GIANT_TOOLS).add(key(TFBlocks.GIANT_COBBLESTONE.asItem()));
		this.builder(TFItemTags.REPAIRS_ICE_TOOLS).add(
			key(Blocks.ICE.asItem()),
			key(Blocks.PACKED_ICE.asItem()),
			key(Blocks.BLUE_ICE.asItem())
		);

		this.builder(TFItemTags.BOAR_TEMPT_ITEMS)
			.addOptionalTag(ConventionalItemTags.CARROT_CROPS)
			.addOptionalTag(ConventionalItemTags.POTATO_CROPS)
			.addOptionalTag(ConventionalItemTags.BEETROOT_CROPS);
		this.builder(TFItemTags.DEER_TEMPT_ITEMS)
			.add(key(Items.APPLE))
			.addOptionalTag(ConventionalItemTags.WHEAT_CROPS);
		this.builder(TFItemTags.DWARF_RABBIT_TEMPT_ITEMS)
			.add(key(Items.GOLDEN_CARROT), key(Items.DANDELION))
			.addOptionalTag(ConventionalItemTags.CARROT_CROPS);
		this.builder(TFItemTags.PENGUIN_TEMPT_ITEMS).addOptionalTag(ItemTags.FISHES);
		this.builder(TFItemTags.RAVEN_TEMPT_ITEMS).addOptionalTag(ConventionalItemTags.SEEDS);
		this.builder(TFItemTags.SQUIRREL_TEMPT_ITEMS).addOptionalTag(ConventionalItemTags.SEEDS);
		this.builder(TFItemTags.TINY_BIRD_TEMPT_ITEMS).addOptionalTag(ConventionalItemTags.SEEDS);
		this.builder(TFItemTags.KOBOLD_PACIFICATION_BREADS).add(key(Items.BREAD));

		this.addConventionTags();
		this.addEquipmentTags();
		this.addEnchantableTags();
		this.addVanillaUtilityTags();
		this.addTwilightForestUtilityTags();
	}

	private void addConventionTags() {
		this.builder(ConventionalItemTags.FEATHERS).add(key(TFItems.RAVEN_FEATHER));
		this.builder(ConventionalItemTags.BERRY_FOODS).add(key(TFItems.TORCHBERRIES));
		this.builder(ConventionalItemTags.COOKED_MEAT_FOODS).add(
			key(TFItems.COOKED_VENISON),
			key(TFItems.COOKED_MEEF),
			key(TFItems.HYDRA_CHOP)
		);
		this.builder(ConventionalItemTags.EDIBLE_WHEN_PLACED_FOODS).add(key(TFItems.EXPERIMENT_115));
		this.builder(ConventionalItemTags.RAW_MEAT_FOODS).add(key(TFItems.RAW_VENISON), key(TFItems.RAW_MEEF));
		this.builder(ConventionalItemTags.SOUP_FOODS).add(key(TFItems.MEEF_STROGANOFF));
		this.builder(ConventionalItemTags.MUSHROOMS).add(key(TFBlocks.MUSHGLOOM.asItem()));
		this.builder(ConventionalItemTags.MUSIC_DISCS).add(
			key(TFItems.MUSIC_DISC_RADIANCE),
			key(TFItems.MUSIC_DISC_STEPS),
			key(TFItems.MUSIC_DISC_SUPERSTITIOUS),
			key(TFItems.MUSIC_DISC_HOME),
			key(TFItems.MUSIC_DISC_WAYFARER),
			key(TFItems.MUSIC_DISC_FINDINGS),
			key(TFItems.MUSIC_DISC_MAKER),
			key(TFItems.MUSIC_DISC_THREAD),
			key(TFItems.MUSIC_DISC_MOTION)
		);
		this.builder(TFItemTags.PAPER).add(key(Items.PAPER));
		this.builder(ConventionalItemTags.BOW_TOOLS).add(
			key(TFItems.TRIPLE_BOW),
			key(TFItems.SEEKER_BOW),
			key(TFItems.ICE_BOW),
			key(TFItems.ENDER_BOW)
		);
		this.builder(ConventionalItemTags.SHIELD_TOOLS).add(key(TFItems.KNIGHTMETAL_SHIELD));
	}

	private void addEquipmentTags() {
		this.builder(ItemTags.AXES).add(
			key(TFItems.IRONWOOD_AXE),
			key(TFItems.STEELEAF_AXE),
			key(TFItems.KNIGHTMETAL_AXE),
			key(TFItems.GOLDEN_MINOTAUR_AXE),
			key(TFItems.DIAMOND_MINOTAUR_AXE)
		);
		this.addPickaxes(ItemTags.PICKAXES);
		this.addPickaxes(ItemTags.CLUSTER_MAX_HARVESTABLES);
		this.builder(ItemTags.SHOVELS).add(key(TFItems.IRONWOOD_SHOVEL), key(TFItems.STEELEAF_SHOVEL));
		this.builder(ItemTags.HOES).add(key(TFItems.IRONWOOD_HOE), key(TFItems.STEELEAF_HOE));
		this.builder(ItemTags.SWORDS).add(
			key(TFItems.IRONWOOD_SWORD),
			key(TFItems.STEELEAF_SWORD),
			key(TFItems.KNIGHTMETAL_SWORD),
			key(TFItems.FIERY_SWORD),
			key(TFItems.GIANT_SWORD),
			key(TFItems.ICE_SWORD),
			key(TFItems.GLASS_SWORD)
		);

		this.builder(ItemTags.HEAD_ARMOR).add(
			key(TFItems.IRONWOOD_HELMET),
			key(TFItems.STEELEAF_HELMET),
			key(TFItems.KNIGHTMETAL_HELMET),
			key(TFItems.ARCTIC_HELMET),
			key(TFItems.YETI_HELMET),
			key(TFItems.FIERY_HELMET),
			key(TFItems.PHANTOM_HELMET)
		);
		this.builder(ItemTags.CHEST_ARMOR).add(
			key(TFItems.IRONWOOD_CHESTPLATE),
			key(TFItems.STEELEAF_CHESTPLATE),
			key(TFItems.KNIGHTMETAL_CHESTPLATE),
			key(TFItems.ARCTIC_CHESTPLATE),
			key(TFItems.YETI_CHESTPLATE),
			key(TFItems.FIERY_CHESTPLATE),
			key(TFItems.PHANTOM_CHESTPLATE),
			key(TFItems.NAGA_CHESTPLATE)
		);
		this.builder(ItemTags.LEG_ARMOR).add(
			key(TFItems.IRONWOOD_LEGGINGS),
			key(TFItems.STEELEAF_LEGGINGS),
			key(TFItems.KNIGHTMETAL_LEGGINGS),
			key(TFItems.ARCTIC_LEGGINGS),
			key(TFItems.YETI_LEGGINGS),
			key(TFItems.FIERY_LEGGINGS),
			key(TFItems.NAGA_LEGGINGS)
		);
		this.builder(ItemTags.FOOT_ARMOR).add(
			key(TFItems.IRONWOOD_BOOTS),
			key(TFItems.STEELEAF_BOOTS),
			key(TFItems.KNIGHTMETAL_BOOTS),
			key(TFItems.ARCTIC_BOOTS),
			key(TFItems.YETI_BOOTS),
			key(TFItems.FIERY_BOOTS)
		);
		this.builder(DYEABLE).add(
			key(TFItems.ARCTIC_HELMET),
			key(TFItems.ARCTIC_CHESTPLATE),
			key(TFItems.ARCTIC_LEGGINGS),
			key(TFItems.ARCTIC_BOOTS)
		);
	}

	private void addPickaxes(TagKey<Item> tag) {
		this.builder(tag).add(
			key(TFItems.IRONWOOD_PICKAXE),
			key(TFItems.STEELEAF_PICKAXE),
			key(TFItems.KNIGHTMETAL_PICKAXE),
			key(TFItems.MAZEBREAKER_PICKAXE),
			key(TFItems.FIERY_PICKAXE),
			key(TFItems.GIANT_PICKAXE)
		);
	}

	private void addEnchantableTags() {
		this.builder(ItemTags.BOW_ENCHANTABLE).add(
			key(TFItems.TRIPLE_BOW),
			key(TFItems.SEEKER_BOW),
			key(TFItems.ICE_BOW),
			key(TFItems.ENDER_BOW)
		);
		this.builder(ItemTags.CHEST_ARMOR_ENCHANTABLE).add(
			key(TFItems.NAGA_CHESTPLATE),
			key(TFItems.IRONWOOD_CHESTPLATE),
			key(TFItems.STEELEAF_CHESTPLATE),
			key(TFItems.KNIGHTMETAL_CHESTPLATE),
			key(TFItems.PHANTOM_CHESTPLATE),
			key(TFItems.FIERY_CHESTPLATE),
			key(TFItems.ARCTIC_CHESTPLATE),
			key(TFItems.YETI_CHESTPLATE)
		);
		this.builder(ItemTags.HEAD_ARMOR_ENCHANTABLE).add(
			key(TFItems.IRONWOOD_HELMET),
			key(TFItems.STEELEAF_HELMET),
			key(TFItems.KNIGHTMETAL_HELMET),
			key(TFItems.PHANTOM_HELMET),
			key(TFItems.FIERY_HELMET),
			key(TFItems.ARCTIC_HELMET),
			key(TFItems.YETI_HELMET)
		);
		this.builder(ItemTags.LEG_ARMOR_ENCHANTABLE).add(
			key(TFItems.NAGA_LEGGINGS),
			key(TFItems.IRONWOOD_LEGGINGS),
			key(TFItems.STEELEAF_LEGGINGS),
			key(TFItems.KNIGHTMETAL_LEGGINGS),
			key(TFItems.FIERY_LEGGINGS),
			key(TFItems.ARCTIC_LEGGINGS),
			key(TFItems.YETI_LEGGINGS)
		);
		this.builder(ItemTags.FOOT_ARMOR_ENCHANTABLE).add(
			key(TFItems.IRONWOOD_BOOTS),
			key(TFItems.STEELEAF_BOOTS),
			key(TFItems.KNIGHTMETAL_BOOTS),
			key(TFItems.FIERY_BOOTS),
			key(TFItems.ARCTIC_BOOTS),
			key(TFItems.YETI_BOOTS)
		);
		this.builder(ItemTags.DURABILITY_ENCHANTABLE).add(
			key(TFItems.TRIPLE_BOW),
			key(TFItems.SEEKER_BOW),
			key(TFItems.ICE_BOW),
			key(TFItems.ENDER_BOW),
			key(TFItems.BLOCK_AND_CHAIN),
			key(TFItems.KNIGHTMETAL_SHIELD),
			key(TFItems.ORE_MAGNET),
			key(TFItems.PEACOCK_FEATHER_FAN),
			key(TFItems.CRUMBLE_HORN)
		);
		this.builder(ItemTags.MINING_ENCHANTABLE).add(key(TFItems.BLOCK_AND_CHAIN));
		this.builder(ItemTags.MINING_LOOT_ENCHANTABLE).add(key(TFItems.BLOCK_AND_CHAIN));
	}

	private void addVanillaUtilityTags() {
		this.builder(ItemTags.BEACON_PAYMENT_ITEMS)
			.addTag(TFItemTags.IRONWOOD_INGOTS)
			.addTag(TFItemTags.STEELEAF_INGOTS)
			.addTag(TFItemTags.KNIGHTMETAL_INGOTS)
			.addTag(TFItemTags.FIERY_INGOTS);
		this.builder(ItemTags.BOATS).add(
			key(TFItems.TWILIGHT_OAK_BOAT),
			key(TFItems.CANOPY_BOAT),
			key(TFItems.MANGROVE_BOAT),
			key(TFItems.DARK_BOAT),
			key(TFItems.TIME_BOAT),
			key(TFItems.TRANSFORMATION_BOAT),
			key(TFItems.MINING_BOAT),
			key(TFItems.SORTING_BOAT)
		);
		this.builder(ItemTags.CHEST_BOATS).add(
			key(TFItems.TWILIGHT_OAK_CHEST_BOAT),
			key(TFItems.CANOPY_CHEST_BOAT),
			key(TFItems.MANGROVE_CHEST_BOAT),
			key(TFItems.DARK_CHEST_BOAT),
			key(TFItems.TIME_CHEST_BOAT),
			key(TFItems.TRANSFORMATION_CHEST_BOAT),
			key(TFItems.MINING_CHEST_BOAT),
			key(TFItems.SORTING_CHEST_BOAT)
		);
		this.builder(ItemTags.BREAKS_DECORATED_POTS).add(key(TFItems.BLOCK_AND_CHAIN));
		this.builder(ItemTags.FREEZE_IMMUNE_WEARABLES).add(
			key(TFItems.FIERY_HELMET),
			key(TFItems.FIERY_CHESTPLATE),
			key(TFItems.FIERY_LEGGINGS),
			key(TFItems.FIERY_BOOTS),
			key(TFItems.ARCTIC_HELMET),
			key(TFItems.ARCTIC_CHESTPLATE),
			key(TFItems.ARCTIC_LEGGINGS),
			key(TFItems.ARCTIC_BOOTS),
			key(TFItems.YETI_HELMET),
			key(TFItems.YETI_CHESTPLATE),
			key(TFItems.YETI_LEGGINGS),
			key(TFItems.YETI_BOOTS)
		);
		this.builder(ItemTags.LOGS_THAT_BURN)
			.addTag(TFItemTags.TWILIGHT_OAK_LOGS)
			.addTag(TFItemTags.CANOPY_LOGS)
			.addTag(TFItemTags.MANGROVE_LOGS)
			.addTag(TFItemTags.TIME_LOGS)
			.addTag(TFItemTags.TRANSFORMATION_LOGS)
			.addTag(TFItemTags.MINING_LOGS)
			.addTag(TFItemTags.SORTING_LOGS);
		this.builder(ItemTags.MEAT).add(
			key(TFItems.RAW_VENISON),
			key(TFItems.COOKED_VENISON),
			key(TFItems.RAW_MEEF),
			key(TFItems.COOKED_MEEF),
			key(TFItems.MEEF_STROGANOFF),
			key(TFItems.EXPERIMENT_115),
			key(TFItems.HYDRA_CHOP)
		);
		this.addSkullCandles(ItemTags.NOTE_BLOCK_TOP_INSTRUMENTS);
		this.builder(ItemTags.PIGLIN_LOVED).add(
			key(TFItems.GOLDEN_MINOTAUR_AXE),
			key(TFItems.CHARM_OF_KEEPING_3),
			key(TFItems.CHARM_OF_LIFE_2),
			key(TFItems.LAMP_OF_CINDERS)
		);
		this.addSkullCandles(ItemTags.SKULLS);
		this.builder(ItemTags.TRIM_MATERIALS).add(
			key(TFItems.IRONWOOD_INGOT),
			key(TFItems.STEELEAF_INGOT),
			key(TFItems.KNIGHTMETAL_INGOT),
			key(TFItems.NAGA_SCALE),
			key(TFItems.CARMINITE),
			key(TFItems.FIERY_INGOT)
		);
	}

	private void addSkullCandles(TagKey<Item> tag) {
		this.builder(tag).add(
			key(TFBlocks.ZOMBIE_SKULL_CANDLE.asItem()),
			key(TFBlocks.SKELETON_SKULL_CANDLE.asItem()),
			key(TFBlocks.WITHER_SKELE_SKULL_CANDLE.asItem()),
			key(TFBlocks.CREEPER_SKULL_CANDLE.asItem()),
			key(TFBlocks.PLAYER_SKULL_CANDLE.asItem()),
			key(TFBlocks.PIGLIN_SKULL_CANDLE.asItem())
		);
	}

	private void addTwilightForestUtilityTags() {
		this.builder(TFItemTags.ARCTIC_FUR).add(key(TFItems.ARCTIC_FUR));
		this.builder(TFItemTags.BANNED_UNCRAFTABLES).add(key(TFBlocks.GIANT_LOG.asItem()));
		this.builder(TFItemTags.BANNED_UNCRAFTING_INGREDIENTS).add(
			key(TFBlocks.INFESTED_TOWERWOOD.asItem()),
			key(TFBlocks.HOLLOW_OAK_SAPLING.asItem()),
			key(TFBlocks.TIME_SAPLING.asItem()),
			key(TFBlocks.TRANSFORMATION_SAPLING.asItem()),
			key(TFBlocks.MINING_SAPLING.asItem()),
			key(TFBlocks.SORTING_SAPLING.asItem()),
			key(TFItems.TRANSFORMATION_POWDER)
		);
		this.builder(TFItemTags.BLOCK_AND_CHAIN_ENCHANTABLE).add(key(TFItems.BLOCK_AND_CHAIN));
		this.builder(TFItemTags.EMPERORS_CLOTH_APPLICABLE)
			.add(key(Items.ELYTRA))
			.addOptionalTag(ConventionalItemTags.ARMORS);
		this.builder(TFItemTags.FIERY_VIAL).add(key(TFItems.FIERY_BLOOD), key(TFItems.FIERY_TEARS));
		this.builder(TFItemTags.IMMUNE_TO_THORNS)
			.add(key(TFBlocks.THORN_LEAVES.asItem()), key(TFBlocks.THORN_ROSE.asItem()));
		this.builder(TFItemTags.KEPT_ON_DEATH)
			.add(key(TFItems.TOWER_KEY), key(TFItems.PHANTOM_HELMET), key(TFItems.PHANTOM_CHESTPLATE));
		this.builder(TFItemTags.PORTAL_ACTIVATOR).addOptionalTag(ConventionalItemTags.DIAMOND_GEMS);
		this.builder(TFItemTags.SCEPTERS).add(
			key(TFItems.TWILIGHT_SCEPTER),
			key(TFItems.LIFEDRAIN_SCEPTER),
			key(TFItems.ZOMBIE_SCEPTER),
			key(TFItems.FORTIFICATION_SCEPTER)
		);
		this.builder(TFItemTags.UNCRAFTING_IGNORES_COST).addOptionalTag(ConventionalItemTags.WOODEN_RODS);
		this.builder(TFItemTags.WIP).add(
			key(TFBlocks.AURORALIZED_GLASS.asItem()),
			key(TFItems.QUEST_RAM_BANNER_PATTERN),
			key(TFBlocks.FINAL_BOSS_BOSS_SPAWNER.asItem()),
			key(TFItems.CUBE_TALISMAN),
			key(TFItems.CUBE_OF_ANNIHILATION),
			key(TFBlocks.CINDER_FURNACE.asItem()),
			key(TFBlocks.CINDER_LOG.asItem()),
			key(TFBlocks.CINDER_WOOD.asItem()),
			key(TFBlocks.SLIDER.asItem()),
			key(TFBlocks.BRAZIER.asItem()),
			key(TFBlocks.MAZE_SLIME_BLOCK.asItem())
		);
	}

	private static ResourceKey<Item> key(Item item) {
		return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
	}
}
