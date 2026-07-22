// SPDX-License-Identifier: CC-BY-NC-SA-4.0
package mcp.mobius.waila.api.component;

import mcp.mobius.waila.api.ITooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Declaration-only WTHIT 20 constructor surface; runtime implementation is supplied by WTHIT. */
public class ItemListComponent implements ITooltipComponent {
	public ItemListComponent(List<ItemStack> items, int maxHeight, float scale) {
	}
}
