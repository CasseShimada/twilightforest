package twilightforest.mixin.client;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.event.ClientEvents;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
	private void twilightforest$hideHeadWhenWearingTrophy(AvatarRenderState state, CallbackInfo ci) {
		if (!(state instanceof FabricRenderState fabricState)) {
			return;
		}

		boolean wearingTrophy = Boolean.TRUE.equals(fabricState.getData(ClientEvents.HEAD_KEY));
		if ((Object) this instanceof HeadedModel headedModel) {
			headedModel.getHead().visible = !wearingTrophy;
		}
		if ((Object) this instanceof HumanoidModel<?> humanoidModel) {
			humanoidModel.hat.visible = humanoidModel.hat.visible && !wearingTrophy;
		}
	}
}
