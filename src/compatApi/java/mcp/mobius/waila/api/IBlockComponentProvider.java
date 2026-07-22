// SPDX-License-Identifier: CC-BY-NC-SA-4.0
package mcp.mobius.waila.api;

/** Minimal declaration-only surface used by Twilight Forest. */
public interface IBlockComponentProvider {
	default void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
	}
}
