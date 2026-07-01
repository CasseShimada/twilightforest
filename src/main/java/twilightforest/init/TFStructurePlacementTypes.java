package twilightforest.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.structures.placements.AvoidLandmarkGridPlacement;
import twilightforest.world.components.structures.placements.LandmarkGridPlacement;

import java.util.LinkedHashMap;
import java.util.Map;

public class TFStructurePlacementTypes {
	private static final Map<Identifier, StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPES = new LinkedHashMap<>();
	private static boolean registered;

	public static final StructurePlacementType<LandmarkGridPlacement> GRID_LANDMARK_PLACEMENT_TYPE = registerPlacer("landmark_grid", () -> LandmarkGridPlacement.CODEC);
	public static final StructurePlacementType<AvoidLandmarkGridPlacement> AVOID_GRID_LANDMARK_PLACEMENT_TYPE = registerPlacer("avoid_landmark_grid", () -> AvoidLandmarkGridPlacement.CODEC);

	private static <P extends StructurePlacement> StructurePlacementType<P> registerPlacer(String name, StructurePlacementType<P> type) {
		STRUCTURE_PLACEMENT_TYPES.put(TwilightForestMod.prefix(name), type);
		return type;
	}

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		STRUCTURE_PLACEMENT_TYPES.forEach((id, type) -> Registry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, id, type));
	}
}
