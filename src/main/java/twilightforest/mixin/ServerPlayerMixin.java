package twilightforest.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import twilightforest.item.travellers_gear.TravellersGearLogic;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
	@ModifyArg(
		method = {"checkMovementStatistics", "jumpFromGround"},
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"
		),
		index = 0
	)
	private float twilightforest$efficientEater(float exhaustion) {
		return TravellersGearLogic.modifyMovementExhaustion((ServerPlayer) (Object) this, exhaustion);
	}
}
