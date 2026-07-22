// SPDX-License-Identifier: LGPL-3.0-only
package tschipp.carryon.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/** Declaration-only API 1 snapshot; never packaged by Twilight Forest. */
public record CarryActionContext(ServerPlayer player, CarryOperation operation, CarryKind subjectKind,
	CarryView carried, Optional<BlockPos> position, Optional<Direction> direction,
	Optional<BlockState> blockState, Optional<Identifier> subjectId, Optional<Identifier> targetId,
	boolean hasBlockEntity, boolean hasPassengerData) {
}
