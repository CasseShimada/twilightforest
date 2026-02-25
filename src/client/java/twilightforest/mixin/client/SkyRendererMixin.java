package twilightforest.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.renderer.TFSkyRenderer;
import twilightforest.init.TFDimension;

	@Mixin(SkyRenderer.class)
	public class SkyRendererMixin {
		@Inject(method = "renderSunMoonAndStars", at = @At("HEAD"), cancellable = true)
		private void twilightforest$renderTwilightStars(PoseStack poseStack, float sunAngle, float moonAngle, float starAngle, MoonPhase moonPhase, float rainBrightness, float starBrightness, CallbackInfo ci) {
			ClientLevel level = Minecraft.getInstance().level;
			if (level == null || !TFDimension.isTwilightWorldOnClient(level)) {
				return;
		}

		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
		TFSkyRenderer.renderTwilightStars(poseStack);
		poseStack.popPose();
		ci.cancel();
	}

	@Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true)
	private void twilightforest$disableTwilightSunrise(PoseStack poseStack, float sunAngle, int color, CallbackInfo ci) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null && TFDimension.isTwilightWorldOnClient(level)) {
			ci.cancel();
		}
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void twilightforest$clearSunriseColor(ClientLevel level, float partialTicks, Camera camera, SkyRenderState state, CallbackInfo ci) {
		if (TFDimension.isTwilightWorldOnClient(level)) {
			state.sunriseAndSunsetColor = 0;
		}
	}
}
