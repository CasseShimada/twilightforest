package twilightforest.world.components.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.util.jigsaw.JigsawPlaceContext;
import twilightforest.util.jigsaw.JigsawRecord;
import twilightforest.world.components.structures.lichtowerrevamp.StructureTemplateDefinitions;
import twilightforest.world.components.structures.markerhandler.TemplateMarkerHandler;
import twilightforest.world.components.structures.util.ProgressionPiece;
import twilightforest.world.components.structures.util.TemplateMarkerHandlerList;
import twilightforest.world.components.structures.util.TemplatePoolInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class TwilightJigsawPiece extends TwilightTemplateStructurePiece implements ProgressionPiece, PieceBeardifierModifier {
	private static final Logger LOGGER = LogManager.getLogger(TwilightForestMod.ID + "/TwilightJigsawPiece");

	private static final String NBT_JIGSAW_SOURCE = "source";
	private static final String NBT_JIGSAW_CONNECTIONS = "connections";
	private static final String NBT_TERRAIN_ADAPT = "terrain_adaptation";
	private static final String NBT_TEMPLATE_PROCESSORS = "template_processors";
	private static final String NBT_PLACE_PROJECTION = "place_projection";
	private static final String NBT_GROUND_OFFSET = "ground_offset";
	private static final String NBT_IGNORE_WATERLOG = "ignore_waterlog";
	private static final String NBT_MARKER_HANDLERS = "marker_handlers";
	private static final String NBT_RANDOMIZED_PROCESSORS = "randomized_processors";
	private static final String NBT_POOL_ALIASES = "pool_aliases";
	private static final Codec<Map<String, String>> POOL_ALIASES_CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING);

	private final JigsawRecord sourceJigsaw;
	private final List<JigsawRecord> spareJigsaws;
	private final TerrainAdjustment terrainAdjustment;
	private final Optional<Holder<StructureProcessorList>> processors;
	private final StructureTemplatePool.Projection projection;
	private final Optional<Holder<TemplateMarkerHandlerList>> markerHandlers;
	private final int beardifierGroundDelta;
	private final StructureProcessorList serializedProcessors;
	private final Map<String, String> poolAliases;

	public static TwilightJigsawPiece defaultDeserialize(StructurePieceSerializationContext context, CompoundTag tag) {
		TwilightJigsawPiece piece = new TwilightJigsawPiece(TFStructurePieceTypes.TFJigsawTemplate, tag, context, readSettings(tag));
		piece.placeSettings().addProcessor(JigsawReplacementProcessor.INSTANCE);
		piece.placeSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
		return piece;
	}

	/** Compatibility entry point retained for Final Castle callers. */
	public static TwilightJigsawPiece initializeTemplateFromPool(
		Identifier templatePool,
		BlockPos parentJunctionPos,
		FrontAndTop parentOrientation,
		String selectName,
		RandomSource random,
		int generationDepth,
		StructureTemplateManager structureManager
	) {
		return StructureTemplateDefinitions.INSTANCE.initializeTemplateFromPool(
			templatePool,
			parentJunctionPos,
			parentOrientation,
			selectName,
			random,
			generationDepth,
			structureManager
		);
	}

	public static TwilightJigsawPiece defaultForTemplate(int generationDepth, StructureTemplateManager structureManager, Identifier templateLocation, JigsawPlaceContext jigsawContext) {
		TemplatePoolInstance defaults = TemplatePoolInstance.defaultsWithWeight(1);
		return defaultForTemplate(generationDepth, structureManager, templateLocation, jigsawContext, defaults, defaults.chooseRandomProcessors(RandomSource.create()));
	}

	public static TwilightJigsawPiece defaultForTemplate(
		int generationDepth,
		StructureTemplateManager structureManager,
		Identifier templateLocation,
		JigsawPlaceContext jigsawContext,
		TemplatePoolInstance templatePoolInstance,
		StructureProcessorList randomizedProcessors
	) {
		TwilightJigsawPiece piece = new TwilightJigsawPiece(
			TFStructurePieceTypes.TFJigsawTemplate,
			generationDepth,
			structureManager,
			templateLocation,
			jigsawContext,
			templatePoolInstance,
			randomizedProcessors,
			templatePoolInstance.poolAliases()
		);
		piece.placeSettings().addProcessor(JigsawReplacementProcessor.INSTANCE);
		piece.placeSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
		return piece;
	}

	public TwilightJigsawPiece(StructurePieceType structurePieceType, CompoundTag tag, StructurePieceSerializationContext context, StructurePlaceSettings placeSettings) {
		super(structurePieceType, tag, context, placeSettings);

		this.sourceJigsaw = readSourceFromNBT(tag);
		this.spareJigsaws = readConnectionsFromNBT(tag);
		this.terrainAdjustment = parseTerrainAdjustment(tag.getStringOr(NBT_TERRAIN_ADAPT, TerrainAdjustment.NONE.getSerializedName()));
		RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, context.registryAccess());

		this.processors = decodeOptional(StructureProcessorType.LIST_CODEC, registryOps, tag.get(NBT_TEMPLATE_PROCESSORS), NBT_TEMPLATE_PROCESSORS);
		this.projection = parseProjection(tag.getStringOr(NBT_PLACE_PROJECTION, StructureTemplatePool.Projection.RIGID.getSerializedName()));
		this.markerHandlers = decodeOptional(TemplateMarkerHandlerList.HOLDER_CODEC, registryOps, tag.get(NBT_MARKER_HANDLERS), NBT_MARKER_HANDLERS);
		this.beardifierGroundDelta = tag.getIntOr(NBT_GROUND_OFFSET, 0);

		if (tag.getBooleanOr(NBT_IGNORE_WATERLOG, false)) {
			this.placeSettings.setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING);
		}

		this.processors.ifPresent(holder -> holder.value().list().forEach(this.placeSettings::addProcessor));
		this.serializedProcessors = decodeOptional(StructureProcessorType.LIST_OBJECT_CODEC, registryOps, tag.get(NBT_RANDOMIZED_PROCESSORS), NBT_RANDOMIZED_PROCESSORS)
			.orElseGet(() -> new StructureProcessorList(List.of()));
		this.serializedProcessors.list().forEach(this.placeSettings::addProcessor);
		this.poolAliases = decodeOptional(POOL_ALIASES_CODEC, registryOps, tag.get(NBT_POOL_ALIASES), NBT_POOL_ALIASES).orElse(Map.of());
	}

	public TwilightJigsawPiece(StructurePieceType type, int generationDepth, StructureTemplateManager structureManager, Identifier templateLocation, JigsawPlaceContext jigsawContext) {
		this(type, generationDepth, structureManager, templateLocation, jigsawContext, TemplatePoolInstance.defaultsWithWeight(1), new StructureProcessorList(List.of()), Map.of());
	}

	public TwilightJigsawPiece(
		StructurePieceType type,
		int generationDepth,
		StructureTemplateManager structureManager,
		Identifier templateLocation,
		JigsawPlaceContext jigsawContext,
		TemplatePoolInstance templatePoolInstance,
		StructureProcessorList randomizedProcessors,
		Map<String, String> poolAliases
	) {
		super(type, generationDepth, structureManager, templateLocation, jigsawContext.placementSettings(), jigsawContext.templatePos());

		this.sourceJigsaw = jigsawContext.seedJigsaw();
		this.spareJigsaws = Collections.unmodifiableList(jigsawContext.spareJigsaws());
		this.terrainAdjustment = templatePoolInstance.terrainAdjustment();
		this.processors = templatePoolInstance.processors();
		this.projection = templatePoolInstance.projection();
		this.markerHandlers = templatePoolInstance.markerHandlers();
		this.beardifierGroundDelta = templatePoolInstance.beardifierGroundDelta().map(TemplatePoolInstance.HeightAdjustment::beardifierGroundDelta).orElse(0);
		if (templatePoolInstance.ignoreWorldWaterlog()) {
			this.placeSettings.setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING);
		}
		this.processors.ifPresent(holder -> holder.value().list().forEach(this.placeSettings::addProcessor));
		this.serializedProcessors = randomizedProcessors;
		this.serializedProcessors.list().forEach(this.placeSettings::addProcessor);
		this.poolAliases = Map.copyOf(poolAliases);
	}

	private static <T> Optional<T> decodeOptional(Codec<T> codec, RegistryOps<Tag> ops, Tag value, String key) {
		if (value == null) {
			return Optional.empty();
		}
		return codec.parse(ops, value).resultOrPartial(message -> LOGGER.error("Error deserializing {}: {}", key, message));
	}

	private static TerrainAdjustment parseTerrainAdjustment(String name) {
		return Arrays.stream(TerrainAdjustment.values())
			.filter(value -> value.getSerializedName().equals(name) || value.name().equalsIgnoreCase(name))
			.findFirst()
			.orElse(TerrainAdjustment.NONE);
	}

	private static StructureTemplatePool.Projection parseProjection(String name) {
		StructureTemplatePool.Projection projection = StructureTemplatePool.Projection.byName(name);
		return projection == null ? StructureTemplatePool.Projection.RIGID : projection;
	}

	protected static JigsawRecord readSourceFromNBT(CompoundTag structureTag) {
		return JigsawRecord.fromTag(structureTag.getCompoundOrEmpty(NBT_JIGSAW_SOURCE));
	}

	protected static List<JigsawRecord> readConnectionsFromNBT(CompoundTag structureTag) {
		ListTag connections = structureTag.getListOrEmpty(NBT_JIGSAW_CONNECTIONS);
		List<JigsawRecord> records = new ArrayList<>();
		for (Tag entry : connections) {
			if (entry instanceof CompoundTag connectionTag) {
				records.add(JigsawRecord.fromTag(connectionTag));
			}
		}
		return Collections.unmodifiableList(records);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag structureTag) {
		super.addAdditionalSaveData(context, structureTag);

		structureTag.put(NBT_JIGSAW_SOURCE, this.sourceJigsaw.toTag());
		ListTag connections = new ListTag();
		for (JigsawRecord record : this.spareJigsaws) {
			connections.add(record.toTag());
		}
		structureTag.put(NBT_JIGSAW_CONNECTIONS, connections);

		if (this.terrainAdjustment != TerrainAdjustment.NONE) {
			structureTag.putString(NBT_TERRAIN_ADAPT, this.terrainAdjustment.getSerializedName());
		}
		if (this.projection != StructureTemplatePool.Projection.RIGID) {
			structureTag.putString(NBT_PLACE_PROJECTION, this.projection.getSerializedName());
		}

		RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, context.registryAccess());
		this.processors.ifPresent(value -> encode(NBT_TEMPLATE_PROCESSORS, StructureProcessorType.LIST_CODEC, value, registryOps, structureTag));
		this.markerHandlers.ifPresent(value -> encode(NBT_MARKER_HANDLERS, TemplateMarkerHandlerList.HOLDER_CODEC, value, registryOps, structureTag));
		if (!this.serializedProcessors.list().isEmpty()) {
			encode(NBT_RANDOMIZED_PROCESSORS, StructureProcessorType.LIST_OBJECT_CODEC, this.serializedProcessors, registryOps, structureTag);
		}
		if (!this.poolAliases.isEmpty()) {
			encode(NBT_POOL_ALIASES, POOL_ALIASES_CODEC, this.poolAliases, registryOps, structureTag);
		}

		if (this.beardifierGroundDelta != 0) {
			structureTag.putInt(NBT_GROUND_OFFSET, this.beardifierGroundDelta);
		}
		if (!this.placeSettings.shouldApplyWaterlogging()) {
			structureTag.putBoolean(NBT_IGNORE_WATERLOG, true);
		}
	}

	private static <T> void encode(String key, Codec<T> codec, T value, RegistryOps<Tag> ops, CompoundTag target) {
		codec.encodeStart(ops, value).resultOrPartial(message -> LOGGER.error("Error serializing {}: {}", key, message)).ifPresent(tag -> target.put(key, tag));
	}

	/** Legacy expansion path retained for existing non-Camp pieces. */
	@Override
	public void addChildren(StructurePiece parent, StructurePieceAccessor pieceAccessor, RandomSource random) {
		super.addChildren(parent, pieceAccessor, random);
		for (int index = 0; index < this.spareJigsaws.size(); index++) {
			this.processJigsaw(parent, pieceAccessor, random, this.spareJigsaws.get(index), index);
		}
	}

	/** Hook retained for the migrated Lich Tower pieces that specialize their child graph. */
	protected void processJigsaw(StructurePiece parent, StructurePieceAccessor pieceAccessor, RandomSource random, JigsawRecord connection, int jigsawIndex) {
		Identifier templatePool = Identifier.parse(this.poolAliases.getOrDefault(connection.pool(), connection.pool()));
		BlockPos parentJunctionPos = this.templatePosition.offset(connection.pos());
		TwilightJigsawPiece child = StructureTemplateDefinitions.INSTANCE.initializeTemplateFromPool(
			templatePool,
			parentJunctionPos,
			connection.orientation(),
			connection.target(),
			random,
			this.genDepth + 1,
			this.structureManager
		);
		if (child == null || pieceAccessor.findCollisionPiece(child.boundingBox) != null) {
			return;
		}
		pieceAccessor.addPiece(child);
		child.addChildren(this, pieceAccessor, random);
	}

	public void addJigsaws(TwilightJigsawPiece parent, StructurePieceAccessor pieceAccessor, Structure.GenerationContext context) {
		RandomSource random = context.random();
		random.setSeed(random.nextLong() ^ (context.seed() * this.templatePosition.asLong()));
		for (JigsawRecord connection : this.spareJigsaws) {
			this.processJigsaw(parent, pieceAccessor, context, connection);
		}
	}

	private void processJigsaw(TwilightJigsawPiece parent, StructurePieceAccessor pieceAccessor, Structure.GenerationContext context, JigsawRecord connection) {
		Identifier templatePool = Identifier.parse(this.poolAliases.getOrDefault(connection.pool(), connection.pool()));
		BlockPos parentJunctionPos = this.templatePosition.offset(connection.pos());
		TwilightJigsawPiece child = StructureTemplateDefinitions.INSTANCE.initializeTemplateFromPool(
			templatePool,
			parentJunctionPos,
			connection.orientation(),
			connection.target(),
			context,
			this.genDepth + 1,
			parent.projection == StructureTemplatePool.Projection.TERRAIN_MATCHING
		);
		if (child == null || pieceAccessor.findCollisionPiece(child.boundingBox) != null) {
			return;
		}
		pieceAccessor.addPiece(child);
		child.addJigsaws(this, pieceAccessor, context);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox chunkBounds, ChunkPos chunkPos, BlockPos structureCenterPos) {
		super.postProcess(level, structureManager, chunkGenerator, random, chunkBounds, chunkPos, structureCenterPos);
		ChunkAccess chunk = level.getChunk(chunkPos.getWorldPosition());
		if (chunk instanceof ProtoChunk protoChunk) {
			for (StructureTemplate.StructureBlockInfo blockInfo : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.JIGSAW)) {
				if (chunkBounds.isInside(blockInfo.pos())) {
					protoChunk.markPosForPostProcessing(blockInfo.pos());
				}
			}
		}
	}

	@Override
	protected void handleDataMarker(String label, BlockPos pos, WorldGenLevel level, RandomSource random, BoundingBox chunkBounds, ChunkGenerator chunkGenerator) {
		super.handleDataMarker(label, pos, level, random, chunkBounds, chunkGenerator);
		if (this.markerHandlers.isEmpty()) {
			return;
		}

		for (TemplateMarkerHandler handler : this.markerHandlers.get().value().markerHandlers()) {
			if (handler.handleDataMarker(label, pos, level, random, chunkBounds, chunkGenerator, this.placeSettings.getRotation())) {
				break;
			}
		}
	}

	public JigsawRecord getSourceJigsaw() {
		return this.sourceJigsaw;
	}

	public BlockPos getSourcePosition() {
		return this.templatePosition.offset(this.sourceJigsaw.pos());
	}

	public List<JigsawRecord> getSpareJigsaws() {
		return this.spareJigsaws;
	}

	public List<JigsawRecord> matchSpareJigsaws(Predicate<JigsawRecord> filter) {
		List<JigsawRecord> matches = new ArrayList<>();
		for (JigsawRecord record : this.spareJigsaws) {
			if (filter.test(record)) {
				matches.add(record);
			}
		}
		return matches;
	}

	public int firstMatchIndex(Predicate<JigsawRecord> filter) {
		for (int index = 0; index < this.spareJigsaws.size(); index++) {
			if (filter.test(this.spareJigsaws.get(index))) {
				return index;
			}
		}
		return -1;
	}

	@Override
	public BoundingBox getBeardifierBox() {
		return this.boundingBox;
	}

	@Override
	public TerrainAdjustment getTerrainAdjustment() {
		return this.terrainAdjustment;
	}

	@Override
	public int getGroundLevelDelta() {
		return this.beardifierGroundDelta;
	}
}
