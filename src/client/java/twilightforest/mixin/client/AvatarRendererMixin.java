package twilightforest.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.renderer.TravellersArmorRenderer;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
	@Inject(method = "renderRightHand", at = @At("TAIL"))
	private void twilightforest$renderRightGlove(PoseStack matrices, SubmitNodeCollector collector, int light,
		Identifier skinTexture, boolean showSleeve, CallbackInfo ci) {
		TravellersArmorRenderer.INSTANCE.renderFirstPersonGlove(matrices, collector, light, HumanoidArm.RIGHT);
	}

	@Inject(method = "renderLeftHand", at = @At("TAIL"))
	private void twilightforest$renderLeftGlove(PoseStack matrices, SubmitNodeCollector collector, int light,
		Identifier skinTexture, boolean showSleeve, CallbackInfo ci) {
		TravellersArmorRenderer.INSTANCE.renderFirstPersonGlove(matrices, collector, light, HumanoidArm.LEFT);
	}
}
