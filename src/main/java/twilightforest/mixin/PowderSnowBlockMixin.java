package twilightforest.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.tags.TFItemTags;

@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockMixin {
	@Inject(method = "canEntityWalkOnPowderSnow", at = @At("RETURN"), cancellable = true)
	private static void twilightforest$allowBootsFromTag(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()
			&& entity instanceof LivingEntity living
			&& living.getItemBySlot(EquipmentSlot.FEET).is(TFItemTags.POWDER_SNOW_WALKABLE_BOOTS)) {
			cir.setReturnValue(true);
		}
	}
}
