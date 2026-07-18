package twilightforest.mixin.client;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.renderer.TravellersArmorRenderer;

@Mixin(HumanoidMobRenderer.class)
public abstract class HumanoidMobRendererMixin {
	@Inject(method = "extractHumanoidRenderState", at = @At("TAIL"))
	private static void twilightforest$extractTravellersGearRenderState(LivingEntity entity, HumanoidRenderState state,
		float partialTick, ItemModelResolver itemModelResolver, CallbackInfo ci) {
		TravellersArmorRenderer.extractRenderState(entity, state, (FabricRenderState) state);
	}
}
