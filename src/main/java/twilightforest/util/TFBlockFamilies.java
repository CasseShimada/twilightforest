package twilightforest.util;

import com.google.common.collect.Maps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.world.level.block.Block;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// For datagen only. Load this after TFBlocks has registered its values.
public class TFBlockFamilies {

	private static final Map<Block, BlockFamily> MAP = Maps.newHashMap();

	public static final BlockFamily TWILIGHT_OAK = familyBuilder(TFBlocks.TWILIGHT_OAK_PLANKS.get())
		.stairs(TFBlocks.TWILIGHT_OAK_STAIRS.get())
		.slab(TFBlocks.TWILIGHT_OAK_SLAB)
		.button(TFBlocks.TWILIGHT_OAK_BUTTON)
		.fence(TFBlocks.TWILIGHT_OAK_FENCE)
		.fenceGate(TFBlocks.TWILIGHT_OAK_GATE)
		.pressurePlate(TFBlocks.TWILIGHT_OAK_PLATE)
		.door(TFBlocks.TWILIGHT_OAK_DOOR.get())
		.trapdoor(TFBlocks.TWILIGHT_OAK_TRAPDOOR.get())
		.sign(TFBlocks.TWILIGHT_OAK_SIGN.get(), TFBlocks.TWILIGHT_WALL_SIGN.get())
		.recipeGroupPrefix("wooden")
		.recipeUnlockedBy("has_planks")
		.getFamily();

	public static final BlockFamily CANOPY = familyBuilder(TFBlocks.CANOPY_PLANKS.get())
		.stairs(TFBlocks.CANOPY_STAIRS.get())
		.slab(TFBlocks.CANOPY_SLAB)
		.button(TFBlocks.CANOPY_BUTTON)
		.fence(TFBlocks.CANOPY_FENCE)
		.fenceGate(TFBlocks.CANOPY_GATE)
		.pressurePlate(TFBlocks.CANOPY_PLATE)
		.door(TFBlocks.CANOPY_DOOR.get())
		.trapdoor(TFBlocks.CANOPY_TRAPDOOR.get())
		.sign(TFBlocks.CANOPY_SIGN.get(), TFBlocks.CANOPY_WALL_SIGN.get())
		.recipeGroupPrefix("wooden")
		.recipeUnlockedBy("has_planks")
		.getFamily();

	public static final BlockFamily MANGROVE = familyBuilder(TFBlocks.MANGROVE_PLANKS.get())
		.stairs(TFBlocks.MANGROVE_STAIRS.get())
		.slab(TFBlocks.MANGROVE_SLAB)
		.button(TFBlocks.MANGROVE_BUTTON)
		.fence(TFBlocks.MANGROVE_FENCE)
		.fenceGate(TFBlocks.MANGROVE_GATE)
		.pressurePlate(TFBlocks.MANGROVE_PLATE)
		.door(TFBlocks.MANGROVE_DOOR.get())
		.trapdoor(TFBlocks.MANGROVE_TRAPDOOR.get())
		.sign(TFBlocks.MANGROVE_SIGN.get(), TFBlocks.MANGROVE_WALL_SIGN.get())
		.recipeGroupPrefix("wooden")
		.recipeUnlockedBy("has_planks")
		.getFamily();

	public static final BlockFamily DARKWOOD = familyBuilder(TFBlocks.DARK_PLANKS.get())
		.stairs(TFBlocks.DARK_STAIRS.get())
		.slab(TFBlocks.DARK_SLAB)
		.button(TFBlocks.DARK_BUTTON)
		.fence(TFBlocks.DARK_FENCE)
		.fenceGate(TFBlocks.DARK_GATE)
		.pressurePlate(TFBlocks.DARK_PLATE)
		.door(TFBlocks.DARK_DOOR.get())
		.trapdoor(TFBlocks.DARK_TRAPDOOR.get())
		.sign(TFBlocks.DARK_SIGN.get(), TFBlocks.DARK_WALL_SIGN.get())
		.recipeGroupPrefix("wooden")
		.recipeUnlockedBy("has_planks")
		.getFamily();

