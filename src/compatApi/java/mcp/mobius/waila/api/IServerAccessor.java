// SPDX-License-Identifier: CC-BY-NC-SA-4.0
package mcp.mobius.waila.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Minimal declaration-only surface used by Twilight Forest. */
public interface IServerAccessor<T> {
	ServerLevel getLevel();
	ServerPlayer getPlayer();
	T getTarget();
}
