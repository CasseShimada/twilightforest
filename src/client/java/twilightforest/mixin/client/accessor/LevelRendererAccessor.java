package twilightforest.mixin.client.accessor;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("skyRenderer")
	SkyRenderer twilightforest$getSkyRenderer();

	@Accessor("weatherEffectRenderer")
	WeatherEffectRenderer twilightforest$getWeatherEffectRenderer();
}
