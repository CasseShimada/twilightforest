package twilightforest.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.block.TFBushBlock;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
	@Inject(method = "getPistonPushReaction", at = @At("HEAD"), cancellable = true)
	private void twilightforest$youngBushesBreakWhenPushed(CallbackInfoReturnable<PushReaction> cir) {
		BlockState state = (BlockState) (Object) this;
		if (state.getBlock() instanceof TFBushBlock && state.getValue(TFBushBlock.AGE) < 2) {
			cir.setReturnValue(PushReaction.DESTROY);
		}
	}
}
