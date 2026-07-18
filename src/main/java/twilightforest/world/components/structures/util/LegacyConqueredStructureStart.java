package twilightforest.world.components.structures.util;

import org.jetbrains.annotations.Nullable;

/** Runtime bridge for the Forge-era {@code TFStructureStart.conquered} chunk field. */
public interface LegacyConqueredStructureStart {
	@Nullable
	Boolean twilightforest$getLegacyConquered();

	void twilightforest$setLegacyConquered(@Nullable Boolean conquered);
}
