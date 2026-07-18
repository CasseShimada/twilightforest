package twilightforest.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.init.TFDataAttachments;

@Mixin(Mob.class)
public abstract class MobMixin {
	@Inject(method = "onLeashRemoved", at = @At("TAIL"))
	private void twilightforest$clearLeashPathfinderOverride(CallbackInfo ci) {
		Mob mob = (Mob) (Object) this;
		if (TFDataAttachments.has(mob, TFDataAttachments.LEASH_PATHFINDER_OVERRIDE)) {
			TFDataAttachments.remove(mob, TFDataAttachments.LEASH_PATHFINDER_OVERRIDE);
		}
	}
}
