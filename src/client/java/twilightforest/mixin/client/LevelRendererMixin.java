package twilightforest.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.renderer.TFSkyRenderer;
import twilightforest.init.TFDimension;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Inject(
		method = "lambda$addSkyPass$0",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFog(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private static void twilightforest$renderTwilightSky(GpuBufferSlice fog, SkyRenderState state, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || !TFDimension.isTwilightWorldOnClient(mc.level)) {
			return;
		}

		Camera camera = mc.gameRenderer.mainCamera();
		float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		TFSkyRenderer.renderSky(mc.level, partialTicks, camera, () -> RenderSystem.setShaderFog(fog));
		ci.cancel();
	}
}
