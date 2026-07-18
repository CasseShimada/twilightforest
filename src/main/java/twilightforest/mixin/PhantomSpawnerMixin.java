package twilightforest.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.init.custom.TravellersModifiersManager;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {
	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"
		)
	)
	private boolean twilightforest$skipAllNightGogglesWearers(ServerPlayer player) {
		return player.isSpectator()
			|| TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.ALL_NIGHT_GOGGLES_MODIFIER);
	}
}
