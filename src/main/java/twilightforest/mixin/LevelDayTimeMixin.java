package twilightforest.mixin;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.init.TFDimension;

@Mixin(Level.class)
public abstract class LevelDayTimeMixin {
	private static final long TWILIGHT_FIXED_DAY_TIME = 13000L;

	@Inject(method = "getOverworldClockTime", at = @At("HEAD"), cancellable = true)
	private void twilightforest$fixTwilightDayTime(CallbackInfoReturnable<Long> cir) {
		Level level = (Level) (Object) this;
		if (TFDimension.DIMENSION_KEY.equals(level.dimension())) {
			cir.setReturnValue(TWILIGHT_FIXED_DAY_TIME);
		}
	}
}
