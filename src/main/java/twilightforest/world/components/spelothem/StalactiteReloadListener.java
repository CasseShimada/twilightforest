package twilightforest.world.components.spelothem;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import twilightforest.TwilightForestMod;

import java.util.*;

public final class StalactiteReloadListener extends SimpleJsonResourceReloadListener<Either<Stalactite, SpeleothemVarietyConfig>> implements IdentifiableResourceReloadListener {
	public final static StalactiteReloadListener INSTANCE = new StalactiteReloadListener(); // TODO Autowired

	public static final String STALACTITE_DIRECTORY = "twilight/stalactites";

	public static final Map<String, SpeleothemVarietyConfig> HILL_CONFIGS = new HashMap<>();
	public static final Map<String, List<Stalactite>> STALACTITES_PER_HILL = new HashMap<>();
	public static final Map<String, List<Stalactite>> ORE_STALACTITES_PER_HILL = new HashMap<>();
	public static final Map<String, List<Stalactite>> STALAGMITES_PER_HILL = new HashMap<>();

	public StalactiteReloadListener() {
		super(Codec.either(Stalactite.CODEC, SpeleothemVarietyConfig.CODEC), FileToIdConverter.json(STALACTITE_DIRECTORY));
	}

	@Override
	public Identifier getFabricId() {
		return TwilightForestMod.prefix("stalactites");
	}

	@Override
	protected void apply(Map<Identifier, Either<Stalactite, SpeleothemVarietyConfig>> resources, ResourceManager manager, ProfilerFiller profiler) {
		HILL_CONFIGS.clear();
		STALACTITES_PER_HILL.clear();
		ORE_STALACTITES_PER_HILL.clear();
		STALAGMITES_PER_HILL.clear();

		Map<Identifier, Stalactite> entries = new HashMap<>();
		List<Map.Entry<Identifier, SpeleothemVarietyConfig>> configs = new ArrayList<>();
		resources.forEach((id, resource) -> resource.ifLeft(entry -> entries.put(id, entry)).ifRight(config -> configs.add(Map.entry(id, config))));

		configs.sort(Comparator
			.<Map.Entry<Identifier, SpeleothemVarietyConfig>, Boolean>comparing(entry -> !TwilightForestMod.ID.equals(entry.getKey().getNamespace()))
			.thenComparing(Map.Entry::getKey));

		configs.forEach(entry -> this.applyConfig(entry.getKey(), entry.getValue(), entries));
	}

	private void applyConfig(Identifier location, SpeleothemVarietyConfig config, Map<Identifier, Stalactite> entries) {
		if (!HILL_CONFIGS.containsKey(config.type()) || config.replace()) {
			HILL_CONFIGS.put(config.type(), config);
			if (config.replace()) {
				TwilightForestMod.LOGGER.info("Stalactite Config {} wiped by {}", config.type(), location.getNamespace());
			}
		}

		this.populateList(config, config.baseStalactites(), STALACTITES_PER_HILL, entries);
		this.populateList(config, config.oreStalactites(), ORE_STALACTITES_PER_HILL, entries);
		this.populateList(config, config.stalagmites(), STALAGMITES_PER_HILL, entries);
	}

	private void populateList(SpeleothemVarietyConfig config, List<Identifier> rawEntries, Map<String, List<Stalactite>> stalactiteDict, Map<Identifier, Stalactite> entries) {
		List<Stalactite> stalactitesForType = stalactiteDict.computeIfAbsent(config.type(), k -> new ArrayList<>());

		if (config.replace()) stalactitesForType.clear();

		for (Identifier id : rawEntries) {
			Stalactite stalactite = entries.get(id);
			if (stalactite != null) {
				stalactitesForType.add(stalactite);
				TwilightForestMod.LOGGER.debug("Loaded Stalactite {} for config {}", id, config.type());
			} else {
				TwilightForestMod.LOGGER.error("Could not find stalactite entry for {}", id);
			}
		}
	}
}
