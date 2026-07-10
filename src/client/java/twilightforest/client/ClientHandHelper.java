package twilightforest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import twilightforest.init.TFParticleType;
import twilightforest.item.LifedrainScepterItem;

public final class ClientHandHelper {
	private ClientHandHelper() {
	}

	public static float getPartialTick() {
		return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
	}

	public static void makeRedMagicTrail(Level level, LivingEntity source, Vec3 target) {
		Vec3 handPos = getPlayerHandPos(source, getPartialTick());
		double distance = handPos.distanceTo(target);

		for (double i = 0; i <= distance * 3; i++) {
			Vec3 particlePos = handPos.subtract(target).scale(i / (distance * 3));
			particlePos = handPos.subtract(particlePos);
			float r = 1.0F;
			float g = 0.5F;
			float b = 0.5F;
			level.addParticle(ColorParticleOption.create(TFParticleType.MAGIC_EFFECT, r, g, b), particlePos.x(), particlePos.y(), particlePos.z(), 0.0D, 0.0D, 0.0D);
		}
	}

	/**
	 * Based on the hand positioning used by the vanilla fishing hook renderer.
	 */
	public static Vec3 getPlayerHandPos(LivingEntity living, float partialTicks) {
		float armSwing = Mth.sin(Mth.sqrt(living.getAttackAnim(partialTicks)) * (float) Math.PI);
		int hand = living.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
		if (!(living.getMainHandItem().getItem() instanceof LifedrainScepterItem)) hand = -hand;

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.options.getCameraType().isFirstPerson() && living == minecraft.player) {
			Vec3 vec3 = minecraft.getEntityRenderDispatcher()
				.camera
				.getNearPlane((float) minecraft.options.fov().get())
				.getPointOnPlane((float) hand * 0.525F, -0.1F)
				.scale(960.0D / (double) minecraft.options.fov().get())
				.yRot(armSwing * 0.5F)
				.xRot(-armSwing * 0.7F);
			return living.getEyePosition(partialTicks).add(vec3);
		}

		float yRot = Mth.lerp(partialTicks, living.yBodyRotO, living.yBodyRot) * (float) (Math.PI / 180.0);
		double sin = Mth.sin(yRot);
		double cos = Mth.cos(yRot);
		float scale = living.getScale();
		double offset = (double) hand * 0.35 * (double) scale;
		double factor = 0.8 * (double) scale;
		float crouch = living.isCrouching() ? -0.1875F : 0.0F;
		return living.getEyePosition(partialTicks).add(-cos * offset - sin * factor, (double) crouch - 0.45 * (double) scale, -sin * offset + cos * factor);
	}
}
