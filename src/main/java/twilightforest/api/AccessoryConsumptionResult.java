package twilightforest.api;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * Result returned by an {@link AccessoryItemConsumer}.
 *
 * <p>A consumed result contains a defensive snapshot of the stack immediately
 * before the provider consumed one item from its own storage. The API does not
 * persist the snapshot or synchronize it to clients.</p>
 *
 * @param outcome whether the provider passed or consumed the requested item
 * @param consumedStack pre-consumption stack snapshot for a consumed result
 */
public record AccessoryConsumptionResult(Outcome outcome, ItemStack consumedStack) {
	private static final AccessoryConsumptionResult PASS = new AccessoryConsumptionResult(Outcome.PASS, ItemStack.EMPTY);

	/**
	 * Validates a provider decision and snapshots consumed stacks. Construction
	 * performs no persistence or networking.
	 *
	 * @param outcome provider decision
	 * @param consumedStack pre-consumption stack, or an empty stack for PASS
	 */
	public AccessoryConsumptionResult {
		Objects.requireNonNull(outcome, "outcome");
		Objects.requireNonNull(consumedStack, "consumedStack");
		if (outcome == Outcome.PASS) {
			if (!consumedStack.isEmpty()) {
				throw new IllegalArgumentException("A PASS result cannot contain a consumed stack");
			}
			consumedStack = ItemStack.EMPTY;
		} else {
			if (consumedStack.isEmpty()) {
				throw new IllegalArgumentException("A CONSUMED result requires a non-empty pre-consumption stack");
			}
			consumedStack = consumedStack.copy();
		}
	}

	/**
	 * Returns the shared immutable pass result. It represents no provider-owned
	 * storage mutation.
	 *
	 * @return shared PASS result
	 */
	public static AccessoryConsumptionResult pass() {
		return PASS;
	}

	/**
	 * Creates a consumed result and defensively copies the supplied stack. The
	 * caller remains responsible for having removed exactly one item from its own
	 * server-side storage before returning this result.
	 *
	 * @param preConsumptionStack non-empty stack before consumption
	 * @return result containing a defensive stack snapshot
	 */
	public static AccessoryConsumptionResult consumed(ItemStack preConsumptionStack) {
		return new AccessoryConsumptionResult(Outcome.CONSUMED, preConsumptionStack);
	}

	/**
	 * Returns whether a provider consumed the requested item.
	 *
	 * @return true for {@link Outcome#CONSUMED}
	 */
	public boolean wasConsumed() {
		return this.outcome == Outcome.CONSUMED;
	}

	/**
	 * Returns the provider decision.
	 *
	 * @return PASS or CONSUMED
	 */
	@Override
	public Outcome outcome() {
		return this.outcome;
	}

	/**
	 * Returns a defensive copy of the pre-consumption stack snapshot.
	 *
	 * @return copied snapshot, or an empty stack for PASS
	 */
	@Override
	public ItemStack consumedStack() {
		return this.consumedStack.copy();
	}

	/**
	 * Provider decision for an accessory consumption request. Decisions are not
	 * persisted or transmitted by this API.
	 */
	public enum Outcome {
		/** The provider made no storage mutation. */
		PASS,
		/** The provider removed one requested item from its own storage. */
		CONSUMED
	}
}
