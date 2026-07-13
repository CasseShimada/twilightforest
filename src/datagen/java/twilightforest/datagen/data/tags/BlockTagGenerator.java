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
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
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
		this.addProtectedMaterialTags();
		this.addTreeTags();
		this.addLogTags();
		this.addWoodFamilyTags();
		this.addShapeTags();
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

	private void addWoodFamilyTags() {
		TFBlockFamilies.getAllFamilies().forEach(family -> {
			this.builder(BlockTags.PLANKS).add(key(family.getBaseBlock()));
			this.addFamilyVariant(BlockTags.WOODEN_BUTTONS, family, BlockFamily.Variant.BUTTON);
			this.addFamilyVariant(BlockTags.WOODEN_DOORS, family, BlockFamily.Variant.DOOR);
			this.addFamilyVariant(BlockTags.WOODEN_FENCES, family, BlockFamily.Variant.FENCE);
			this.addFamilyVariant(BlockTags.FENCE_GATES, family, BlockFamily.Variant.FENCE_GATE);
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

	private static ResourceKey<Block> key(Block block) {
		return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
	}
}
