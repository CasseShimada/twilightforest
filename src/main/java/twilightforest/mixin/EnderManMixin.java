package twilightforest.mixin;

import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.init.custom.TravellersModifiersManager;

@Mixin(EnderMan.class)
public class EnderManMixin {
	@Inject(method = "isBeingStaredBy", at = @At("HEAD"), cancellable = true)
	private void twilightforest$disguiseAllNightGoggles(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.ALL_NIGHT_GOGGLES_MODIFIER)) {
			cir.setReturnValue(false);
		}
	}
}
