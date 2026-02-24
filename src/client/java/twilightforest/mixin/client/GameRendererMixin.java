package twilightforest.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.client.event.ClientEvents;
import twilightforest.config.TFConfig;
import twilightforest.item.EnderBowItem;
import twilightforest.item.IceBowItem;
import twilightforest.item.SeekerBowItem;
import twilightforest.item.TripleBowItem;
import twilightforest.mixin.client.accessor.CameraAccessor;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void twilightforest$adjustBowFov(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Float> cir) {
		Entity entity = camera.entity();
		if (!(entity instanceof Player player) || !player.isUsingItem()) {
			return;
		}
		if (!(player.getUseItem().getItem() instanceof TripleBowItem
			|| player.getUseItem().getItem() instanceof EnderBowItem
			|| player.getUseItem().getItem() instanceof IceBowItem
			|| player.getUseItem().getItem() instanceof SeekerBowItem)) {
			return;
		}

		float f = player.getTicksUsingItem() / 20.0F;
		f = f > 1.0F ? 1.0F : f * f;
		float current = cir.getReturnValueF();
		float scaled = (float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0F, (current * (1.0F - f * 0.15F)));
		cir.setReturnValue(scaled);
	}

	@Inject(method = "updateCamera", at = @At("TAIL"))
	private void twilightforest$shakeCamera(Camera camera, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (!TFConfig.firstPersonEffects || mc.isPaused() || mc.player == null) {
			ClientEvents.consumeShakeIntensity();
			return;
		}

		float intensity = ClientEvents.consumeShakeIntensity();
		if (intensity <= 0.0F) {
			return;
		}

		float yaw = camera.yRot();
		float pitch = camera.xRot();
		float yawOffset = (mc.player.getRandom().nextFloat() * 2F - 1F) * intensity;
		float pitchOffset = (mc.player.getRandom().nextFloat() * 2F - 1F) * intensity;
		((CameraAccessor) camera).twilightforest$setRotation(yaw + yawOffset, pitch + pitchOffset);
	}
}
