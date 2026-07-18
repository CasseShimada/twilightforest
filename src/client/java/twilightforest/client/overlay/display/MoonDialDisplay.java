package twilightforest.client.overlay.display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import twilightforest.TwilightForestMod;
import twilightforest.item.MoonDialItem;

public final class MoonDialDisplay implements ItemDisplay {
	@Override
	public void render(ItemStack item, GuiGraphicsExtractor graphics, Minecraft minecraft, Gui gui, Player player, int widestWidgetWidth) {
		int phase = minecraft.level.environmentAttributes()
			.getDimensionValue(EnvironmentAttributes.MOON_PHASE).index();
		FormattedCharSequence text = this.getText(minecraft).getVisualOrderText();
		int x = widestWidgetWidth / 2 - 5 - minecraft.font.width(text) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TwilightForestMod.getGuiTexture("moon.png"), x, 0,
			(phase % 4) * 8, (phase / 4 % 2) * 8, 8, 8, 32, 16);
		graphics.text(minecraft.font, text,
			Math.max(0, widestWidgetWidth / 2 + 5 - minecraft.font.width(text) / 2), 0, 0xFFFFFFFF);
	}

	private Component getText(Minecraft minecraft) {
		return MoonDialItem.getMoonPhase(minecraft.level);
	}

	@Override
	public Bounds getWidgetSize(ItemStack item, Minecraft minecraft, Gui gui, Player player, int widestWidgetWidth) {
		int textWidth = minecraft.font.width(this.getText(minecraft));
		return new Bounds(Math.max(0, widestWidgetWidth / 2 - 5 - textWidth / 2),
			0, textWidth + 10, minecraft.font.lineHeight);
	}
}
