package twilightforest.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.events.TravellersGearEvents;

@Mixin(Player.class)
public abstract class PlayerMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	private void twilightforest$travellersGearTickStart(CallbackInfo ci) {
		TravellersGearEvents.onPlayerTickStart((Player) (Object) this);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void twilightforest$travellersGearTickEnd(CallbackInfo ci) {
		TravellersGearEvents.onPlayerTickEnd((Player) (Object) this);
	}
}
