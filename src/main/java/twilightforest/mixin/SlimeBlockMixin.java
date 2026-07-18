package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SlimeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.init.custom.TravellersModifiersManager;

@Mixin(SlimeBlock.class)
public abstract class SlimeBlockMixin {
	@ModifyExpressionValue(
		method = "stepOn",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isSteppingCarefully()Z")
	)
	private boolean twilightforest$unrestrainedSlimeMomentum(boolean original, @Local(argsOnly = true) Entity entity) {
		return original || TravellersModifiersManager.isModifierActive(entity, TravellersModifiersManager.UNRESTRAINED_MODIFIER);
	}
}
