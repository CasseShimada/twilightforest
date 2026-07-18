package twilightforest.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.event.TravellersClientEvents;
import twilightforest.mixin.client.accessor.ClientInputAccessor;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void twilightforest$modifyTravellersGearInput(CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		ClientInput input = (ClientInput) (Object) this;
		if (player == null || player.input != input) {
			return;
		}
		((ClientInputAccessor) this).twilightforest$setMoveVector(
			TravellersClientEvents.modifyMovementInput(player, input, input.getMoveVector()));
	}
}
