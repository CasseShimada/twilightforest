package twilightforest.api;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

/**
 * Registration and dispatch API for accessory-like item storage.
 *
 * <p>This common entry point does not depend on an accessory mod. Registration
 * has no save or network effect; only a provider may mutate storage during a
 * logical-server dispatch.</p>
 */
public final class AccessoryApi {
	private static final String EXTENSION_POINT = "accessory item consumption";
	private static final ProviderRegistry<AccessoryItemConsumer> CONSUMERS = new ProviderRegistry<>(EXTENSION_POINT);
	private static final ThreadLocal<Boolean> DISPATCHING = ThreadLocal.withInitial(() -> Boolean.FALSE);

	private AccessoryApi() {
	}

	/**
	 * Registers a server-side item consumer under a stable, namespaced ID. Call
	 * this from a common mod initializer before the server-start freeze. Although
	 * the registry publishes thread-safe snapshots, registration is a lifecycle
	 * operation rather than a runtime operation.
	 *
	 * @param id stable provider ID owned by the registering mod
	 * @param consumer provider to invoke on server consumption requests
	 *
	 * @throws IllegalArgumentException when the ID is already registered
	 * @throws IllegalStateException when registrations are frozen
	 */
	public static void registerItemConsumer(Identifier id, AccessoryItemConsumer consumer) {
		CONSUMERS.register(id, consumer);
	}

	/**
	 * Returns consumer IDs in an immutable, deterministic lexicographic snapshot.
	 * This common-side query does not invoke providers or touch saved/networked
	 * state.
	 *
	 * @return immutable ordered provider-ID snapshot
	 */
	public static List<Identifier> itemConsumerIds() {
		return CONSUMERS.ids();
	}

	/**
	 * Invokes consumers in deterministic ID order and returns the first consumed
	 * result. Runtime and linkage failures are logged with their provider ID and
	 * dispatch continues. Recursive dispatch on the same thread returns PASS.
	 *
	 * <p>This method enforces the owning logical server thread before invoking any
	 * provider; an off-thread call fails immediately. The API itself does not
	 * persist the returned stack or send a packet.</p>
	 *
	 * <p>Provider-owned storage mutations cannot be rolled back by this API. A
	 * provider must therefore atomically remove exactly one requested item and
	 * return its valid pre-consumption snapshot. After mutating storage it must not
	 * throw, return PASS, or return an invalid snapshot, because failure handling
	 * may continue dispatch to a later provider.</p>
	 *
	 * @param context server-side request context
	 * @return first valid consumed result, or PASS
	 * @throws IllegalStateException when called outside the player's owning logical
	 * server thread
	 */
	public static AccessoryConsumptionResult tryConsume(AccessoryConsumptionContext context) {
		Objects.requireNonNull(context, "context");
		MinecraftServer server = context.player().level().getServer();
		if (server == null || !server.isSameThread()) {
			throw new IllegalStateException("Accessory item consumption must run on the owning logical server thread");
		}
		if (DISPATCHING.get()) {
			return AccessoryConsumptionResult.pass();
		}

		DISPATCHING.set(Boolean.TRUE);
		try {
			for (ProviderRegistry.Entry<AccessoryItemConsumer> entry : CONSUMERS.entries()) {
				try {
					AccessoryConsumptionResult result = Objects.requireNonNull(entry.provider().tryConsume(context), "provider result");
					if (!result.wasConsumed()) {
						continue;
					}
					ItemStack consumedStack = result.consumedStack();
					if (!consumedStack.is(context.requestedItem())) {
						throw new IllegalArgumentException("Consumed snapshot does not contain the requested item");
					}
					return AccessoryConsumptionResult.consumed(consumedStack);
				} catch (RuntimeException | LinkageError failure) {
					ApiLog.providerFailure(EXTENSION_POINT, entry.id(), failure);
				}
			}
			return AccessoryConsumptionResult.pass();
		} finally {
			DISPATCHING.remove();
		}
	}

	/**
	 * Returns whether accessory registrations are frozen. This thread-safe query
	 * does not invoke providers.
	 *
	 * @return true after the accessory registry has frozen
	 */
	public static boolean registrationsFrozen() {
		return CONSUMERS.isFrozen();
	}

	static void freezeRegistrations() {
		CONSUMERS.freeze();
	}

	static void resetForTests() {
		CONSUMERS.resetForTests();
		DISPATCHING.remove();
	}
}
