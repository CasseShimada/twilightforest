package twilightforest.mixin.client;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.init.TFAttributeModifiers;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
	@Unique
	@Nullable
	private AttributeModifier twilightforest$suppressedStraightAheadModifier;

	@Inject(method = "getFieldOfViewModifier", at = @At("HEAD"))
	private void twilightforest$suppressStraightAheadFov(boolean scoping, float effectScale, CallbackInfoReturnable<Float> cir) {
		AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
		if (!(player instanceof LocalPlayer)) {
			return;
		}
		AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (movementSpeed == null) {
			return;
		}
		this.twilightforest$suppressedStraightAheadModifier = movementSpeed.getModifier(TFAttributeModifiers.STRAIGHT_AHEAD_ATTRIBUTE_MODIFIER_LOCATION);
		if (this.twilightforest$suppressedStraightAheadModifier != null) {
			movementSpeed.removeModifier(TFAttributeModifiers.STRAIGHT_AHEAD_ATTRIBUTE_MODIFIER_LOCATION);
		}
	}

	@Inject(method = "getFieldOfViewModifier", at = @At("RETURN"))
	private void twilightforest$restoreStraightAheadFov(boolean scoping, float effectScale, CallbackInfoReturnable<Float> cir) {
		AttributeModifier suppressed = this.twilightforest$suppressedStraightAheadModifier;
		this.twilightforest$suppressedStraightAheadModifier = null;
		if (suppressed == null) {
			return;
		}
		AttributeInstance movementSpeed = ((AbstractClientPlayer) (Object) this).getAttribute(Attributes.MOVEMENT_SPEED);
		if (movementSpeed != null) {
			movementSpeed.addOrUpdateTransientModifier(suppressed);
		}
	}
}
