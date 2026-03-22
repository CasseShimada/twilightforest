package twilightforest.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.ASMHooks;
import twilightforest.events.HostileMountEvents;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "stopRiding", at = @At("HEAD"), cancellable = true)
	private void twilightforest$preventHostileDismount(CallbackInfo ci) {
		Entity self = (Entity) (Object) this;
		if (!(self instanceof Player player)) {
			return;
		}
		Entity vehicle = player.getVehicle();
		if (HostileMountEvents.shouldPreventDismount(player, vehicle)) {
			ci.cancel();
		}
	}

	@Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"), cancellable = true)
	private void twilightforest$preventTeleport(TeleportTransition transition, CallbackInfoReturnable<Entity> cir) {
		Entity self = (Entity) (Object) this;
		if (HostileMountEvents.shouldCancelTeleport(self)) {
			cir.setReturnValue(null);
		}
	}

	@Inject(method = "teleportTo(DDD)V", at = @At("HEAD"), cancellable = true)
	private void twilightforest$preventTeleportTo(double x, double y, double z, CallbackInfo ci) {
		Entity self = (Entity) (Object) this;
		if (HostileMountEvents.shouldCancelTeleport(self)) {
			ci.cancel();
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void twilightforest$hostileMountShiftKey(CallbackInfo ci) {
		HostileMountEvents.enforcePassengerShiftKey((Entity) (Object) this);
	}

	@Inject(method = "isInWaterOrRain", at = @At("RETURN"), cancellable = true)
	private void twilightforest$allowUrGhastTearsToCountAsRain(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && ASMHooks.isEntityInUrGhastTears((Entity) (Object) this)) {
			cir.setReturnValue(true);
		}
	}
}
