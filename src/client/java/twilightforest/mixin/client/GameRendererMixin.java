package twilightforest.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.client.event.ClientEvents;
import twilightforest.client.event.TravellersClientEvents;
import twilightforest.config.TFConfig;
import twilightforest.item.EnderBowItem;
import twilightforest.item.IceBowItem;
import twilightforest.item.SeekerBowItem;
import twilightforest.item.TripleBowItem;
import twilightforest.mixin.client.accessor.CameraAccessor;

@Mixin(Camera.class)
public class GameRendererMixin {
	@Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
	private void twilightforest$adjustFov(float partialTick, CallbackInfoReturnable<Float> cir) {
		Camera camera = (Camera) (Object) this;
		Entity entity = camera.entity();
		if (!(entity instanceof Player player)) {
			return;
		}

		float current = cir.getReturnValueF();
		if (player.isUsingItem() && (player.getUseItem().getItem() instanceof TripleBowItem
			|| player.getUseItem().getItem() instanceof EnderBowItem
			|| player.getUseItem().getItem() instanceof IceBowItem
			|| player.getUseItem().getItem() instanceof SeekerBowItem)) {
			float useProgress = player.getTicksUsingItem() / 20.0F;
			useProgress = useProgress > 1.0F ? 1.0F : useProgress * useProgress;
			current = (float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(),
				1.0F, current * (1.0F - useProgress * 0.15F));
		}
		cir.setReturnValue(TravellersClientEvents.modifyFov(current, player));
	}

	@Inject(method = "update", at = @At("TAIL"))
	private void twilightforest$shakeCamera(DeltaTracker deltaTracker, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!TFConfig.firstPersonEffects || minecraft.isPaused() || minecraft.player == null) {
			ClientEvents.consumeShakeIntensity();
			return;
		}

		float intensity = ClientEvents.consumeShakeIntensity();
		if (intensity <= 0.0F) {
			return;
		}

		Camera camera = (Camera) (Object) this;
		float yawOffset = (minecraft.player.getRandom().nextFloat() * 2.0F - 1.0F) * intensity;
		float pitchOffset = (minecraft.player.getRandom().nextFloat() * 2.0F - 1.0F) * intensity;
		((CameraAccessor) camera).twilightforest$setRotation(camera.yRot() + yawOffset, camera.xRot() + pitchOffset);
	}
}
