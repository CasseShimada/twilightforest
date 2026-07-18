package twilightforest.mixin;

import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.block.WroughtIronFenceBlock;

@Mixin(LeashFenceKnotEntity.class)
public abstract class LeashFenceKnotEntityMixin {
	@Inject(method = "survives", at = @At("RETURN"), cancellable = true)
	private void twilightforest$survivesOnWroughtIronPost(CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			return;
		}
		LeashFenceKnotEntity knot = (LeashFenceKnotEntity) (Object) this;
		cir.setReturnValue(WroughtIronFenceBlock.supportsLeashKnot(knot.level().getBlockState(knot.getPos())));
	}
}
