package twilightforest.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
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

	private static MinecraftServer requireServer() {
		MinecraftServer s = server;
		if (s == null) throw new IllegalStateException("Server not available (called too early or from client-only context)");
		return s;
	}

	@Nullable
	public static MinecraftServer getServer() {
		return server;
	}

	public static void sendToAllPlayers(CustomPacketPayload payload) {
		Objects.requireNonNull(payload, "payload");
		for (ServerPlayer player : PlayerLookup.all(requireServer())) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
		Objects.requireNonNull(player, "player");
		Objects.requireNonNull(payload, "payload");
		ServerPlayNetworking.send(player, payload);
	}

	public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload) {
		Objects.requireNonNull(entity, "entity");
		Objects.requireNonNull(payload, "payload");
		for (ServerPlayer player : PlayerLookup.tracking(entity)) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload) {
		sendToPlayersTrackingEntity(entity, payload);
		if (entity instanceof ServerPlayer sp) {
			ServerPlayNetworking.send(sp, payload);
		}
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

	public static void sendToPlayersTrackingBlockEntity(BlockEntity be, CustomPacketPayload payload) {
		Objects.requireNonNull(be, "be");
		Objects.requireNonNull(payload, "payload");
		for (ServerPlayer player : PlayerLookup.tracking(be)) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	public static void sendToPlayersTrackingPos(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
		Objects.requireNonNull(level, "level");
		Objects.requireNonNull(pos, "pos");
		Objects.requireNonNull(payload, "payload");
		for (ServerPlayer player : PlayerLookup.tracking(level, pos)) {
			ServerPlayNetworking.send(player, payload);
		}
	}

}
