// SPDX-License-Identifier: MIT
package net.kyrptonaught.diggusmaximus.api;

import java.util.Optional;

/** Declaration-only API 1 snapshot; never packaged by Twilight Forest. */
public record ExcavationCompletion(ExcavationView excavation, ExcavationEndReason reason,
	Optional<String> diagnostic) {
}
