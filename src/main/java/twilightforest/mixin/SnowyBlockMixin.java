package twilightforest.mixin;

import net.minecraft.world.level.block.SnowyBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.block.SnowLoggable;

@Mixin(SnowyBlock.class)
public abstract class SnowyBlockMixin {
	@Inject(method = "isSnowySetting", at = @At("HEAD"), cancellable = true)
	private static void twilightforest$snowloggedBlocksAreSnowy(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (state.getBlock() instanceof SnowLoggable && state.getValue(SnowLoggable.SNOW_LAYERS) > 0) {
			cir.setReturnValue(true);
		}
	}
}
