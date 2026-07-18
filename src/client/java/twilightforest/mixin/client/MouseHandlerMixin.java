package twilightforest.mixin.client;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.client.event.TravellersClientEvents;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Redirect(
		method = "turnPlayer",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0)
	)
	private Object twilightforest$adjustZoomSensitivity(OptionInstance<?> option) {
		Object value = option.get();
		return value instanceof Double sensitivity
			? TravellersClientEvents.modifyMouseSensitivity(sensitivity)
			: value;
	}
}
