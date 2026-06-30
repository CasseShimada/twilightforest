package twilightforest.init;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.timeline.Timeline;

import java.util.Optional;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.init.custom.BiomeLayerStack;
import twilightforest.world.components.biomesources.TFBiomeProvider;
import twilightforest.world.components.layer.BiomeDensitySource;
import twilightforest.world.registration.surface_rules.TFSurfaceRules;

import java.util.List;

public class TFDimensionData {

	// Avoid at all costs. If you need SeaLevel info instead and are ServerSide, use WorldUtil.getGeneratorSeaLevel()
	// TODO How should we fix this for clients? Would need to sync serverlevel's sealevel to clients
	@Deprecated // FIXME Make private
	public static final int SEALEVEL = 0;

	public static final ResourceKey<DimensionType> TWILIGHT_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, TwilightForestMod.prefix("twilight_forest_type"));

	public static final ResourceKey<NoiseGeneratorSettings> TWILIGHT_NOISE_GEN = ResourceKey.create(Registries.NOISE_SETTINGS, TwilightForestMod.prefix("twilight_noise_gen"));
	public static final ResourceKey<NoiseGeneratorSettings> SKYLIGHT_NOISE_GEN = ResourceKey.create(Registries.NOISE_SETTINGS, TwilightForestMod.prefix("skylight_noise_gen"));

	public static final ResourceKey<LevelStem> TWILIGHT_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM, TFDimension.DIMENSION);

	private static DimensionType twilightDimType(BootstrapContext<DimensionType> context) {
		TagKey<Timeline> timelineTag = TagKey.create(Registries.TIMELINE, TwilightForestMod.prefix("in_twilight"));
		HolderSet<Timeline> timelines = context.lookup(Registries.TIMELINE).getOrThrow(timelineTag);
		HolderGetter<WorldClock> worldClocks = context.lookup(Registries.WORLD_CLOCK);
		HolderSet<net.minecraft.world.level.block.Block> infiniburn = context.lookup(Registries.BLOCK).getOrThrow(BlockTags.INFINIBURN_OVERWORLD);
		EnvironmentAttributeMap attributes = EnvironmentAttributeMap.builder()
			.set(EnvironmentAttributes.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK)
			.set(EnvironmentAttributes.CAN_START_RAID, false)
			.set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
			.set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, true)
			.set(EnvironmentAttributes.PIGLINS_ZOMBIFY, true)
			.build();

		return new DimensionType(
			false, //fixed time
			true, //skylight
			false, //ceiling
			false, //ender dragon fight
			1 / 8.0, //coordinate scale
			-32, // Minimum Y Level
			32 + 256, // Height + Min Y = Max Y
			32 + 256, // Logical Height
			infiniburn,
			0.01f,
			new DimensionType.MonsterSettings(UniformInt.of(0, 7), 7),
			DimensionType.Skybox.OVERWORLD,
			net.minecraft.world.level.CardinalLighting.Type.DEFAULT,
			attributes,
			timelines,
			Optional.of(worldClocks.getOrThrow(WorldClocks.OVERWORLD))
		);
	}

	public static void bootstrapType(BootstrapContext<DimensionType> context) {
		context.register(TWILIGHT_DIM_TYPE, twilightDimType(context));
	}

	public static NoiseGeneratorSettings makeNoiseSettings(BootstrapContext<NoiseGeneratorSettings> context, boolean skylight) {
		HolderGetter<DensityFunction> densityFunctions = context.lookup(Registries.DENSITY_FUNCTION);
		DensityFunction finalDensity = new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(skylight ? TFDensityFunctions.SKYLIGHT_TERRAIN : TFDensityFunctions.FORESTED_TERRAIN));

		NoiseSettings tfNoise = NoiseSettings.create(
			-32, //TODO Deliberate over this. For now it'll be -32
			256,
			2,
			2
		);

		return new NoiseGeneratorSettings(
			tfNoise,
			Blocks.STONE.defaultBlockState(),
			skylight ? Blocks.AIR.defaultBlockState() : Blocks.WATER.defaultBlockState(),
			new NoiseRouter(
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				finalDensity,
				finalDensity,
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero()
			),
			TFSurfaceRules.tfSurface(context.lookup(Registries.BIOME)),
			List.of(),
			TFDimensionData.SEALEVEL,
			false,
			false,
			false,
			false
		);
	}

	public static void bootstrapNoise(BootstrapContext<NoiseGeneratorSettings> context) {
		context.register(TWILIGHT_NOISE_GEN, makeNoiseSettings(context, false));
		context.register(SKYLIGHT_NOISE_GEN, makeNoiseSettings(context, true));
	}

	public static void bootstrapStem(BootstrapContext<LevelStem> context) {
		HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
		HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

		HolderGetter<BiomeDensitySource> biomeDataRegistry = context.lookup(TFRegistries.Keys.BIOME_TERRAIN_DATA);

		NoiseBasedChunkGenerator twilightChunkGenerator = new NoiseBasedChunkGenerator(
			new TFBiomeProvider(biomeDataRegistry.getOrThrow(BiomeLayerStack.BIOME_GRID)),
			noiseGenSettings.getOrThrow(TWILIGHT_NOISE_GEN)
		);

		LevelStem stem = new LevelStem(
			dimTypes.getOrThrow(TWILIGHT_DIM_TYPE),
			twilightChunkGenerator
		);

		context.register(TWILIGHT_LEVEL_STEM, stem);
	}
}
