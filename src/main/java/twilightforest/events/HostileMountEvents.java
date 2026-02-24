package twilightforest.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import twilightforest.entity.IHostileMount;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFDataAttachments;

public class HostileMountEvents {

	public static volatile boolean allowDismount = false;

	public static boolean handleIncomingDamage(LivingEntity living, DamageSource damageSource, float amount) {
		// lets not make the player take suffocation damage if riding something
		if (living instanceof Player && isRidingUnfriendly(living) && damageSource.is(DamageTypes.IN_WALL)) {
			return false;
		}

		if (living.level() instanceof ServerLevel level && damageSource.is(DamageTypes.FALL) && TFDataAttachments.get(living, TFDataAttachments.YETI_THROWING).getThrown()) {
			living.hurtServer(level, level.damageSources().source(TFDamageTypes.YEETED, TFDataAttachments.get(living, TFDataAttachments.YETI_THROWING).getThrower()), amount);
			return false;
		}
		return true;
	}

	public static boolean shouldCancelTeleport(Entity entity) {
		return entity instanceof LivingEntity living && isRidingUnfriendly(living);
	}

	public static void hostileDismount(Entity rider) {
		HostileMountEvents.allowDismount = true;
		rider.stopRiding();
		HostileMountEvents.allowDismount = false;
	}

	public static boolean shouldPreventDismount(Player player, Entity vehicle) {
		if (player == null || vehicle == null) {
			return false;
		}
		if (!vehicle.isAlive()) {
			return false;
		}
		if (!player.isAlive() || player.getAbilities().invulnerable) {
			return false;
		}
		if (!isRidingUnfriendly(player) || allowDismount) {
			return false;
		}
		return true;
	}

	public static void enforcePassengerShiftKey(Entity entity) {
		if (entity instanceof IHostileMount) {
			entity.getPassengers().forEach(passenger -> passenger.setShiftKeyDown(false));
		}
	}

	public static boolean isRidingUnfriendly(LivingEntity entity) {
		return entity.isPassenger() && entity.getVehicle() instanceof IHostileMount;
	}
}
