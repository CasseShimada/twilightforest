package twilightforest.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Transitional server reference retained while the remaining call sites are migrated.
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

}
