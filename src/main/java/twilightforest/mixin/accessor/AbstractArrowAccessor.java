package twilightforest.mixin.accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
	@Invoker("doPostHurtEffects")
	void twilightforest$doPostHurtEffects(LivingEntity target);
}
