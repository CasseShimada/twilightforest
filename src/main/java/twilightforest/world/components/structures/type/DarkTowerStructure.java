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
import twilightforest.TwilightForestMod;
import twilightforest.tags.TFBiomeTags;
import twilightforest.init.TFEntities;
import twilightforest.init.TFMapDecorations;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.structures.darktower.DarkTowerMainComponent;
import twilightforest.world.components.structures.util.ControlledSpawningStructure;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static twilightforest.world.components.structures.util.ControlledSpawns.ControlledSpawningConfig.weightedSpawn;

public class DarkTowerStructure extends ControlledSpawningStructure {
	public static final MapCodec<DarkTowerStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
		controlledSpawningCodec(instance).apply(instance, DarkTowerStructure::new)
	);

	public DarkTowerStructure(ControlledSpawningConfig controlledSpawningConfig, AdvancementLockConfig advancementLockConfig, HintConfig hintConfig, DecorationConfig decorationConfig, boolean centerInChunk, Optional<Holder<MapDecorationType>> structureIcon, StructureSettings structureSettings) {
		super(controlledSpawningConfig, advancementLockConfig, hintConfig, decorationConfig, centerInChunk, structureIcon, structureSettings);
	}

	@Override
	protected @Nullable StructurePiece getFirstPiece(GenerationContext context, RandomSource random, ChunkPos chunkPos, int x, int y, int z) {
		return new DarkTowerMainComponent(random, 0, x, y, z);
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.DARK_TOWER;
	}

	public static DarkTowerStructure buildDarkTowerConfig(BootstrapContext<Structure> context) {
		return new DarkTowerStructure(
			ControlledSpawningConfig.create(List.of(List.of(
				weightedSpawn(TFEntities.CARMINITE_GOLEM.get(), 10, 1, 2),
				weightedSpawn(EntityTypes.SKELETON, 10, 1, 2),
				weightedSpawn(EntityTypes.CREEPER, 5, 1, 1),
				weightedSpawn(EntityTypes.ENDERMAN, 2, 1, 2),
				weightedSpawn(EntityTypes.WITCH, 1, 1, 1),
				weightedSpawn(TFEntities.CARMINITE_GHASTLING.get(), 10, 1, 2),
				weightedSpawn(TFEntities.CARMINITE_BROODLING.get(), 10, 4, 4),
				weightedSpawn(TFEntities.PINCH_BEETLE.get(), 10, 1, 1)
			), List.of(
				// roof ghasts
				weightedSpawn(TFEntities.CARMINITE_GHASTGUARD.get(), 10, 1, 2)
			)), List.of(), List.of(
				// aquarium squids (only in aquariums between y = 35 and y = 64. :/
				weightedSpawn(EntityTypes.SQUID, 10, 4, 4)
			)),
			new AdvancementLockConfig(List.of(TwilightForestMod.prefix("progress_knights"))),
			new HintConfig(HintConfig.book("darktower", 3), TFEntities.KOBOLD.get()),
			new DecorationConfig(1, false, true, true),
			true, Optional.of(Holder.direct(TFMapDecorations.DARK_TOWER)),
			new StructureSettings(
				context.lookup(Registries.BIOME).getOrThrow(TFBiomeTags.VALID_DARK_TOWER_BIOMES),
				Arrays.stream(MobCategory.values()).collect(Collectors.toMap(category -> category, category -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedList.of()))), // Landmarks have Controlled Mob spawning
				GenerationStep.Decoration.SURFACE_STRUCTURES,
				TerrainAdjustment.BEARD_THIN
			)
		);
	}
}
