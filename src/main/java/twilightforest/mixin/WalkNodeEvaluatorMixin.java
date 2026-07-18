package twilightforest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.tags.TFBlockTags;
import twilightforest.block.OreBerryBushBlock;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMixin {
	@Inject(method = "getPathTypeFromState", at = @At("HEAD"), cancellable = true)
	private static void twilightforest$useFencePathTypeForBanisters(BlockGetter level, BlockPos pos, CallbackInfoReturnable<PathType> cir) {
		if (level.getBlockState(pos).getBlock() instanceof OreBerryBushBlock) {
			cir.setReturnValue(PathType.DAMAGING);
		} else if (level.getBlockState(pos).is(TFBlockTags.BANISTERS)) {
			cir.setReturnValue(PathType.FENCE);
		}
	}
}
