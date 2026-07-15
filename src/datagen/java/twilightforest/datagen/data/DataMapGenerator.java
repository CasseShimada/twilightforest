package twilightforest.datagen.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopperCollection;
import net.minecraft.world.level.material.MapColor;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBiomes;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFEntities;
import twilightforest.util.datamaps.CrumbledBlock;
import twilightforest.util.datamaps.EntityTransformation;
import twilightforest.util.datamaps.MagicMapBiomeColor;
import twilightforest.util.datamaps.OreMapOreColor;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class DataMapGenerator implements DataProvider {
	private static final float DEFAULT_CRUMBLE_CHANCE = 0.2F;
	private static final float DISAPPEAR_CHANCE = 0.05F;

	private final PackOutput.PathProvider blockPath;
	private final PackOutput.PathProvider entityTypePath;
	private final PackOutput.PathProvider biomePath;

	public DataMapGenerator(FabricPackOutput output) {
		this.blockPath = output.createPathProvider(PackOutput.Target.DATA_PACK, "data_maps/block");
		this.entityTypePath = output.createPathProvider(PackOutput.Target.DATA_PACK, "data_maps/entity_type");
		this.biomePath = output.createPathProvider(PackOutput.Target.DATA_PACK, "data_maps/worldgen/biome");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return CompletableFuture.allOf(
			save(output, CrumbledBlock.CODEC, this.blockPath.json(TwilightForestMod.prefix("crumble_horn")), crumbleHorn()),
			save(output, OreMapOreColor.CODEC, this.blockPath.json(TwilightForestMod.prefix("ore_map_color")), oreMapColors()),
			save(output, EntityTransformation.CODEC, this.entityTypePath.json(TwilightForestMod.prefix("transformation_powder")), transformationPowder()),
			save(output, EntityTransformation.CODEC, this.entityTypePath.json(TwilightForestMod.prefix("ominous_fire")), ominousFire()),
			save(output, MagicMapBiomeColor.CODEC, this.biomePath.json(TwilightForestMod.prefix("magic_map_color")), magicMapColors())
		);
	}

	@Override
	public String getName() {
		return "Twilight Forest Data Maps";
	}

	private static Map<Identifier, CrumbledBlock> crumbleHorn() {
		Map<Identifier, CrumbledBlock> values = new LinkedHashMap<>();

		addCrumble(values, Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS);
		addCrumble(values, Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS);
		addCrumble(values, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
		addCrumble(values, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.BLACKSTONE);
		addCrumble(values, Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS);
		addCrumble(values, Blocks.DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_BRICKS);
		addCrumble(values, Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_TILES);
		addCrumble(values, TFBlocks.MAZESTONE_BRICK, TFBlocks.CRACKED_MAZESTONE);
		addCrumble(values, TFBlocks.UNDERBRICK, TFBlocks.CRACKED_UNDERBRICK);
		addCrumble(values, TFBlocks.DEADROCK, TFBlocks.CRACKED_DEADROCK);
		addCrumble(values, TFBlocks.CRACKED_DEADROCK, TFBlocks.WEATHERED_DEADROCK);
		addCrumble(values, TFBlocks.TOWERWOOD, TFBlocks.CRACKED_TOWERWOOD);
		addCrumble(values, TFBlocks.CASTLE_BRICK, TFBlocks.CRACKED_CASTLE_BRICK);
		addCrumble(values, TFBlocks.CRACKED_CASTLE_BRICK, TFBlocks.WORN_CASTLE_BRICK);
		addCrumble(values, TFBlocks.NAGASTONE_PILLAR, TFBlocks.CRACKED_NAGASTONE_PILLAR);
		addCrumble(values, TFBlocks.ETCHED_NAGASTONE, TFBlocks.CRACKED_ETCHED_NAGASTONE);
		addCrumble(values, TFBlocks.CASTLE_BRICK_STAIRS, TFBlocks.CRACKED_CASTLE_BRICK_STAIRS);
		addCrumble(values, TFBlocks.NAGASTONE_STAIRS_LEFT, TFBlocks.CRACKED_NAGASTONE_STAIRS_LEFT);
		addCrumble(values, TFBlocks.NAGASTONE_STAIRS_RIGHT, TFBlocks.CRACKED_NAGASTONE_STAIRS_RIGHT);

		addCrumble(values, Blocks.STONE, Blocks.COBBLESTONE);
		addCrumble(values, Blocks.COBBLESTONE, Blocks.GRAVEL);
		addCrumble(values, Blocks.SANDSTONE, Blocks.SAND);
		addCrumble(values, Blocks.RED_SANDSTONE, Blocks.RED_SAND);
		addCrumble(values, Blocks.GRASS_BLOCK, Blocks.DIRT);
		addCrumble(values, Blocks.PODZOL, Blocks.DIRT);
		addCrumble(values, Blocks.MYCELIUM, Blocks.DIRT);
		addCrumble(values, Blocks.COARSE_DIRT, Blocks.DIRT);
		addCrumble(values, Blocks.ROOTED_DIRT, Blocks.DIRT);

		addCrumbleChain(values, Blocks.COPPER_BLOCK.weathering());
		addCrumbleChain(values, Blocks.CUT_COPPER.weathering());
		addCrumbleChain(values, Blocks.CUT_COPPER_STAIRS.weathering());
		addCrumbleChain(values, Blocks.CUT_COPPER_SLAB.weathering());
		addCrumbleChain(values, Blocks.CHISELED_COPPER.weathering());
		addCrumbleChain(values, Blocks.COPPER_GRATE.weathering());
		addCrumbleChain(values, Blocks.COPPER_BULB.weathering());
		addCrumbleChain(values, Blocks.COPPER_TRAPDOOR.weathering());
		addCrumbleChain(values, Blocks.COPPER_DOOR.weathering());

		addCrumble(values, Blocks.GRAVEL, Blocks.AIR, DISAPPEAR_CHANCE);
		addCrumble(values, Blocks.DIRT, Blocks.AIR, DISAPPEAR_CHANCE);
		addCrumble(values, Blocks.SAND, Blocks.AIR, DISAPPEAR_CHANCE);
		addCrumble(values, Blocks.RED_SAND, Blocks.AIR, DISAPPEAR_CHANCE);
		addCrumble(values, Blocks.CLAY, Blocks.AIR, DISAPPEAR_CHANCE);
		addCrumble(values, Blocks.ANDESITE, Blocks.AIR, DISAPPEAR_CHANCE);
		addCrumble(values, Blocks.DIORITE, Blocks.AIR, DISAPPEAR_CHANCE);
		addCrumble(values, Blocks.GRANITE, Blocks.AIR, DISAPPEAR_CHANCE);
		return values;
	}

	private static Map<Identifier, OreMapOreColor> oreMapColors() {
		Map<Identifier, OreMapOreColor> values = new LinkedHashMap<>();
		addOreColor(values, MapColor.COLOR_ORANGE, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
		addOreColor(values, MapColor.COLOR_BLACK, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
		addOreColor(values, MapColor.RAW_IRON, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
		addOreColor(values, MapColor.LAPIS, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
		addOreColor(values, MapColor.GOLD, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE);
		addOreColor(values, MapColor.COLOR_RED, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
		addOreColor(values, MapColor.DIAMOND, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
		addOreColor(values, MapColor.EMERALD, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE);
		addOreColor(values, MapColor.TERRACOTTA_BROWN, Blocks.ANCIENT_DEBRIS);
		return values;
	}

	private static Map<Identifier, EntityTransformation> transformationPowder() {
		Map<Identifier, EntityTransformation> values = new LinkedHashMap<>();
		addTwoWayTransform(values, TFEntities.MINOTAUR, EntityTypes.ZOMBIFIED_PIGLIN);
		addTwoWayTransform(values, TFEntities.DEER, EntityTypes.COW);
		addTwoWayTransform(values, TFEntities.BOAR, EntityTypes.PIG);
		addTwoWayTransform(values, TFEntities.BIGHORN_SHEEP, EntityTypes.SHEEP);
		addTwoWayTransform(values, TFEntities.DWARF_RABBIT, EntityTypes.RABBIT);
		addTwoWayTransform(values, TFEntities.TINY_BIRD, EntityTypes.PARROT);
		addTwoWayTransform(values, TFEntities.RAVEN, EntityTypes.BAT);
		addTwoWayTransform(values, TFEntities.HOSTILE_WOLF, EntityTypes.WOLF);
		addTwoWayTransform(values, TFEntities.PENGUIN, EntityTypes.CHICKEN);
		addTwoWayTransform(values, TFEntities.HEDGE_SPIDER, EntityTypes.SPIDER);
		addTwoWayTransform(values, TFEntities.SWARM_SPIDER, EntityTypes.CAVE_SPIDER);
		addTwoWayTransform(values, TFEntities.WRAITH, EntityTypes.VEX);
		addTwoWayTransform(values, TFEntities.SKELETON_DRUID, EntityTypes.WITCH);
		addTwoWayTransform(values, TFEntities.CARMINITE_GHASTGUARD, EntityTypes.GHAST);
		addTwoWayTransform(values, TFEntities.TOWERWOOD_BORER, EntityTypes.SILVERFISH);
		addTwoWayTransform(values, TFEntities.MAZE_SLIME, EntityTypes.SLIME);
		return values;
	}

	private static Map<Identifier, EntityTransformation> ominousFire() {
		Map<Identifier, EntityTransformation> values = new LinkedHashMap<>();
		addTransform(values, EntityTypes.VILLAGER, EntityTypes.ZOMBIE_VILLAGER);
		addTransform(values, EntityTypes.PIGLIN, EntityTypes.ZOMBIFIED_PIGLIN);
		addTransform(values, EntityTypes.HORSE, EntityTypes.ZOMBIE_HORSE);
		return values;
	}

	private static Map<Identifier, MagicMapBiomeColor> magicMapColors() {
		Map<Identifier, MagicMapBiomeColor> values = new LinkedHashMap<>();
		addBiomeColor(values, TFBiomes.FOREST, MapColor.PLANT, 1);
		addBiomeColor(values, TFBiomes.DENSE_FOREST, MapColor.PLANT, 0);
		addBiomeColor(values, TFBiomes.LAKE, MapColor.WATER, 3);
		addBiomeColor(values, TFBiomes.STREAM, MapColor.WATER, 1);
		addBiomeColor(values, TFBiomes.SWAMP, MapColor.DIAMOND, 3);
		addBiomeColor(values, TFBiomes.FIRE_SWAMP, MapColor.NETHER, 1);
		addBiomeColor(values, TFBiomes.CLEARING, MapColor.GRASS, 2);
		addBiomeColor(values, TFBiomes.OAK_SAVANNAH, MapColor.GRASS, 0);
		addBiomeColor(values, TFBiomes.HIGHLANDS, MapColor.DIRT, 0);
		addBiomeColor(values, TFBiomes.THORNLANDS, MapColor.WOOD, 3);
		addBiomeColor(values, TFBiomes.FINAL_PLATEAU, MapColor.COLOR_LIGHT_GRAY, 2);
		addBiomeColor(values, TFBiomes.FIREFLY_FOREST, MapColor.EMERALD, 1);
		addBiomeColor(values, TFBiomes.DARK_FOREST, MapColor.COLOR_GREEN, 3);
		addBiomeColor(values, TFBiomes.DARK_FOREST_CENTER, MapColor.COLOR_ORANGE, 3);
		addBiomeColor(values, TFBiomes.SNOWY_FOREST, MapColor.SNOW, 1);
		addBiomeColor(values, TFBiomes.GLACIER, MapColor.ICE, 1);
		addBiomeColor(values, TFBiomes.MUSHROOM_FOREST, MapColor.COLOR_ORANGE, 0);
		addBiomeColor(values, TFBiomes.DENSE_MUSHROOM_FOREST, MapColor.COLOR_PINK, 0);
		addBiomeColor(values, TFBiomes.ENCHANTED_FOREST, MapColor.COLOR_CYAN, 2);
		addBiomeColor(values, TFBiomes.SPOOKY_FOREST, MapColor.COLOR_PURPLE, 0);
		return values;
	}

	private static void addCrumble(Map<Identifier, CrumbledBlock> values, Block from, Block to) {
		addCrumble(values, from, to, DEFAULT_CRUMBLE_CHANCE);
	}

	private static void addCrumble(Map<Identifier, CrumbledBlock> values, Block from, Block to, float chance) {
		values.put(BuiltInRegistries.BLOCK.getKey(from), new CrumbledBlock(to, chance));
	}

	private static void addCrumbleChain(Map<Identifier, CrumbledBlock> values, WeatheringCopperCollection.ByState<Block> blocks) {
		addCrumble(values, blocks.oxidized(), blocks.weathered());
		addCrumble(values, blocks.weathered(), blocks.exposed());
		addCrumble(values, blocks.exposed(), blocks.unaffected());
	}

	private static void addOreColor(Map<Identifier, OreMapOreColor> values, MapColor color, Block... blocks) {
		for (Block block : blocks) {
			values.put(BuiltInRegistries.BLOCK.getKey(block), new OreMapOreColor(color));
		}
	}

	private static void addTransform(Map<Identifier, EntityTransformation> values, EntityType<?> from, EntityType<?> to) {
		values.put(BuiltInRegistries.ENTITY_TYPE.getKey(from), new EntityTransformation(to));
	}

	private static void addTwoWayTransform(Map<Identifier, EntityTransformation> values, EntityType<?> twilightForestType, EntityType<?> vanillaType) {
		addTransform(values, twilightForestType, vanillaType);
		addTransform(values, vanillaType, twilightForestType);
	}

	private static void addBiomeColor(Map<Identifier, MagicMapBiomeColor> values, ResourceKey<Biome> biome, MapColor color, int brightness) {
		values.put(biome.identifier(), new MagicMapBiomeColor(color, brightness));
	}

	private static <T> CompletableFuture<?> save(CachedOutput output, Codec<T> valueCodec, Path path, Map<Identifier, T> values) {
		return DataProvider.saveStable(output, DataMapFile.codec(valueCodec), new DataMapFile<>(values), path);
	}

	private record DataMapFile<T>(Map<Identifier, T> values) {
		private static <T> Codec<DataMapFile<T>> codec(Codec<T> valueCodec) {
			return RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(Identifier.CODEC, valueCodec).fieldOf("values").forGetter(DataMapFile::values)
			).apply(instance, DataMapFile::new));
		}
	}
}
