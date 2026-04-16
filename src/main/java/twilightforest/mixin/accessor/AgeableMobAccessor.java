package twilightforest.mixin.accessor;

import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AgeableMob.class)
public interface AgeableMobAccessor {
	@Accessor("ageLockParticleTimer")
	int twilightforest$getAgeLockParticleTimer();

	@Invoker("setAgeLocked")
	void twilightforest$invokeSetAgeLocked(boolean ageLocked);
}
