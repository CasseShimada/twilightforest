package twilightforest.mixin;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.events.EntityEvents;
import twilightforest.events.ToolEvents;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {
	@Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
	private void twilightforest$onProjectileHit(EntityHitResult result, CallbackInfo ci) {
		Projectile projectile = (Projectile) (Object) this;
		if (EntityEvents.handleParryProjectile(projectile, result)) {
			ci.cancel();
			return;
		}
		ToolEvents.handleEnderBowHit(projectile, result);
	}
}
