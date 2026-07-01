package twilightforest.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.client.renderer.TFWeatherRenderer;
import twilightforest.init.TFDimension;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
	@Redirect(
		method = "extractRenderState",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
	)
	private float twilightforest$useEffectiveRainLevel(ClientLevel level, float partialTicks) {
		if (TFDimension.isTwilightWorldOnClient(level)) {
			return TFWeatherRenderer.getEffectiveRainLevel(level, partialTicks);
		}
		return level.getRainLevel(partialTicks);
	}
}
