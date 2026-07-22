// SPDX-License-Identifier: LGPL-3.0-only
package tschipp.carryon.api;

/** Declaration-only API 1 snapshot; never packaged by Twilight Forest. */
public record CarryResult(CarryOperation operation, Status status, Stage stage, String detail) {
	public boolean successful() {
		return this.status == Status.SUCCESS;
	}

	public enum Status {
		SUCCESS, NO_ACTION, INVALID_CONTEXT, ALREADY_CARRYING, NOT_CARRYING, CORE_DENIED,
		EVENT_DENIED, SERIALIZATION_FAILED, WORLD_MUTATION_FAILED, ROLLBACK_FAILED,
		CALLBACK_FAILED
	}

	public enum Stage {
		VALIDATION, DECISION, SERIALIZATION, REMOVE_SOURCE, RESTORE_TARGET, COMMIT, ROLLBACK,
		LIFECYCLE
	}
}
