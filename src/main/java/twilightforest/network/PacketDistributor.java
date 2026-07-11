package twilightforest.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Fabric replacement for the previous PacketDistributor helpers.
 *
 * <p>This keeps existing call-sites mostly intact while the codebase is migrated.</p>
 */
public final class PacketDistributor {
	private static volatile MinecraftServer server;

	private PacketDistributor() {
	}

	public static void init() {
		// Capture a server reference so call-sites don't need to thread it through everywhere.
		ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
		ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
			if (server == s) server = null;
		});
	}

	@Nullable
	public static MinecraftServer getServer() {
		return server;
	}

	public static void sendToPlayersNear(ServerLevel level, ServerPlayer excluded, double x, double y, double z, double radius, CustomPacketPayload payload) {
		Objects.requireNonNull(level, "level");
		Objects.requireNonNull(payload, "payload");
		Vec3 pos = new Vec3(x, y, z);
		for (ServerPlayer player : PlayerLookup.around(level, pos, radius)) {
			if (player != excluded) {
				ServerPlayNetworking.send(player, payload);
			}
		}
	}

}
