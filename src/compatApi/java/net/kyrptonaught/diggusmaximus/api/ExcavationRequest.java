// SPDX-License-Identifier: MIT
package net.kyrptonaught.diggusmaximus.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/** Declaration-only API 1 snapshot; never packaged by Twilight Forest. */
public record ExcavationRequest(ServerPlayer player, BlockPos origin, Optional<Direction> direction,
	ExcavationPattern pattern) {
	public ExcavationRequest(ServerPlayer player, BlockPos origin, ExcavationPattern pattern) {
		this(player, origin, Optional.empty(), pattern);
	}
}
