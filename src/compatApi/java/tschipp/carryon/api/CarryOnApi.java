// SPDX-License-Identifier: LGPL-3.0-only
package tschipp.carryon.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Declaration-only API 1 snapshot; never packaged by Twilight Forest. */
public final class CarryOnApi {
	public static final int API_VERSION = 1;

	private CarryOnApi() {
	}

	public static boolean isCarrying(Player player) {
		throw new UnsupportedOperationException("declaration-only API snapshot");
	}

	public static CarryView carryView(Player player) {
		throw new UnsupportedOperationException("declaration-only API snapshot");
	}

	public static CarryResult requestPickupBlock(ServerPlayer player, BlockPos source) {
		throw new UnsupportedOperationException("declaration-only API snapshot");
	}

	public static CarryResult requestPlace(ServerPlayer player, BlockPos target, Direction face) {
		throw new UnsupportedOperationException("declaration-only API snapshot");
	}
}
