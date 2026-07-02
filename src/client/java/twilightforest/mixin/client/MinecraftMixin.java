package twilightforest.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.init.TFSounds;
import twilightforest.network.ClientPacketDistributor;
import twilightforest.network.WipeOreMeterPacket;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	public ClientLevel level;

	@Shadow
	public LocalPlayer player;

	@Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V", shift = At.Shift.AFTER))
	private void twilightforest$wipeOreMeterOnMiss(CallbackInfoReturnable<Boolean> cir) {
		if (this.level == null || this.player == null) {
			return;
		}
		ItemStack stack = this.player.getMainHandItem();
		if (!stack.is(TFItems.ORE_METER.get())) {
			return;
		}
		if (!stack.has(TFDataComponents.ORE_DATA) && !stack.has(TFDataComponents.ORE_FILTER)) {
			return;
		}

		ClientPacketDistributor.sendToServer(new WipeOreMeterPacket(InteractionHand.MAIN_HAND));
		stack.remove(TFDataComponents.ORE_DATA);
		stack.remove(TFDataComponents.ORE_FILTER);
		this.level.playSound(this.player, this.player.blockPosition(), TFSounds.ORE_METER_CLEAR, SoundSource.PLAYERS, 1.25F, this.level.getRandom().nextFloat() * 0.2F + 0.6F);
	}
}
