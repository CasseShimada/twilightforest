package twilightforest.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.feature.templates.GraveyardFeature;
import twilightforest.world.components.processors.*;
import twilightforest.world.components.structures.courtyard.CourtyardTerraceTemplateProcessor;
import twilightforest.util.registry.RegistryAliasUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class for registering structure processor codecs.
 */
public class TFStructureProcessors {

	private static final Map<Identifier, Identifier> ALIASES = new LinkedHashMap<>();
	private static boolean aliasesApplied;

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

	public static void init() {
		if (aliasesApplied) {
			return;
		}

		aliasesApplied = true;
		applyAliases();
	}

	public static void addAlias(Identifier from, Identifier to) {
		if (aliasesApplied) throw new IllegalStateException("Cannot add aliases after structure processor aliases have been applied.");
		ALIASES.put(from, to);
	}

	private static <P extends StructureProcessor> MapCodec<P> registerProcessor(String name, MapCodec<P> processor) {
		return Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, TwilightForestMod.prefix(name), processor);
	}

	private static void applyAliases() {
		RegistryAliasUtil.applyAliases(BuiltInRegistries.STRUCTURE_PROCESSOR, ALIASES);
	}
}
