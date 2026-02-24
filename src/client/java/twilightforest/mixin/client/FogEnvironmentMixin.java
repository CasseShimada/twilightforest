package twilightforest.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.event.FogHandler;

@Mixin(AtmosphericFogEnvironment.class)
public class FogEnvironmentMixin {
	@Inject(method = "setupFog", at = @At("TAIL"))
	private void twilightforest$applyTwilightFog(FogData fogData, Camera camera, ClientLevel level, float viewDistance, DeltaTracker tickCounter, CallbackInfo ci) {
		FogHandler.applyTwilightFog(fogData, level, camera.entity());
	}
}
