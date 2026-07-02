package twilightforest.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.feature.templates.GraveyardFeature;
import twilightforest.world.components.processors.*;
import twilightforest.world.components.structures.courtyard.CourtyardTerraceTemplateProcessor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class for registering structure processor codecs.
 */
public class TFStructureProcessors {

	private static final Logger LOGGER = LoggerFactory.getLogger("twilightforest");
	private static final Map<Identifier, MapCodec<? extends StructureProcessor>> STRUCTURE_PROCESSORS = new LinkedHashMap<>();
	private static final Map<Identifier, Identifier> ALIASES = new LinkedHashMap<>();
	private static boolean registered;

	public static final MapCodec<CobbleVariants> COBBLE_VARIANTS = registerProcessor("cobble_variants", CobbleVariants.CODEC);
	public static final MapCodec<SmoothStoneVariants> SMOOTH_STONE_VARIANTS = registerProcessor("smooth_stone_variants", SmoothStoneVariants.CODEC);
	public static final MapCodec<StoneBricksVariants> STONE_BRICK_VARIANTS = registerProcessor("stone_brick_variants", StoneBricksVariants.CODEC);
	public static final MapCodec<InfestBlocksProcessor> INFEST_BLOCKS = registerProcessor("infest_blocks", InfestBlocksProcessor.CODEC);
	public static final MapCodec<NagastoneVariants> NAGASTONE_VARIANTS = registerProcessor("nagastone_variants", NagastoneVariants.CODEC);

	public static final MapCodec<StateTransfiguringProcessor> STATE_TRANSFIGURING = registerProcessor("state_transfiguring", StateTransfiguringProcessor.CODEC);

	public static final MapCodec<WoodPaletteSwizzle> PLANK_SWIZZLE = registerProcessor("wood_swizzle", WoodPaletteSwizzle.CODEC);
	public static final MapCodec<SmartGrassProcessor> SMART_GRASS = registerProcessor("smart_grass", SmartGrassProcessor.CODEC);
	public static final MapCodec<BoxCuttingProcessor> BOX_CUTTING_PROCESSOR = registerProcessor("box_cutting", BoxCuttingProcessor.CODEC);
	public static final MapCodec<TargetedRotProcessor> TARGETED_ROT = registerProcessor("targeted_rot", TargetedRotProcessor.CODEC);

	public static final MapCodec<GraveyardFeature.WebTemplateProcessor> WEB = registerProcessor("web", GraveyardFeature.WebTemplateProcessor.CODEC);
	public static final MapCodec<CourtyardTerraceTemplateProcessor> COURTYARD_TERRACE = registerProcessor("courtyard_terrace", CourtyardTerraceTemplateProcessor.CODEC);

	public static final MapCodec<SoftReplaceProcessor> SOFT_REPLACE = registerProcessor("soft_replace", SoftReplaceProcessor.CODEC);

	public static final MapCodec<SpawnerProcessor> SPAWNER_PROCESSOR = registerProcessor("spawner_processor", SpawnerProcessor.CODEC);
	public static final MapCodec<UpdateMarkingProcessor> UPDATE_MARKING_PROCESSOR = registerProcessor("update_marking", UpdateMarkingProcessor.CODEC);

	public static final MapCodec<VerticalDecayProcessor> VERTICAL_DECAY = registerProcessor("vertical_decay", VerticalDecayProcessor.CODEC);
	public static final MapCodec<WoodMultiPaletteSwizzle> PLANK_MULTISWIZZLE = registerProcessor("wood_multiswizzle", WoodMultiPaletteSwizzle.CODEC);

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		STRUCTURE_PROCESSORS.forEach((id, codec) -> Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, id, codec));
		applyAliases();
	}

	public static void addAlias(Identifier from, Identifier to) {
		if (registered) throw new IllegalStateException("Cannot add aliases after structure processors have been registered.");
		ALIASES.put(from, to);
	}

	private static <P extends StructureProcessor> MapCodec<P> registerProcessor(String name, MapCodec<P> processor) {
		STRUCTURE_PROCESSORS.put(TwilightForestMod.prefix(name), processor);
		return processor;
	}

	private static void applyAliases() {
		if (ALIASES.isEmpty()) {
			return;
		}
		if (!(BuiltInRegistries.STRUCTURE_PROCESSOR instanceof MappedRegistry<MapCodec<? extends StructureProcessor>> mapped)) {
			LOGGER.warn("Registry aliasing is not supported for {}", BuiltInRegistries.STRUCTURE_PROCESSOR.key().identifier());
			return;
		}

		try {
			Map<Identifier, Holder.Reference<MapCodec<? extends StructureProcessor>>> byLocation = resolveMap(mapped, "byLocation", Identifier.class, Holder.Reference.class);
			Map<ResourceKey<MapCodec<? extends StructureProcessor>>, Holder.Reference<MapCodec<? extends StructureProcessor>>> byKey = resolveMap(mapped, "byKey", ResourceKey.class, Holder.Reference.class);
			Map<ResourceKey<MapCodec<? extends StructureProcessor>>, RegistrationInfo> registrationInfos = resolveMap(mapped, "registrationInfos", ResourceKey.class, RegistrationInfo.class);
			if (byLocation == null || byKey == null || registrationInfos == null) {
				LOGGER.warn("Registry aliasing is not supported for {}", BuiltInRegistries.STRUCTURE_PROCESSOR.key().identifier());
				return;
			}

			for (Map.Entry<Identifier, Identifier> entry : ALIASES.entrySet()) {
				Identifier from = entry.getKey();
				if (byLocation.containsKey(from)) continue;
				Identifier to = entry.getValue();
				Holder.Reference<MapCodec<? extends StructureProcessor>> target = byLocation.get(to);
				if (target == null) {
					LOGGER.warn("Alias target {} missing in registry {}", to, BuiltInRegistries.STRUCTURE_PROCESSOR.key().identifier());
					continue;
				}
				ResourceKey<MapCodec<? extends StructureProcessor>> fromKey = ResourceKey.create(BuiltInRegistries.STRUCTURE_PROCESSOR.key(), from);
				byLocation.put(from, target);
				byKey.put(fromKey, target);
				RegistrationInfo info = registrationInfos.get(target.key());
				if (info != null) {
					registrationInfos.put(fromKey, info);
				}
			}
		} catch (ReflectiveOperationException e) {
			LOGGER.warn("Failed applying aliases for {}", BuiltInRegistries.STRUCTURE_PROCESSOR.key().identifier(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <K, V> Map<K, V> resolveMap(MappedRegistry<?> registry, String nameHint, Class<?> keyClass, Class<?> valueClass) throws ReflectiveOperationException {
		try {
			var field = MappedRegistry.class.getDeclaredField(nameHint);
			field.setAccessible(true);
			return (Map<K, V>) field.get(registry);
		} catch (NoSuchFieldException ignored) {
			for (var field : MappedRegistry.class.getDeclaredFields()) {
				if (!Map.class.isAssignableFrom(field.getType())) {
					continue;
				}
				field.setAccessible(true);
				Object value = field.get(registry);
				if (!(value instanceof Map<?, ?> map) || map.isEmpty()) {
					continue;
				}
				var entry = map.entrySet().iterator().next();
				if (keyClass.isInstance(entry.getKey()) && valueClass.isInstance(entry.getValue())) {
					return (Map<K, V>) map;
				}
			}
			return null;
		}
	}
}
