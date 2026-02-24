package twilightforest.mixin.client.accessor;

import net.minecraft.client.renderer.WeatherEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeatherEffectRenderer.class)
public interface WeatherEffectRendererAccessor {
	@Accessor("rainSoundTime")
	int twilightforest$getRainSoundTime();

	@Accessor("rainSoundTime")
	void twilightforest$setRainSoundTime(int value);
}
