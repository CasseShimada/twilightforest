package twilightforest.world;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

/** Connected test player used only by the isolated runtime transaction probes. */
final class RuntimeCompatTestPlayer {
	private RuntimeCompatTestPlayer() {
	}

	static ServerPlayer create(MinecraftServer server, ServerLevel level, String identity) {
		UUID id = UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
		String name = "TFProbe" + id.toString().substring(0, 8);
		GameProfile profile = new GameProfile(id, name);
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
		ServerPlayer player = new ServerPlayer(server, level, profile, cookie.clientInformation());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		server.getPlayerList().placeNewPlayer(connection, player, cookie);
		player.setGameMode(GameType.SURVIVAL);
		player.getInventory().clearContent();
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		player.teleportTo(level, 1.5D, 120.0D, 5.5D, Set.of(), 180.0F, 0.0F, false);
		return player;
	}

	static void remove(MinecraftServer server, ServerPlayer player) {
		server.getPlayerList().remove(player);
	}
}
