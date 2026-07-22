// SPDX-License-Identifier: MIT
package net.kyrptonaught.diggusmaximus.api;

import java.util.Optional;

/** Declaration-only API 1 snapshot; never packaged by Twilight Forest. */
public record ExcavationRequestResult(ExcavationRequestStatus status,
	Optional<ExcavationCompletion> completion) {
	public boolean successful() {
		return this.status == ExcavationRequestStatus.SUCCESS;
	}
}
