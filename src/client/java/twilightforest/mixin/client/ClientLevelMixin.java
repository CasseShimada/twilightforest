package twilightforest.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ParticleStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.renderer.TFWeatherRenderer;
import twilightforest.init.TFDimension;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	@Inject(method = "tickWeatherEffects", at = @At("HEAD"), cancellable = true)
	private void twilightforest$tickProgressionRain(CallbackInfo ci) {
		ClientLevel level = (ClientLevel) (Object) this;
		if (!TFDimension.isTwilightWorldOnClient(level)) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		ParticleStatus particleStatus = minecraft.options.particles().get();
		int radius = minecraft.options.weatherRadius().get();
		BlockPos cameraPos = BlockPos.containing(minecraft.gameRenderer.mainCamera().position());
		if (TFWeatherRenderer.tickRain(level, cameraPos, particleStatus, radius)) {
			ci.cancel();
		}
	}
}
