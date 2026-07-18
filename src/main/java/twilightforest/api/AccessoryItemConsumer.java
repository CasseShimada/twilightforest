package twilightforest.api;

/**
 * Consumes Twilight Forest-requested items from storage owned by another mod.
 *
 * <p>Providers run on the logical server thread. A provider must return
 * {@link AccessoryConsumptionResult#pass()} without mutation when it cannot
 * satisfy the request. When it can, it removes exactly one requested item from
 * its own storage and returns the complete stack snapshot from immediately
 * before that mutation.</p>
 */
@FunctionalInterface
public interface AccessoryItemConsumer {
	/**
	 * Attempts one logical-server consumption request synchronously. Runtime and
	 * linkage failures are caught by {@link AccessoryApi}, logged with the
	 * provider ID, and treated as PASS so later providers may run.
	 *
	 * @param context immutable request context
	 * @return non-null PASS or consumed result
	 */
	AccessoryConsumptionResult tryConsume(AccessoryConsumptionContext context);
}
