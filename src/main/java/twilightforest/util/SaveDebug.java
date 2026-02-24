package twilightforest.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.TicketStorage;
import org.apache.logging.log4j.Logger;
import twilightforest.TwilightForestMod;

import java.lang.reflect.Field;
import java.util.Map;

public final class SaveDebug {
	private static final Logger LOGGER = TwilightForestMod.LOGGER;
	private static volatile boolean shutdownInProgress;
	private static volatile long shutdownStartNanos;
	private static volatile boolean clearedKeepAliveTickets;
	private static volatile Field ticketStorageField;

	private SaveDebug() {
	}

	public static void onServerStopping(MinecraftServer server) {
		if (shutdownInProgress) {
			return;
		}
		shutdownInProgress = true;
		shutdownStartNanos = System.nanoTime();

		if (!clearedKeepAliveTickets) {
			clearedKeepAliveTickets = true;
			clearKeepAliveTickets(server);
		}

		Thread watchdog = new Thread(() -> {
			try {
				Thread.sleep(15000L);
			} catch (InterruptedException ignored) {
				return;
			}
			if (!shutdownInProgress) {
				return;
			}
			LOGGER.error("Server still stopping after {} ms; dumping threads", shutdownElapsedMs());
			for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
				Thread thread = entry.getKey();
				LOGGER.error("Thread {} [id={}, state={}]", thread.getName(), thread.getId(), thread.getState());
				for (StackTraceElement element : entry.getValue()) {
					LOGGER.error("  at {}", element);
				}
			}
		}, "TF Save Watchdog");
		watchdog.setDaemon(true);
		watchdog.start();
	}

	public static void onServerStopped(MinecraftServer server) {
		shutdownInProgress = false;
		shutdownStartNanos = 0L;
		clearedKeepAliveTickets = false;
	}

	public static boolean isShutdownInProgress() {
		return shutdownInProgress;
	}

	public static long shutdownElapsedMs() {
		if (!shutdownInProgress) {
			return 0L;
		}
		return (System.nanoTime() - shutdownStartNanos) / 1_000_000L;
	}

	private static void clearKeepAliveTickets(MinecraftServer server) {
		for (ServerLevel level : server.getAllLevels()) {
			ServerChunkCache chunkSource = level.getChunkSource();
			TicketStorage storage = getTicketStorage(chunkSource);
			if (storage == null) {
				continue;
			}
			storage.removeTicketIf((ticket, chunkKey) -> ticket.getType().shouldKeepDimensionActive(), null);
			LOGGER.warn("Cleared keep-alive tickets during shutdown for {}", level.dimension().identifier());
		}
	}

	private static TicketStorage getTicketStorage(ServerChunkCache chunkSource) {
		try {
			Field field = ticketStorageField;
			if (field == null) {
				for (Field candidate : ServerChunkCache.class.getDeclaredFields()) {
					if (TicketStorage.class.isAssignableFrom(candidate.getType())) {
						field = candidate;
						break;
					}
				}
				if (field == null) {
					throw new NoSuchFieldException("TicketStorage field not found");
				}
				field.setAccessible(true);
				ticketStorageField = field;
			}
			return (TicketStorage) field.get(chunkSource);
		} catch (ReflectiveOperationException e) {
			LOGGER.error("Failed to access ServerChunkCache.ticketStorage for shutdown cleanup", e);
			return null;
		}
	}
}
