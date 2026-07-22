// SPDX-License-Identifier: CC-BY-NC-SA-4.0
package mcp.mobius.waila.api;

import net.minecraft.network.chat.Component;

/** Minimal declaration-only surface used by Twilight Forest. */
public interface ITooltip {
	void addLine(Component component);
	void addLine(ITooltipComponent component);
}
