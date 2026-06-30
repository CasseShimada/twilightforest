package twilightforest.world.components.structures.type;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.tags.TFBiomeTags;
import twilightforest.init.TFEntities;
import twilightforest.init.TFMapDecorations;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.structures.stronghold.StrongholdEntranceComponent;
import twilightforest.world.components.structures.util.ControlledSpawningStructure;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static twilightforest.world.components.structures.util.ControlledSpawns.ControlledSpawningConfig.weightedSpawn;

public class KnightStrongholdStructure extends ControlledSpawningStructure {
	public static final MapCodec<KnightStrongholdStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
		controlledSpawningCodec(instance).apply(instance, KnightStrongholdStructure::new)
	);

	public KnightStrongholdStructure(ControlledSpawningConfig controlledSpawningConfig, AdvancementLockConfig advancementLockConfig, HintConfig hintConfig, DecorationConfig decorationConfig, boolean centerInChunk, Optional<Holder<MapDecorationType>> structureIcon, StructureSettings structureSettings) {
		super(controlledSpawningConfig, advancementLockConfig, hintConfig, decorationConfig, centerInChunk, structureIcon, structureSettings);
	}

	@Override
	protected @Nullable StructurePiece getFirstPiece(GenerationContext context, RandomSource random, ChunkPos chunkPos, int x, int y, int z) {
		return new StrongholdEntranceComponent(0, x, y + random.nextInt(3) == 0 ? 5 : 1, z);
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.KNIGHT_STRONGHOLD.get();
	}

	public static KnightStrongholdStructure buildKnightStrongholdConfig(BootstrapContext<Structure> context) {
		return new KnightStrongholdStructure(
			ControlledSpawningConfig.justMonsters(List.of(List.of(
				weightedSpawn(TFEntities.BLOCKCHAIN_GOBLIN.get(), 10, 1, 2),
				weightedSpawn(TFEntities.LOWER_GOBLIN_KNIGHT.get(), 5, 1, 2),
				weightedSpawn(TFEntities.HELMET_CRAB.get(), 10, 2, 4),
				weightedSpawn(TFEntities.SLIME_BEETLE.get(), 10, 2, 3),
				weightedSpawn(TFEntities.REDCAP_SAPPER.get(), 2, 1, 2),
				weightedSpawn(TFEntities.KOBOLD.get(), 10, 2, 4),
				weightedSpawn(EntityTypes.CREEPER, 5, 1, 2),
				weightedSpawn(EntityTypes.SLIME, 5, 4, 4)
			))),
			new AdvancementLockConfig(List.of(TwilightForestMod.prefix("progress_trophy_pedestal"))),
			new HintConfig(HintConfig.book("tfstronghold", 4), TFEntities.KOBOLD.get()),
			new DecorationConfig(3, true, false, false),
			true, Optional.of(Holder.direct(TFMapDecorations.KNIGHT_STRONGHOLD.get())),
			new StructureSettings(
				context.lookup(Registries.BIOME).getOrThrow(TFBiomeTags.VALID_KNIGHT_STRONGHOLD_BIOMES),
				Arrays.stream(MobCategory.values()).collect(Collectors.toMap(category -> category, category -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedList.of()))), // Landmarks have Controlled Mob spawning
				GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
				TerrainAdjustment.BURY
			)
		);
	}

	@Override
	protected StructureStart createStart(ChunkPos chunkPos, int reference, GenerationStub generationStub) {
		return new StructureStart(this, chunkPos, reference, generationStub.getPiecesBuilder().build());
	}
}
