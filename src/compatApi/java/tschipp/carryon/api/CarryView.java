// SPDX-License-Identifier: LGPL-3.0-only
package tschipp.carryon.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Declaration-only API 1 snapshot; never packaged by Twilight Forest. */
public record CarryView(CarryKind kind, Optional<Identifier> contentId, Optional<BlockState> blockState,
	Optional<EntityType<?>> entityType, Optional<UUID> carriedPlayerId, boolean hasBlockEntityData,
	boolean hasPassengerData, Set<Identifier> extensionDataIds, int serializedSize) {
}
