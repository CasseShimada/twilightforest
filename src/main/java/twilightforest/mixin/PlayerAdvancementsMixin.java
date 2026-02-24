package twilightforest.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.events.EntityEvents;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
	@Shadow
	private ServerPlayer player;

	@Inject(method = "award", at = @At("TAIL"))
	private void twilightforest$onAward(AdvancementHolder advancement, String criterionKey, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue() || this.player == null) {
			return;
		}
		EntityEvents.handleAdvancementEarned(this.player, advancement);
	}
}
