package twilightforest.client.overlay.display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.Level;
import twilightforest.TwilightForestMod;
import twilightforest.config.TFConfig;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class ClockDisplay implements ItemDisplay {
	private static final long INITIAL_DAY_OFFSET_IN_TICKS = 6000L;
	private static final long MINECRAFT_DAY_LENGTH_IN_TICKS = 24000L;
	private static final long REAL_LIFE_DAY_LENGTH_IN_SECONDS = 86400L;
	private static final DateTimeFormatter FORMAT_24 = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter FORMAT_12 = DateTimeFormatter.ofPattern("hh:mm a");
	private static final NavigableMap<Long, TimeFrame> TIME_FRAMES = new TreeMap<>(Map.ofEntries(
		Map.entry(0L, TimeFrame.SUNRISE),
		Map.entry(501L, TimeFrame.DAY),
		Map.entry(12500L, TimeFrame.SUNSET),
		Map.entry(14000L, TimeFrame.NIGHT),
		Map.entry(22000L, TimeFrame.SUNRISE)
	));

	private static Component getGameTime(Level level, boolean use24HourFormat) {
		if (!hasOverworldClock(level)) {
			return Component.translatable("travellers_gear.modifier.twilightforest.item_display.clock.unknown");
		}
		long rawTime = level.getDefaultClockTime();
		long ticksOfDay = (rawTime + INITIAL_DAY_OFFSET_IN_TICKS) % MINECRAFT_DAY_LENGTH_IN_TICKS;
		long secondsOfDay = ticksOfDay * REAL_LIFE_DAY_LENGTH_IN_SECONDS / MINECRAFT_DAY_LENGTH_IN_TICKS;
		LocalTime localTime = LocalTime.ofSecondOfDay(secondsOfDay);
		return Component.literal(localTime.format(use24HourFormat ? FORMAT_24 : FORMAT_12));
	}

	@Override
	public void render(ItemStack item, GuiGraphicsExtractor graphics, Minecraft minecraft, Gui gui, Player player, int widestWidgetWidth) {
		FormattedCharSequence text = this.getText(minecraft).getVisualOrderText();
		if (hasOverworldClock(minecraft.level)) {
			int frame = this.getFrameForTime(minecraft.level.getDefaultClockTime()).frame;
			int x = widestWidgetWidth / 2 - 5 - minecraft.font.width(text) / 2;
			graphics.blit(RenderPipelines.GUI_TEXTURED, TwilightForestMod.getGuiTexture("time.png"), x, 0,
				(frame % 2) * 8, (frame / 2 % 2) * 8, 8, 8, 16, 16);
		}
		graphics.text(minecraft.font, text,
			Math.max(0, widestWidgetWidth / 2 + 5 - minecraft.font.width(text) / 2), 0, 0xFFFFFFFF);
	}

	@Override
	public Bounds getWidgetSize(ItemStack item, Minecraft minecraft, Gui gui, Player player, int widestWidgetWidth) {
		int textWidth = minecraft.font.width(this.getText(minecraft));
		boolean natural = hasOverworldClock(minecraft.level);
		return new Bounds(Math.max(0, widestWidgetWidth / 2 - (natural ? 5 : 0) - textWidth / 2),
			0, textWidth + (natural ? 10 : 0), minecraft.font.lineHeight);
	}

	private Component getText(Minecraft minecraft) {
		return getGameTime(minecraft.level, TFConfig.clock24HourFormat);
	}

	private static boolean hasOverworldClock(Level level) {
		return level.dimensionType().defaultClock()
			.filter(clock -> clock.is(WorldClocks.OVERWORLD))
			.isPresent();
	}

	private TimeFrame getFrameForTime(long dayTimeInTicks) {
		return TIME_FRAMES.floorEntry(dayTimeInTicks % MINECRAFT_DAY_LENGTH_IN_TICKS).getValue();
	}

	private enum TimeFrame {
		SUNRISE(0),
		DAY(1),
		SUNSET(2),
		NIGHT(3);

		private final int frame;

		TimeFrame(int frame) {
			this.frame = frame;
		}
	}
}
