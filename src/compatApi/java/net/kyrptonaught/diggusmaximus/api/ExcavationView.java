// SPDX-License-Identifier: MIT
package net.kyrptonaught.diggusmaximus.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.UUID;

/** Declaration-only API 1 snapshot; never packaged by Twilight Forest. */
public record ExcavationView(UUID id, UUID playerId, ResourceKey<Level> dimension, BlockPos origin,
	BlockState originState, ItemStack tool, Optional<Direction> direction, ExcavationPattern pattern,
	int brokenBlocks, int visitedCandidates, boolean programmatic) {
}
