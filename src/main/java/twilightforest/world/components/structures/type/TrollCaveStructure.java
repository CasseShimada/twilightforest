package twilightforest.world.components.structures.type;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import org.jetbrains.annotations.Nullable;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFEntities;
import twilightforest.init.TFMapDecorations;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.init.TFStructureTypes;
import twilightforest.init.custom.StructureSpeleothemConfigs;
import twilightforest.tags.TFBiomeTags;
import twilightforest.world.components.spelothem.StructureSpeleothemConfig;
import twilightforest.world.components.structures.trollcave.TrollCaveMainComponent;
import twilightforest.world.components.structures.util.ConfigurableSpawns;
import twilightforest.world.components.structures.util.ControlledSpawns;
import twilightforest.world.components.structures.util.ProgressionStructure;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static twilightforest.world.components.structures.util.ControlledSpawns.ControlledSpawningConfig.weightedSpawn;

public class TrollCaveStructure extends ProgressionStructure implements ConfigurableSpawns {
	public static final MapCodec<TrollCaveStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(
			ControlledSpawns.ControlledSpawningConfig.FLAT_CODEC.forGetter(ConfigurableSpawns::getConfig),
			StructureSpeleothemConfigs.CODEC.fieldOf("speleothem_config").forGetter(s -> s.speleothemConfig)
		)
		.and(progressionCodec(instance))
		.apply(instance, TrollCaveStructure::new)
	);

	private final ControlledSpawningConfig controlledSpawningConfig;
	private final Holder.Reference<StructureSpeleothemConfig> speleothemConfig;

	public TrollCaveStructure(ControlledSpawningConfig controlledSpawningConfig, Holder<StructureSpeleothemConfig> speleothemConfig, AdvancementLockConfig advancementLockConfig, HintConfig hintConfig, DecorationConfig decorationConfig, boolean centerInChunk, Optional<Holder<MapDecorationType>> structureIcon, StructureSettings structureSettings) {
		super(advancementLockConfig, hintConfig, decorationConfig, centerInChunk, structureIcon, structureSettings);

		this.controlledSpawningConfig = controlledSpawningConfig;
		this.speleothemConfig = (Holder.Reference<StructureSpeleothemConfig>) speleothemConfig;
	}

	@Override
	protected @Nullable StructurePiece getFirstPiece(GenerationContext context, RandomSource random, ChunkPos chunkPos, int x, int y, int z) {
		return new TrollCaveMainComponent(TFStructurePieceTypes.TFTCMai.get(), 0, x, y + 11, z, this.speleothemConfig);
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.TROLL_CAVE.get();
	}

	@Override
	public ControlledSpawningConfig getConfig() {
		return this.controlledSpawningConfig;
	}

	public static TrollCaveStructure buildTrollCaveConfig(BootstrapContext<Structure> context) {
		return new TrollCaveStructure(
			ControlledSpawningConfig.create(List.of(List.of(
				weightedSpawn(EntityTypes.CREEPER, 5, 1, 2),
				weightedSpawn(EntityTypes.SKELETON, 10, 1, 2),
				weightedSpawn(TFEntities.TROLL.get(), 20, 1, 2),
				weightedSpawn(EntityTypes.WITCH, 5, 1, 1)
			), List.of(
				// cloud monsters
				weightedSpawn(TFEntities.GIANT_MINER.get(), 10, 1, 1),
				weightedSpawn(TFEntities.ARMORED_GIANT.get(), 10, 1, 1)
			)), List.of(), List.of()),
			context.lookup(TFRegistries.Keys.STRUCTURE_SPELEOTHEM_SETTINGS).getOrThrow(StructureSpeleothemConfigs.TROLL_CAVE),
			new AdvancementLockConfig(List.of(TwilightForestMod.prefix("progress_merge"))),
			new HintConfig(HintConfig.book("trollcave", 3), TFEntities.KOBOLD.get()),
			new DecorationConfig(4, true, true, false),
			false, Optional.of(Holder.direct(TFMapDecorations.TROLL_CAVES)),
			new StructureSettings(
				context.lookup(Registries.BIOME).getOrThrow(TFBiomeTags.VALID_TROLL_CAVE_BIOMES),
				Arrays.stream(MobCategory.values()).collect(Collectors.toMap(category -> category, category -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedList.of()))), // Landmarks have Controlled Mob spawning
				GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
				TerrainAdjustment.BURY
			)
		);
	}
}
