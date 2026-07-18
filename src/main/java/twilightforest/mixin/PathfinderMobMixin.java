package twilightforest.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.block.WroughtIronFenceBlock;
import twilightforest.init.TFDataAttachments;

@Mixin(PathfinderMob.class)
public abstract class PathfinderMobMixin {
	@Inject(method = "whenLeashedTo", at = @At("HEAD"))
	private void twilightforest$clearInvalidLeashOverride(Entity holder, CallbackInfo ci) {
		PathfinderMob mob = (PathfinderMob) (Object) this;
		if (TFDataAttachments.has(mob, TFDataAttachments.LEASH_PATHFINDER_OVERRIDE)
			&& !twilightforest$isSpecialLeashHolder(holder)) {
			TFDataAttachments.remove(mob, TFDataAttachments.LEASH_PATHFINDER_OVERRIDE);
		}
	}

	@Inject(method = "shouldStayCloseToLeashHolder", at = @At("RETURN"), cancellable = true)
	private void twilightforest$overrideStayCloseToHolder(CallbackInfoReturnable<Boolean> cir) {
		PathfinderMob mob = (PathfinderMob) (Object) this;
		if (!TFDataAttachments.has(mob, TFDataAttachments.LEASH_PATHFINDER_OVERRIDE)) {
			return;
		}

		if (twilightforest$isSpecialLeashHolder(mob.getLeashHolder())) {
			cir.setReturnValue(false);
		} else {
			TFDataAttachments.remove(mob, TFDataAttachments.LEASH_PATHFINDER_OVERRIDE);
		}
	}

	private static boolean twilightforest$isSpecialLeashHolder(Entity holder) {
		return holder instanceof LeashFenceKnotEntity knot
			&& WroughtIronFenceBlock.supportsLeashKnot(knot.level().getBlockState(knot.getPos()));
	}
}
