// SPDX-License-Identifier: CC-BY-NC-SA-4.0
package mcp.mobius.waila.api;

import net.minecraft.resources.Identifier;

/** Minimal declaration-only surface used by Twilight Forest. */
public interface IPluginConfig {
	boolean getBoolean(Identifier key);
}
