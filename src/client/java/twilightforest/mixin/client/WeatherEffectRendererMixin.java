package twilightforest.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.renderer.TFWeatherRenderer;
import twilightforest.init.TFDimension;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void twilightforest$renderProgressionWeather(Vec3 camera, WeatherRenderState state, CallbackInfo ci) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null || !TFDimension.isTwilightWorldOnClient(level)) {
			return;
		}

		int ticks = (int) level.getGameTime();
		float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
		TFWeatherRenderer.renderSnowAndRain(level, ticks, partialTicks, camera);
	}

	@Inject(method = "tickRainParticles", at = @At("HEAD"), cancellable = true)
	private void twilightforest$tickProgressionRain(ClientLevel level, Camera camera, int ticks, ParticleStatus particleStatus, int radius, CallbackInfo ci) {
		if (!TFDimension.isTwilightWorldOnClient(level)) {
			return;
		}

		if (TFWeatherRenderer.tickRain(level, ticks, camera.blockPosition(), particleStatus, radius)) {
			ci.cancel();
		}
	}

	@Redirect(
		method = "extractRenderState",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getRainLevel(F)F")
	)
	private float twilightforest$useEffectiveRainLevel(Level level, float partialTicks) {
		if (level instanceof ClientLevel clientLevel && TFDimension.isTwilightWorldOnClient(clientLevel)) {
			return TFWeatherRenderer.getEffectiveRainLevel(clientLevel, partialTicks);
		}
		return level.getRainLevel(partialTicks);
	}
}
