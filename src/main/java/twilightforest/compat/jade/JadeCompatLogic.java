package twilightforest.compat.jade;

import net.minecraft.world.item.DyeColor;
import net.minecraft.network.chat.Component;
import twilightforest.compat.OverlayCompatLogic;

public final class JadeCompatLogic {
	private JadeCompatLogic() {
	}

	public static Component formatDryingTime(int ticks) {
		return OverlayCompatLogic.formatDryingTime(ticks);
	}

	public static boolean isColorMissing(int colorFlags, DyeColor color) {
		return OverlayCompatLogic.isColorMissing(colorFlags, color);
	}
}
