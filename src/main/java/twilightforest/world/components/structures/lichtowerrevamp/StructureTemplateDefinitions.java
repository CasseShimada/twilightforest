package twilightforest.world.components.structures.lichtowerrevamp;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;

import java.util.*;
import java.util.stream.Collectors;

public class StructureTemplateDefinitions extends SimpleJsonResourceReloadListener<StructureTemplateDefinition> implements IdentifiableResourceReloadListener {
	public static final StructureTemplateDefinitions INSTANCE = new StructureTemplateDefinitions(); // TODO Autowired

	private final Map<Identifier, WeightedList<Identifier>> templatePools = new HashMap<>();

	public static final String DIRECTORY = "twilight/template_definition";

	StructureTemplateDefinitions() {
		super(StructureTemplateDefinition.CODEC, FileToIdConverter.json(DIRECTORY));
	}

	@Override
	public Identifier getFabricId() {
		return TwilightForestMod.prefix("structure_templates");
	}

	@Override
	protected void apply(Map<Identifier, StructureTemplateDefinition> map, ResourceManager manager, ProfilerFiller profiler) {
		this.templatePools.clear();

		final Map<Identifier, WeightedList.Builder<Identifier>> rawTemplatePools = new HashMap<>();

		for(Map.Entry<Identifier, StructureTemplateDefinition> mapEntry : map.entrySet()) {

			// Ensures that the order of elements stays deterministic between sessions, as Map/Set sorting are undefined behavior
			List<Map.Entry<Identifier, Integer>> sorted = mapEntry.getValue().poolWeights().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();

			for(Map.Entry<Identifier, Integer> poolToRegisterWeight : sorted) {
				Identifier templatePoolId = poolToRegisterWeight.getKey();
				Integer templateWeight = poolToRegisterWeight.getValue();

				WeightedList.Builder<Identifier> pool = rawTemplatePools.computeIfAbsent(templatePoolId, k -> WeightedList.builder());

				pool.add(mapEntry.getKey(), templateWeight);
			}
		}

		for (Map.Entry<Identifier, WeightedList.Builder<Identifier>> rawTemplatePool : rawTemplatePools.entrySet()) {
			this.templatePools.put(rawTemplatePool.getKey(), rawTemplatePool.getValue().build());
		}

		rawTemplatePools.clear();
	}

	@Nullable
	public Identifier rollTemplatePool(RandomSource random, Identifier templatePoolId) {
		WeightedList<Identifier> templatePool = this.templatePools.get(templatePoolId);
		return templatePool == null ? null : templatePool.getRandom(random).orElse(null);
	}

	// https://en.wikipedia.org/wiki/Reservoir_sampling
	public Iterable<Identifier> shuffledTemplatePool(RandomSource random, Identifier templatePoolId) {
		WeightedList<Identifier> templatePool = this.templatePools.get(templatePoolId);

		if (templatePool == null)
			return List.of();

		Map<Identifier, Double> reservoirSampled = new HashMap<>();
		for (Weighted<Identifier> entry : templatePool.unwrap()) {
			double rand = random.nextDouble();
			reservoirSampled.put(entry.value(), -Math.log(rand) / entry.weight());
		}

		return reservoirSampled.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).collect(Collectors.toList());
	}
}
