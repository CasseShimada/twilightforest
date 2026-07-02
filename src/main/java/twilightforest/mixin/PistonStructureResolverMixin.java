package twilightforest.mixin;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.init.TFBlocks;

@Mixin(PistonStructureResolver.class)
public class PistonStructureResolverMixin {
	@Inject(method = "isSticky", at = @At("HEAD"), cancellable = true)
	private static void twilightforest$isSticky(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (state.is(TFBlocks.MAZE_SLIME_BLOCK)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "canStickToEachOther", at = @At("HEAD"), cancellable = true)
	private static void twilightforest$canStickToEachOther(BlockState first, BlockState second, CallbackInfoReturnable<Boolean> cir) {
		boolean firstMaze = first.is(TFBlocks.MAZE_SLIME_BLOCK);
		boolean secondMaze = second.is(TFBlocks.MAZE_SLIME_BLOCK);
		if (!(firstMaze || secondMaze)) {
			return;
		}

		if (firstMaze && secondMaze) {
			cir.setReturnValue(true);
			return;
		}

		BlockState other = firstMaze ? second : first;
		if (other.is(Blocks.SLIME_BLOCK) || other.is(Blocks.HONEY_BLOCK)) {
			cir.setReturnValue(false);
			return;
		}

		cir.setReturnValue(true);
	}
}