	public static final BlockFamily TIMEWOOD = familyBuilder(TFBlocks.TIME_PLANKS.get())
		.stairs(TFBlocks.TIME_STAIRS.get())
		.slab(TFBlocks.TIME_SLAB)
		.button(TFBlocks.TIME_BUTTON)
		.fence(TFBlocks.TIME_FENCE)
		.fenceGate(TFBlocks.TIME_GATE)
		.pressurePlate(TFBlocks.TIME_PLATE)
		.door(TFBlocks.TIME_DOOR.get())
		.trapdoor(TFBlocks.TIME_TRAPDOOR.get())
		.sign(TFBlocks.TIME_SIGN.get(), TFBlocks.TIME_WALL_SIGN.get())
		.recipeGroupPrefix("wooden")
		.recipeUnlockedBy("has_planks")
		.getFamily();

	public static final BlockFamily TRANSWOOD = familyBuilder(TFBlocks.TRANSFORMATION_PLANKS.get())
		.stairs(TFBlocks.TRANSFORMATION_STAIRS.get())
		.slab(TFBlocks.TRANSFORMATION_SLAB)
		.button(TFBlocks.TRANSFORMATION_BUTTON)
		.fence(TFBlocks.TRANSFORMATION_FENCE)
		.fenceGate(TFBlocks.TRANSFORMATION_GATE)
		.pressurePlate(TFBlocks.TRANSFORMATION_PLATE)
		.door(TFBlocks.TRANSFORMATION_DOOR.get())
		.trapdoor(TFBlocks.TRANSFORMATION_TRAPDOOR.get())
		.sign(TFBlocks.TRANSFORMATION_SIGN.get(), TFBlocks.TRANSFORMATION_WALL_SIGN.get())
		.recipeGroupPrefix("wooden")
		.recipeUnlockedBy("has_planks")
		.getFamily();

	public static final BlockFamily MINEWOOD = familyBuilder(TFBlocks.MINING_PLANKS.get())
		.stairs(TFBlocks.MINING_STAIRS.get())
		.slab(TFBlocks.MINING_SLAB)
		.button(TFBlocks.MINING_BUTTON)
		.fence(TFBlocks.MINING_FENCE)
		.fenceGate(TFBlocks.MINING_GATE)
		.pressurePlate(TFBlocks.MINING_PLATE)
		.door(TFBlocks.MINING_DOOR.get())
		.trapdoor(TFBlocks.MINING_TRAPDOOR.get())
		.sign(TFBlocks.MINING_SIGN.get(), TFBlocks.MINING_WALL_SIGN.get())
		.recipeGroupPrefix("wooden")
		.recipeUnlockedBy("has_planks")
		.getFamily();

	public static final BlockFamily SORTWOOD = familyBuilder(TFBlocks.SORTING_PLANKS.get())
		.stairs(TFBlocks.SORTING_STAIRS.get())
		.slab(TFBlocks.SORTING_SLAB)
		.button(TFBlocks.SORTING_BUTTON)
		.fence(TFBlocks.SORTING_FENCE)
		.fenceGate(TFBlocks.SORTING_GATE)
		.pressurePlate(TFBlocks.SORTING_PLATE)
		.door(TFBlocks.SORTING_DOOR.get())
		.trapdoor(TFBlocks.SORTING_TRAPDOOR.get())
		.sign(TFBlocks.SORTING_SIGN.get(), TFBlocks.SORTING_WALL_SIGN.get())
		.recipeGroupPrefix("wooden")
		.recipeUnlockedBy("has_planks")
		.getFamily();

	public static void verifyFamilyShapes(BlockFamily family, BlockFamily.Variant... required) {
		List<BlockFamily.Variant> missing = findMissingFamilyShapes(family, required);

		if (!missing.isEmpty())
			TwilightForestMod.LOGGER.warn("BlockFamily " + family + " for " + family.getBaseBlock() + " is missing variants for " + missing);
	}

	public static List<BlockFamily.Variant> findMissingFamilyShapes(BlockFamily family, BlockFamily.Variant... required) {
		ArrayList<BlockFamily.Variant> available = new ArrayList<>(Arrays.asList(required));
		available.removeAll(family.getVariants().keySet());
		return available;
	}

	private static BlockFamily.Builder familyBuilder(Block baseBlock) {
		BlockFamily.Builder builder = new BlockFamily.Builder(baseBlock);
		BlockFamily blockfamily = MAP.put(baseBlock, builder.getFamily());
		if (blockfamily != null) {
			throw new IllegalStateException("Duplicate family definition for " + BuiltInRegistries.BLOCK.getKey(baseBlock));
		} else {
			return builder;
		}
	}

	public static Stream<BlockFamily> getAllFamilies() {
		return MAP.values().stream();
	}
}
