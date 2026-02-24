package twilightforest.world.components.structures;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

/**
 * Fabric-compatible placeholder for the previous PieceBeardifierModifier.
 * <p>
 * The methods are used by existing structure pieces but are not consumed by Fabric itself.
 */
public interface PieceBeardifierModifier {
	BoundingBox getBeardifierBox();

	TerrainAdjustment getTerrainAdjustment();

	int getGroundLevelDelta();
}
