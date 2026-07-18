package twilightforest.mixin;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.events.EntityEvents;
import twilightforest.events.TravellersGearEvents;
import twilightforest.events.ToolEvents;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {
	@Inject(
		method = "hitTargetOrDeflectSelf(Lnet/minecraft/world/phys/HitResult;)Lnet/minecraft/world/entity/projectile/ProjectileDeflection;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void twilightforest$performPerfectDodge(HitResult result, CallbackInfoReturnable<ProjectileDeflection> cir) {
		Projectile projectile = (Projectile) (Object) this;
		if (result instanceof EntityHitResult entityHitResult
			&& TravellersGearEvents.onProjectileHitEntity(projectile, entityHitResult)) {
			// A piercing arrow treats NONE as permission to continue its collision loop. Since a
			// dodged hit never records the target as pierced, use a non-applied deflection result
			// to stop that loop without changing the arrow's movement.
			cir.setReturnValue(projectile instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0
				? ProjectileDeflection.REVERSE
				: ProjectileDeflection.NONE);
		}
	}

	@Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
	private void twilightforest$onProjectileHit(EntityHitResult result, CallbackInfo ci) {
		Projectile projectile = (Projectile) (Object) this;
		if (EntityEvents.handleParryProjectile(projectile, result)) {
			ci.cancel();
			return;
		}
		ToolEvents.handleEnderBowHit(projectile, result);
	}

	@Inject(method = "onHitBlock", at = @At("TAIL"))
	private void twilightforest$onProjectileHitBlock(BlockHitResult result, CallbackInfo ci) {
		TravellersGearEvents.onProjectileHitBlock((Projectile) (Object) this, result);
	}
}
