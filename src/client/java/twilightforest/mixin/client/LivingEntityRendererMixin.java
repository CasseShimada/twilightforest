package twilightforest.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.event.ClientEvents;
import twilightforest.client.renderer.entity.layers.IceLayer;
import twilightforest.client.renderer.entity.layers.ShieldLayer;
import twilightforest.item.TrophyItem;
import twilightforest.potions.FrostedEffect;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {
	@Shadow
	protected net.minecraft.client.model.EntityModel<S> model;

	@Unique
	private boolean twilightforest$headVisible;
	@Unique
	private boolean twilightforest$hatVisible;
	@Unique
	private boolean twilightforest$hadHeadModel;
	@Unique
	private boolean twilightforest$hadHatModel;

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void twilightforest$extractRenderState(T entity, S state, float partialTick, CallbackInfo ci) {
		if (!(state instanceof FabricRenderState fabricState)) {
			return;
		}
		boolean wearingTrophy = entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof TrophyItem || ClientEvents.areCuriosEquipped(entity);
		fabricState.setData(ClientEvents.HEAD_KEY, wearingTrophy);
		fabricState.setData(ShieldLayer.SHIELD_COUNT_KEY, ShieldLayer.getShieldCount(entity));

		AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		AttributeModifier frost = speed != null ? speed.getModifier(FrostedEffect.MOVEMENT_SPEED_MODIFIER) : null;
		if (frost != null) {
			fabricState.setData(IceLayer.FROST_COUNT_KEY, frost.amount());
			fabricState.setData(IceLayer.FROST_ID_KEY, entity.getId());
		} else {
			fabricState.setData(IceLayer.FROST_COUNT_KEY, 0.0D);
			fabricState.setData(IceLayer.FROST_ID_KEY, 0);
		}
	}

	@Inject(method = "submit", at = @At("HEAD"))
	private void twilightforest$hideHead(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
		twilightforest$hadHeadModel = false;
		twilightforest$hadHatModel = false;

		if (!(state instanceof FabricRenderState fabricState)) {
			return;
		}
		boolean visible = !Boolean.TRUE.equals(fabricState.getData(ClientEvents.HEAD_KEY));
		boolean isPlayer = state instanceof AvatarRenderState;

		if (model instanceof HeadedModel headedModel) {
			twilightforest$hadHeadModel = true;
			twilightforest$headVisible = headedModel.getHead().visible;
			headedModel.getHead().visible = visible && (isPlayer || headedModel.getHead().visible);

			if (model instanceof HumanoidModel<?> humanoidModel) {
				twilightforest$hadHatModel = true;
				twilightforest$hatVisible = humanoidModel.hat.visible;
				humanoidModel.hat.visible = visible && (isPlayer || humanoidModel.hat.visible);
			}
		}
	}

	@Inject(method = "submit", at = @At("RETURN"))
	private void twilightforest$restoreHead(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
		if (twilightforest$hadHeadModel && model instanceof HeadedModel headedModel) {
			headedModel.getHead().visible = twilightforest$headVisible;
		}
		if (twilightforest$hadHatModel && model instanceof HumanoidModel<?> humanoidModel) {
			humanoidModel.hat.visible = twilightforest$hatVisible;
		}
	}
}
