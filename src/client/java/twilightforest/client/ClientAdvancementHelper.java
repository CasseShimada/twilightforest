package twilightforest.client;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import twilightforest.mixin.client.ClientAdvancementsAccessor;

public final class ClientAdvancementHelper {
	private ClientAdvancementHelper() {
	}

	@Nullable
	public static AdvancementHolder getAdvancement(Player player, Identifier id) {
		if (player instanceof LocalPlayer local) {
			ClientAdvancements manager = local.connection.getAdvancements();
			return manager.get(id);
		}
		return null;
	}

	public static boolean hasAdvancement(Player player, @Nullable AdvancementHolder holder) {
		if (!(player instanceof LocalPlayer local)) {
			return false;
		}
		if (holder == null) {
			return false;
		}
		ClientAdvancements manager = local.connection.getAdvancements();
		AdvancementProgress progress = ((ClientAdvancementsAccessor) manager).twilightforest$getProgress().get(holder);
		return progress != null && progress.isDone();
	}
}
