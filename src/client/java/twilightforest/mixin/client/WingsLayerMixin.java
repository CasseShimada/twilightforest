package twilightforest.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.ASMHooks;

@Mixin(WingsLayer.class)
public abstract class WingsLayerMixin {
	@Inject(
		method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void twilightforest$hideShroudedWings(PoseStack stack, SubmitNodeCollector nodeCollector, int light, HumanoidRenderState state, float yRot, float xRot, CallbackInfo ci) {
		if (!ASMHooks.cancelArmorRendering(true, state.chestEquipment)) {
			ci.cancel();
		}
	}
}
