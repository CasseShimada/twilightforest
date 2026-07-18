package twilightforest.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.events.TravellersGearEvents;

@Mixin(GrindstoneMenu.class)
public class GrindstoneMenuMixin {
	@Unique
	private Player twilightforest$player;

	@Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
	private void twilightforest$storePlayer(int containerId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
		this.twilightforest$player = inventory.player;
	}

	@Inject(method = "computeResult", at = @At("HEAD"), cancellable = true)
	private void twilightforest$removeTravellersModifiers(ItemStack top, ItemStack bottom, CallbackInfoReturnable<ItemStack> cir) {
		TravellersGearEvents.computeGrindstoneResult(this.twilightforest$player.registryAccess(), top, bottom).ifPresent(cir::setReturnValue);
	}
}
