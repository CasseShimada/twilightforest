package twilightforest.datagen.data.custom;

import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFStructures;
import twilightforest.world.components.feature.BlockSpikeFeature;
import twilightforest.world.components.spelothem.SpeleothemVarietyConfig;
import twilightforest.world.components.spelothem.Stalactite;
import twilightforest.world.components.spelothem.StalactiteReloadListener;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class StalactiteGenerator implements DataProvider {
	private static final Identifier BLUE_ICE = entry("blue_ice_stalactite");
	private static final Identifier COAL = entry("coal_stalactite");
	private static final Identifier COPPER = entry("copper_stalactite");
	private static final Identifier DIAMOND = entry("diamond_stalactite");
	private static final Identifier EMERALD = entry("emerald_stalactite");
	private static final Identifier GLOWSTONE = entry("glowstone_stalactite");
	private static final Identifier GOLD = entry("gold_stalactite");
	private static final Identifier HILL_STONE = entry("hill_stone_stalactite");
	private static final Identifier ICE = entry("ice_stalactite");
	private static final Identifier IRON = entry("iron_stalactite");
	private static final Identifier LAPIS = entry("lapis_stalactite");
	private static final Identifier PACKED_ICE = entry("packed_ice_stalactite");
	private static final Identifier REDSTONE = entry("redstone_stalactite");

	private final PackOutput.PathProvider pathProvider;

	public StalactiteGenerator(FabricPackOutput output) {
		this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, StalactiteReloadListener.STALACTITE_DIRECTORY);
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		Map<Identifier, Stalactite> entries = entries();
		Map<Identifier, SpeleothemVarietyConfig> configs = configs();
		configs.values().forEach(config -> validateReferences(config, entries));
		return CompletableFuture.allOf(
			DataProvider.saveAll(output, Stalactite.CODEC, this.pathProvider, entries),
			DataProvider.saveAll(output, SpeleothemVarietyConfig.CODEC, this.pathProvider, configs)
		);
	}

	@Override
	public String getName() {
		return "Twilight Forest Stalactites";
	}

	private static Map<Identifier, Stalactite> entries() {
		return Map.ofEntries(
			Map.entry(BLUE_ICE, stalactite(Blocks.BLUE_ICE, 1.0F, 8, 1)),
			Map.entry(COAL, stalactite(Blocks.COAL_ORE, 0.8F, 12, 30)),
			Map.entry(COPPER, stalactite(Blocks.COPPER_ORE, 0.6F, 7, 25)),
			Map.entry(DIAMOND, stalactite(Blocks.DIAMOND_ORE, 0.5F, 4, 8)),
			Map.entry(EMERALD, stalactite(Blocks.EMERALD_ORE, 0.5F, 3, 8)),
			Map.entry(GLOWSTONE, stalactite(Blocks.GLOWSTONE, 0.5F, 8, 20)),
			Map.entry(GOLD, stalactite(Blocks.GOLD_ORE, 0.6F, 6, 10)),
			Map.entry(HILL_STONE, BlockSpikeFeature.STONE_STALACTITE),
			Map.entry(ICE, stalactite(Blocks.ICE, 0.6F, 10, 1)),
			Map.entry(IRON, stalactite(Blocks.IRON_ORE, 0.7F, 8, 40)),
			Map.entry(LAPIS, stalactite(Blocks.LAPIS_ORE, 0.8F, 8, 12)),
			Map.entry(PACKED_ICE, stalactite(Blocks.PACKED_ICE, 0.5F, 9, 1)),
			Map.entry(REDSTONE, stalactite(Blocks.REDSTONE_ORE, 0.8F, 8, 20))
		);
	}

	private static Map<Identifier, SpeleothemVarietyConfig> configs() {
		Map<Identifier, SpeleothemVarietyConfig> configs = new LinkedHashMap<>();
		addConfig(configs, TFStructures.HOLLOW_HILL_SMALL.identifier().getPath(), List.of(HILL_STONE), List.of(IRON, COAL, COPPER, GLOWSTONE), List.of(HILL_STONE), 0.2F, 1.0F, 0.8F);
		addConfig(configs, TFStructures.HOLLOW_HILL_MEDIUM.identifier().getPath(), List.of(HILL_STONE), List.of(IRON, COAL, COPPER, GLOWSTONE, GOLD, REDSTONE), List.of(HILL_STONE), 0.2F, 1.0F, 0.8F);
		addConfig(configs, TFStructures.HOLLOW_HILL_LARGE.identifier().getPath(), List.of(HILL_STONE), List.of(IRON, COAL, COPPER, GLOWSTONE, GOLD, REDSTONE, DIAMOND, LAPIS, EMERALD), List.of(HILL_STONE), 0.2F, 1.0F, 0.8F);
		addConfig(configs, TFStructures.HYDRA_LAIR.identifier().getPath(), List.of(), List.of(IRON, COAL, COPPER, GLOWSTONE, GOLD, REDSTONE), List.of(HILL_STONE), 1.0F, 1.0F, 1.0F / 16.0F);
		addConfig(configs, TFStructures.YETI_CAVE.identifier().getPath(), List.of(ICE, PACKED_ICE, BLUE_ICE), List.of(), List.of(), 0.0F, 1.0F, 0.0F);
		addConfig(configs, TFStructures.TROLL_CAVE.identifier().getPath(), List.of(HILL_STONE), List.of(), List.of(HILL_STONE), 0.0F, 1.0F, 0.25F);
		return configs;
	}

	private static void addConfig(Map<Identifier, SpeleothemVarietyConfig> configs, String type, List<Identifier> base, List<Identifier> ores, List<Identifier> stalagmites, float oreChance, float stalactiteChance, float stalagmiteChance) {
		SpeleothemVarietyConfig config = new SpeleothemVarietyConfig(type, base, ores, stalagmites, oreChance, stalactiteChance, stalagmiteChance, false);
		Identifier id = TwilightForestMod.prefix(type);
		if (configs.put(id, config) != null) {
			throw new IllegalStateException("Duplicate stalactite config " + id);
		}
	}

	private static void validateReferences(SpeleothemVarietyConfig config, Map<Identifier, Stalactite> entries) {
		Stream.of(config.baseStalactites(), config.oreStalactites(), config.stalagmites())
			.flatMap(List::stream)
			.filter(id -> !entries.containsKey(id))
			.findFirst()
			.ifPresent(id -> {
				throw new IllegalStateException("Missing stalactite entry " + id + " referenced by " + config.type());
			});
	}

	private static Stalactite stalactite(Block block, float sizeVariation, int maxLength, int weight) {
		return new Stalactite(Either.right(block), sizeVariation, maxLength, weight);
	}

	private static Identifier entry(String name) {
		return TwilightForestMod.prefix("entries/" + name);
	}
}
