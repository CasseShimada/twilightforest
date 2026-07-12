package twilightforest.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.feature.trees.treeplacers.*;
import twilightforest.world.components.placements.AvoidLandmarkModifier;
import twilightforest.world.components.placements.ChunkBlanketingModifier;
import twilightforest.world.components.placements.ChunkCenterModifier;

public final class TFFeatureModifiers {
	public static final TrunkPlacerType<BranchingTrunkPlacer> TRUNK_BRANCHING = trunkPlacer("branching_trunk_placer", new TrunkPlacerType<>(BranchingTrunkPlacer.CODEC));
	public static final TrunkPlacerType<TrunkRiser> TRUNK_RISER = trunkPlacer("trunk_mover_upper", new TrunkPlacerType<>(TrunkRiser.CODEC));

	public static final FoliagePlacerType<LeafSpheroidFoliagePlacer> FOLIAGE_SPHEROID = foliagePlacer("spheroid_foliage_placer", new FoliagePlacerType<>(LeafSpheroidFoliagePlacer.CODEC));

	public static final TreeDecoratorType<TreeCorePlacer> CORE_PLACER = treeDecorator("core_placer", new TreeDecoratorType<>(TreeCorePlacer.CODEC));
	public static final TreeDecoratorType<TrunkSideDecorator> TRUNKSIDE_DECORATOR = treeDecorator("trunkside_decorator", new TreeDecoratorType<>(TrunkSideDecorator.CODEC));
	public static final TreeDecoratorType<TreeRootsDecorator> TREE_ROOTS = treeDecorator("tree_roots", new TreeDecoratorType<>(TreeRootsDecorator.CODEC));
	public static final TreeDecoratorType<DangleFromTreeDecorator> DANGLING_DECORATOR = treeDecorator("dangle_from_tree_decorator", new TreeDecoratorType<>(DangleFromTreeDecorator.CODEC));

	public static final PlacementModifierType<AvoidLandmarkModifier> NO_STRUCTURE = placementModifier("no_structure", () -> AvoidLandmarkModifier.CODEC);
	public static final PlacementModifierType<ChunkCenterModifier> CHUNK_CENTERER = placementModifier("chunk_centerer", () -> ChunkCenterModifier.CODEC);
	public static final PlacementModifierType<ChunkBlanketingModifier> CHUNK_BLANKETING = placementModifier("chunk_blanketing", () -> ChunkBlanketingModifier.CODEC);

	private static <P extends TrunkPlacer> TrunkPlacerType<P> trunkPlacer(String name, TrunkPlacerType<P> type) {
		return Registry.register(BuiltInRegistries.TRUNK_PLACER_TYPE, TwilightForestMod.prefix(name), type);
	}

	private static <P extends FoliagePlacer> FoliagePlacerType<P> foliagePlacer(String name, FoliagePlacerType<P> type) {
		return Registry.register(BuiltInRegistries.FOLIAGE_PLACER_TYPE, TwilightForestMod.prefix(name), type);
	}

	private static <P extends TreeDecorator> TreeDecoratorType<P> treeDecorator(String name, TreeDecoratorType<P> type) {
		return Registry.register(BuiltInRegistries.TREE_DECORATOR_TYPE, TwilightForestMod.prefix(name), type);
	}

	private static <P extends PlacementModifier> PlacementModifierType<P> placementModifier(String name, PlacementModifierType<P> type) {
		return Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, TwilightForestMod.prefix(name), type);
	}

	public static void init() {
	}
}
