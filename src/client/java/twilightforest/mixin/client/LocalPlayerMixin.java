package twilightforest.mixin.client;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import twilightforest.util.PlayerHelper;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin implements PlayerHelper.ClientAdvancementAccess {
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
