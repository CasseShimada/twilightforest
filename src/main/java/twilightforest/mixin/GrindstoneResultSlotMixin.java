package twilightforest.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.events.TravellersGearEvents;

@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
public class GrindstoneResultSlotMixin {
	@Shadow
	@Final
	private GrindstoneMenu this$0;

	@Inject(method = "onTake", at = @At("HEAD"))
	private void twilightforest$returnModifierContents(Player player, ItemStack result, CallbackInfo ci) {
		if (!player.level().isClientSide()) {
			TravellersGearEvents.returnGrindstoneContents(player, this.this$0.getSlot(0).getItem(), this.this$0.getSlot(1).getItem());
		}
	}
}
