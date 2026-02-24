package twilightforest.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.events.EntityEvents;

@Mixin(ResultSlot.class)
public class ResultSlotMixin {
	@Shadow
	private CraftingContainer craftSlots;

	@Inject(method = "onTake", at = @At("HEAD"))
	private void twilightforest$onTake(Player player, ItemStack stack, CallbackInfo ci) {
		if (player.level().isClientSide()) {
			return;
		}
		EntityEvents.handleCrafting(player, stack, this.craftSlots);
	}
}
