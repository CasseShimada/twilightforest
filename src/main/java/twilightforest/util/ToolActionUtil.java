package twilightforest.util;

import net.minecraft.world.item.*;

/**
 * Fabric/vanilla-friendly tool checks replacing the previous ItemAbility/ItemAbilities system.
 *
 * <p>These checks are intentionally conservative: we only match vanilla tool item types.</p>
 */
public final class ToolActionUtil {
	private ToolActionUtil() {
	}

	public static boolean isAxe(ItemStack stack) {
		return stack.getItem() instanceof AxeItem;
	}

	public static boolean isShovel(ItemStack stack) {
		return stack.getItem() instanceof ShovelItem;
	}

	public static boolean isShears(ItemStack stack) {
		return stack.getItem() instanceof ShearsItem;
	}

	public static boolean isFireStarter(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof FlintAndSteelItem || item instanceof FireChargeItem;
	}
}
