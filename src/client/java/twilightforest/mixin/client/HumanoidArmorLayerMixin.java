package twilightforest.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.ASMHooks;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {
	@Inject(method = "shouldRender(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Z", at = @At("RETURN"), cancellable = true)
	private static void twilightforest$hideShroudedArmor(ItemStack stack, EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
		if (!ASMHooks.cancelArmorRendering(cir.getReturnValue(), stack)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
	private void twilightforest$hideShroudedArmor(PoseStack stack, SubmitNodeCollector nodeCollector, ItemStack itemStack, EquipmentSlot slot, int light, HumanoidRenderState state, CallbackInfo ci) {
		if (!ASMHooks.cancelArmorRendering(true, itemStack)) {
			ci.cancel();
		}
	}
}
