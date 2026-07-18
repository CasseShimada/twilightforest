package twilightforest.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

public final class JadeCompatLogic {
	private JadeCompatLogic() {
	}

	public static Component formatDryingTime(int ticks) {
		if (ticks < 20) {
			return Component.translatable("gui.twilightforest.drying_ticks", ticks);
		}

		int seconds = ticks / 20;
		int minutes = seconds / 60;
		seconds %= 60;

		if (minutes > 0 && seconds > 0) {
			return Component.translatable("gui.twilightforest.drying_time", minutes, seconds);
		}
		if (minutes > 0) {
			return Component.translatable(minutes == 1 ? "gui.twilightforest.drying_minute" : "gui.twilightforest.drying_minutes", minutes);
		}
		return Component.translatable(seconds == 1 ? "gui.twilightforest.drying_second" : "gui.twilightforest.drying_seconds", seconds);
	}

	public static boolean isColorMissing(int colorFlags, DyeColor color) {
		return (colorFlags & 1 << color.getId()) == 0;
	}
}
