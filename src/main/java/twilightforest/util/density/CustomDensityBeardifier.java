package twilightforest.util.density;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;
import twilightforest.world.components.structures.CustomDensitySource;

public interface CustomDensityBeardifier {
	void twilightforest$setCustomDensities(@Nullable ObjectListIterator<DensityFunction> customDensities);

	static ObjectListIterator<DensityFunction> gatherCustomTerrain(StructureManager structureManager, ChunkPos chunkPos) {
		ObjectArrayList<DensityFunction> customDensities = new ObjectArrayList<>(10);
		for (StructureStart structureStart : structureManager.startsForStructure(chunkPos, structure -> structure instanceof CustomDensitySource)) {
			if (structureStart.getStructure() instanceof CustomDensitySource source) {
				customDensities.add(source.getStructureTerraformer(chunkPos, structureStart));
			}
		}
		return customDensities.iterator();
	}

	static double applyCustomDensity(double base, DensityFunction.FunctionContext context, ObjectListIterator<DensityFunction> customDensities) {
		double density = base;
		while (customDensities.hasNext()) {
			density += customDensities.next().compute(context);
		}
		customDensities.back(Integer.MAX_VALUE);
		return density;
	}
}
