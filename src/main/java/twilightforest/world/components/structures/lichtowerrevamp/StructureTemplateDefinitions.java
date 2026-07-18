package twilightforest.world.components.structures.lichtowerrevamp;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.Nullable;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.util.jigsaw.JigsawPlaceContext;
import twilightforest.world.components.structures.TwilightJigsawPiece;
import twilightforest.world.components.structures.util.TemplatePoolInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Reloads Twilight Forest's template-pool definitions. The 26.2 listener uses
 * registry-aware JSON operations so inline processor lists and marker-handler
 * holders in Camp definitions decode correctly.
 */
public final class StructureTemplateDefinitions extends SimpleJsonResourceReloadListener<StructureTemplateDefinition> implements IdentifiableResourceReloadListener {
	public static final String DIRECTORY = "twilight/template_definition";

	private static final ResourceKey<Registry<StructureTemplateDefinition>> RESOURCE_KEY =
		ResourceKey.createRegistryKey(TFRegistries.Keys.namedRegistry("template_definition"));

	/**
	 * Available before the first reload for legacy simple-pool callers, and replaced
	 * atomically by the registry-aware listener Fabric creates for each server.
	 */
	public static volatile StructureTemplateDefinitions INSTANCE = new StructureTemplateDefinitions();

	private final Map<Identifier, WeightedList<TemplatePoolEntry>> templatePools = new HashMap<>();

	public StructureTemplateDefinitions() {
		super(StructureTemplateDefinition.CODEC, FileToIdConverter.json(DIRECTORY));
	}

	public StructureTemplateDefinitions(HolderLookup.Provider registries) {
		super(registries, StructureTemplateDefinition.CODEC, RESOURCE_KEY);
		INSTANCE = this;
	}

	@Override
	public Identifier getFabricId() {
		return TwilightForestMod.prefix("structure_templates");
	}

	@Override
	protected void apply(Map<Identifier, StructureTemplateDefinition> definitions, ResourceManager manager, ProfilerFiller profiler) {
		this.templatePools.clear();
		Map<Identifier, WeightedList.Builder<TemplatePoolEntry>> builders = new HashMap<>();

		definitions.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(definitionEntry -> {
			Identifier templateId = definitionEntry.getKey();
			definitionEntry.getValue().poolWeights().entrySet().stream()
				.filter(poolEntry -> poolEntry.getValue().weight() > 0)
				.sorted(Map.Entry.comparingByKey())
				.forEach(poolEntry -> builders.computeIfAbsent(poolEntry.getKey(), ignored -> WeightedList.builder())
					.add(new TemplatePoolEntry(templateId, poolEntry.getValue()), poolEntry.getValue().weight()));
		});

		builders.forEach((poolId, builder) -> this.templatePools.put(poolId, builder.build()));
	}

	private Optional<TemplatePoolEntry> getRandomEntry(RandomSource random, Identifier templatePoolId) {
		WeightedList<TemplatePoolEntry> pool = this.templatePools.get(templatePoolId);
		return pool == null ? Optional.empty() : pool.getRandom(random);
	}

	@Nullable
	public Identifier getRandomTemplate(RandomSource random, Identifier templatePoolId) {
		return this.getRandomEntry(random, templatePoolId).map(TemplatePoolEntry::templateId).orElse(null);
	}

	/** Compatibility name retained for the migrated Lich Tower and Final Castle. */
	@Nullable
	public Identifier rollTemplatePool(RandomSource random, Identifier templatePoolId) {
		return this.getRandomTemplate(random, templatePoolId);
	}

	// https://en.wikipedia.org/wiki/Reservoir_sampling
	public Iterable<Identifier> getShuffledSequence(RandomSource random, Identifier templatePoolId) {
		WeightedList<TemplatePoolEntry> pool = this.templatePools.get(templatePoolId);
		if (pool == null) {
			return Collections.emptyList();
		}

		Map<Identifier, Double> sampled = new HashMap<>();
		for (Weighted<TemplatePoolEntry> entry : pool.unwrap()) {
			double randomValue = random.nextDouble();
			sampled.put(entry.value().templateId(), -Math.log(randomValue) / entry.weight());
		}
		return sampled.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).collect(Collectors.toList());
	}

	/** Compatibility name retained for current callers. */
	public Iterable<Identifier> shuffledTemplatePool(RandomSource random, Identifier templatePoolId) {
		return this.getShuffledSequence(random, templatePoolId);
	}

	@Nullable
	public TwilightJigsawPiece initializeTemplateFromPool(
		Identifier templatePool,
		BlockPos parentJunctionPos,
		FrontAndTop parentOrientation,
		String selectName,
		RandomSource random,
		int generationDepth,
		StructureTemplateManager structureManager
	) {
		Optional<TemplatePoolEntry> selected = this.getRandomEntry(random, templatePool);
		if (selected.isEmpty()) {
			return null;
		}

		TemplatePoolEntry entry = selected.get();
		JigsawPlaceContext placeContext = JigsawPlaceContext.pickPlaceableJunction(
			parentJunctionPos,
			BlockPos.ZERO,
			parentOrientation,
			structureManager,
			entry.templateId,
			selectName,
			random
		);
		if (placeContext == null) {
			return null;
		}

		return TwilightJigsawPiece.defaultForTemplate(
			generationDepth,
			structureManager,
			entry.templateId,
			placeContext,
			entry.instance,
			entry.instance.chooseRandomProcessors(random)
		);
	}

	@Nullable
	public TwilightJigsawPiece initializeTemplateFromPool(
		Identifier templatePool,
		BlockPos parentJunctionPos,
		FrontAndTop parentOrientation,
		String selectName,
		Structure.GenerationContext generationContext,
		int generationDepth,
		boolean parentProjectsTerrain
	) {
		RandomSource random = generationContext.random();
		Optional<TemplatePoolEntry> selected = this.getRandomEntry(random, templatePool);
		if (selected.isEmpty()) {
			return null;
		}

		TemplatePoolEntry entry = selected.get();
		JigsawPlaceContext placeContext = JigsawPlaceContext.pickPlaceableJunction(
			parentJunctionPos,
			BlockPos.ZERO,
			parentOrientation,
			generationContext.structureTemplateManager(),
			entry.templateId,
			selectName,
			random
		);
		if (placeContext == null) {
			return null;
		}

		JigsawPlaceContext adjustedContext = entry.instance.adjustContextForTerrain(placeContext, generationContext, parentProjectsTerrain);
		return TwilightJigsawPiece.defaultForTemplate(
			generationDepth,
			generationContext.structureTemplateManager(),
			entry.templateId,
			adjustedContext,
			entry.instance,
			entry.instance.chooseRandomProcessors(random)
		);
	}

	private record TemplatePoolEntry(Identifier templateId, TemplatePoolInstance instance) {
	}
}
