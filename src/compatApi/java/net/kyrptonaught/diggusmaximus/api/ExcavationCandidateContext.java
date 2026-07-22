// SPDX-License-Identifier: MIT
package net.kyrptonaught.diggusmaximus.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Declaration-only API 1 snapshot; never packaged by Twilight Forest. */
public record ExcavationCandidateContext(ServerPlayer player, ServerLevel level, BlockPos origin,
	BlockState originState, BlockPos position, BlockState state, ItemStack tool,
	ExcavationPattern pattern, int brokenBlocks, int visitedCandidates) {
}
