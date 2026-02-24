package twilightforest.util.density;

import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.Nullable;

public interface CustomDensityBeardifier {
	void twilightforest$setCustomDensities(@Nullable ObjectListIterator<DensityFunction> customDensities);

	@Nullable
	ObjectListIterator<DensityFunction> twilightforest$getCustomDensities();
}
