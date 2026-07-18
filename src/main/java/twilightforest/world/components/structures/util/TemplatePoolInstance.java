package twilightforest.world.components.structures.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import twilightforest.util.WorldUtil;
import twilightforest.util.jigsaw.JigsawPlaceContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Per-template metadata used by Twilight Forest's weighted structure-template pools.
 *
 * <p>The integer codec alternative is intentionally retained for the older, simple
 * template definitions used by the Lich Tower and Final Castle. Camp definitions use
 * the full object form.</p>
 */
public record TemplatePoolInstance(
	int weight,
	Optional<Holder<StructureProcessorList>> processors,
	StructureTemplatePool.Projection projection,
	TerrainAdjustment terrainAdjustment,
	Optional<HeightAdjustment> beardifierGroundDelta,
	boolean ignoreWorldWaterlog,
	Optional<Holder<TemplateMarkerHandlerList>> markerHandlers,
	Optional<ChooseRandomProcessors> randomizedProcessors,
	Map<String, String> poolAliases
) {
	private static final Codec<TemplatePoolInstance> CODEC_DIRECT = Codec.withAlternative(
		RecordCodecBuilder.create(instance -> instance.group(
			Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(TemplatePoolInstance::weight),
			StructureProcessorType.LIST_CODEC.optionalFieldOf("processors").forGetter(TemplatePoolInstance::processors),
			StructureTemplatePool.Projection.CODEC.optionalFieldOf("projection", StructureTemplatePool.Projection.RIGID).forGetter(TemplatePoolInstance::projection),
			TerrainAdjustment.CODEC.optionalFieldOf("terrain_adaptation", TerrainAdjustment.NONE).forGetter(TemplatePoolInstance::terrainAdjustment),
			HeightAdjustment.CODEC.optionalFieldOf("height_adjustment").forGetter(TemplatePoolInstance::beardifierGroundDelta),
			Codec.BOOL.optionalFieldOf("ignore_world_waterlog", false).forGetter(TemplatePoolInstance::ignoreWorldWaterlog),
			Codec.lazyInitialized(() -> TemplateMarkerHandlerList.HOLDER_CODEC).optionalFieldOf("marker_handlers").forGetter(TemplatePoolInstance::markerHandlers),
			ChooseRandomProcessors.CODEC.optionalFieldOf("randomized_processors").forGetter(TemplatePoolInstance::randomizedProcessors),
			Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("pool_aliases", Map.of()).forGetter(TemplatePoolInstance::poolAliases)
		).apply(instance, TemplatePoolInstance::new)),
		Codec.intRange(0, Integer.MAX_VALUE),
		TemplatePoolInstance::defaultsWithWeight
	);

	public static final Codec<TemplatePoolInstance> CODEC = new TemplatePoolInstanceCodec();

	public static TemplatePoolInstance defaultsWithWeight(int weight) {
		return new TemplatePoolInstance(
			weight,
			Optional.empty(),
			StructureTemplatePool.Projection.RIGID,
			TerrainAdjustment.NONE,
			Optional.empty(),
			false,
			Optional.empty(),
			Optional.empty(),
			Map.of()
		);
	}

	public JigsawPlaceContext adjustContextForTerrain(JigsawPlaceContext placeContext, Structure.GenerationContext generationContext, boolean parentProjectsTerrain) {
		return this.beardifierGroundDelta
			.map(adjustment -> adjustment.adjustForTerrain(placeContext, generationContext, parentProjectsTerrain))
			.orElse(placeContext);
	}

	public StructureProcessorList chooseRandomProcessors(RandomSource randomSource) {
		return this.randomizedProcessors
			.map(processors -> processors.chooseRandomProcessors(randomSource))
			.orElseGet(() -> new StructureProcessorList(List.of()));
	}

	private boolean hasDefaultMetadata() {
		return this.processors.isEmpty()
			&& this.projection == StructureTemplatePool.Projection.RIGID
			&& this.terrainAdjustment == TerrainAdjustment.NONE
			&& this.beardifierGroundDelta.isEmpty()
			&& !this.ignoreWorldWaterlog
			&& this.markerHandlers.isEmpty()
			&& this.randomizedProcessors.isEmpty()
			&& this.poolAliases.isEmpty();
	}

	public record HeightAdjustment(Heightmap.Types heightType, int beardifierGroundDelta, Optional<Integer> groundJunctionDiffLimit) {
		public static final Codec<HeightAdjustment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(HeightAdjustment::heightType),
			Codec.INT.optionalFieldOf("y_offset", 0).forGetter(HeightAdjustment::beardifierGroundDelta),
			Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("ground_junction_diff_clamp").forGetter(HeightAdjustment::groundJunctionDiffLimit)
		).apply(instance, HeightAdjustment::new));

		public JigsawPlaceContext adjustForTerrain(JigsawPlaceContext placeContext, Structure.GenerationContext generationContext, boolean parentProjectsTerrain) {
			BoundingBox box = placeContext.makeBoundingBox(generationContext.structureTemplateManager());
			int adjustedY = parentProjectsTerrain
				? this.clampYLevelPos(generationContext, placeContext, placeContext.templatePos().offset(placeContext.seedJigsaw().pos()))
				: this.clampYLevelBox(generationContext, placeContext, box);

			return new JigsawPlaceContext(
				placeContext.templatePos().atY(adjustedY),
				placeContext.placementSettings().copy(),
				placeContext.seedJigsaw(),
				List.copyOf(placeContext.spareJigsaws()),
				placeContext.templateLocation()
			);
		}

		private int clampYLevelBox(Structure.GenerationContext generationContext, JigsawPlaceContext placeContext, BoundingBox box) {
			if (this.groundJunctionDiffLimit.isEmpty()) {
				return this.beardifierGroundDelta + WorldUtil.adjustForTerrain(generationContext, box.minX(), box.minZ(), box.maxX(), box.maxZ(), 2, this.heightType);
			}

			int templateY = placeContext.templatePos().getY();
			int varianceLimit = this.groundJunctionDiffLimit.get();
			if (varianceLimit == 0) {
				return templateY;
			}

			int terrainY = WorldUtil.adjustForTerrain(generationContext, box.minX(), box.minZ(), box.maxX(), box.maxZ(), 2, this.heightType);
			return Mth.clamp(this.beardifierGroundDelta + terrainY, templateY - varianceLimit, templateY + varianceLimit);
		}

		private int clampYLevelPos(Structure.GenerationContext generationContext, JigsawPlaceContext placeContext, BlockPos pos) {
			if (this.groundJunctionDiffLimit.isEmpty()) {
				return this.beardifierGroundDelta + generationContext.chunkGenerator().getFirstOccupiedHeight(pos.getX(), pos.getZ(), this.heightType, generationContext.heightAccessor(), generationContext.randomState());
			}

			int templateY = placeContext.templatePos().getY();
			int varianceLimit = this.groundJunctionDiffLimit.get();
			if (varianceLimit == 0) {
				return templateY;
			}

			int terrainY = generationContext.chunkGenerator().getFirstOccupiedHeight(pos.getX(), pos.getZ(), this.heightType, generationContext.heightAccessor(), generationContext.randomState());
			return Mth.clamp(this.beardifierGroundDelta + terrainY, templateY - varianceLimit, templateY + varianceLimit);
		}
	}

	private static final class TemplatePoolInstanceCodec implements Codec<TemplatePoolInstance> {
		@Override
		public <T> DataResult<T> encode(TemplatePoolInstance input, DynamicOps<T> ops, T prefix) {
			if (input.hasDefaultMetadata()) {
				return DataResult.success(ops.createInt(input.weight));
			}
			return CODEC_DIRECT.encode(input, ops, prefix);
		}

		@Override
		public <T> DataResult<Pair<TemplatePoolInstance, T>> decode(DynamicOps<T> ops, T input) {
			return CODEC_DIRECT.decode(ops, input);
		}
	}

	public record ChooseRandomProcessors(List<WeightedList<StructureProcessor>> processors) {
		public static final Codec<ChooseRandomProcessors> CODEC = WeightedList.codec(StructureProcessorType.SINGLE_CODEC)
			.listOf()
			.xmap(ChooseRandomProcessors::new, ChooseRandomProcessors::processors);

		public StructureProcessorList chooseRandomProcessors(RandomSource random) {
			List<StructureProcessor> chosenProcessors = new ArrayList<>();
			for (WeightedList<StructureProcessor> list : this.processors) {
				list.getRandom(random).ifPresent(chosenProcessors::add);
			}
			return new StructureProcessorList(Collections.unmodifiableList(chosenProcessors));
		}
	}
}
