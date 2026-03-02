package twilightforest.mixin;

import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.ASMHooks;

@Mixin(PathfinderMob.class)
public abstract class PathfinderMobMixin {
	@Inject(method = "shouldStayCloseToLeashHolder", at = @At("RETURN"), cancellable = true)
	private void twilightforest$overrideStayCloseToHolder(CallbackInfoReturnable<Boolean> cir) {
		PathfinderMob mob = (PathfinderMob) (Object) this;
		cir.setReturnValue(ASMHooks.overrideStayCloseToHolder(cir.getReturnValue(), mob));
	}
}
