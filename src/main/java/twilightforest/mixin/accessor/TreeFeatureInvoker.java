package twilightforest.mixin.accessor;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;
import net.minecraft.core.BlockPos;

@Mixin(TreeFeature.class)
public interface TreeFeatureInvoker {
	@Invoker("updateLeaves")
	static DiscreteVoxelShape twilightforest$updateLeaves(LevelAccessor level, BoundingBox box, Set<BlockPos> logPositions, Set<BlockPos> leaves, Set<BlockPos> decorations) {
		throw new UnsupportedOperationException("Mixin");
	}
}
