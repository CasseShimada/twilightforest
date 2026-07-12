package twilightforest.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.init.TFDataComponents;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
	@Shadow
	private boolean hasLayer(ItemStack stack, EquipmentClientInfo.LayerType layerType) {
		throw new AssertionError();
	}

	@Redirect(
		method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/layers/CapeLayer;hasLayer(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;)Z",
			ordinal = 0
		)
	)
	private boolean twilightforest$showCapeWhenWingsAreShrouded(CapeLayer layer, ItemStack stack, EquipmentClientInfo.LayerType layerType, PoseStack poseStack, SubmitNodeCollector nodeCollector, int light, AvatarRenderState state, float yRot, float xRot) {
		return this.hasLayer(stack, layerType) && !stack.has(TFDataComponents.EMPERORS_CLOTH);
	}
}
