package twilightforest.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import twilightforest.util.ColorUtil;

import java.util.List;
import java.util.Optional;

/** Overlay-neutral formatting and validation shared by Jade and WTHIT. */
public final class OverlayCompatLogic {
	private OverlayCompatLogic() {
	}

	public static Component formatDryingTime(int ticks) {
		ticks = Math.max(0, ticks);
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

	public static List<Block> missingQuestWools(int colorFlags) {
		return ColorUtil.WOOL_TO_DYE_IN_RAM_ORDER.entrySet().stream()
			.filter(entry -> isColorMissing(colorFlags, entry.getKey()))
			.map(entry -> entry.getValue())
			.toList();
	}

	public static Optional<DryingStatus> dryingStatus(boolean drying, int progress, int total) {
		if (!drying || total <= 0) {
			return Optional.empty();
		}
		int normalizedProgress = Math.clamp(progress, 0, total);
		return Optional.of(new DryingStatus(normalizedProgress, total, total - normalizedProgress));
	}

	public record DryingStatus(int progress, int total, int remaining) {
	}
}
