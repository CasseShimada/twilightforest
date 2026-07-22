// SPDX-License-Identifier: LGPL-3.0-only
package tschipp.carryon.api;

import net.fabricmc.fabric.api.event.Event;

/** Declaration-only API 1 snapshot; runtime fields are supplied by Carry On. */
public final class CarryOnEvents {
	public static final Event<DecisionCallback> BEFORE_PICKUP = null;
	public static final Event<DecisionCallback> BEFORE_PLACE = null;
	public static final Event<DecisionCallback> BEFORE_STACK = null;

	private CarryOnEvents() {
	}

	@FunctionalInterface
	public interface DecisionCallback {
		CarryDecision decide(CarryActionContext context);
	}
}
