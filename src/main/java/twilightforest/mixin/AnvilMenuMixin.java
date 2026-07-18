package twilightforest.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.events.TravellersGearEvents;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
	@Shadow
	@Final
	private DataSlot cost;

	@Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
	private void twilightforest$blockTravellersGearCombination(CallbackInfo ci) {
		AnvilMenu menu = (AnvilMenu) (Object) this;
		if (!TravellersGearEvents.blocksAnvilCombination(menu.getSlot(0).getItem(), menu.getSlot(1).getItem())) {
			return;
		}
		menu.getSlot(2).set(ItemStack.EMPTY);
		this.cost.set(0);
		menu.broadcastChanges();
		ci.cancel();
	}
}
