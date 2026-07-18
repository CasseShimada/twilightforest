package twilightforest.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.util.PlayerHelper;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin implements PlayerHelper.ClientAdvancementAccess {
	@ModifyExpressionValue(
		method = "isSprintingPossible",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInShallowWater()Z")
	)
	private boolean twilightforest$unrestrainedWaterSprinting(boolean shallowWater) {
		if (!shallowWater) {
			return false;
		}
		LocalPlayer player = (LocalPlayer) (Object) this;
		return !TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.UNRESTRAINED_MODIFIER)
			|| !player.canStandOnFluid(player.level().getFluidState(player.blockPosition()));
	}

	@Override
	@Nullable
	public AdvancementHolder twilightforest$getAdvancement(Identifier advancementLocation) {
		ClientAdvancements manager = ((LocalPlayer) (Object) this).connection.getAdvancements();
		return manager.get(advancementLocation);
	}

	@Override
	public boolean twilightforest$hasAdvancement(@Nullable AdvancementHolder holder) {
		if (holder == null) {
			return false;
		}
		ClientAdvancements manager = ((LocalPlayer) (Object) this).connection.getAdvancements();
		AdvancementProgress progress = ((ClientAdvancementsAccessor) manager).twilightforest$getProgress().get(holder);
		return progress != null && progress.isDone();
	}
}
