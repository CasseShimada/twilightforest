package twilightforest.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PlayerMessaging {
	private PlayerMessaging() {}

	public static void displayClientMessage(Player player, Component message, boolean actionBar) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendSystemMessage(message, actionBar);
		} else {
			player.sendSystemMessage(message);
		}
	}
}
