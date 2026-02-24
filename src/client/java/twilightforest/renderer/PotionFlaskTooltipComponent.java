package twilightforest.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.alchemy.PotionContents;
import twilightforest.TwilightForestMod;
import twilightforest.components.item.PotionFlaskComponent;
import twilightforest.item.BrittleFlaskItem;

import java.util.ArrayList;
import java.util.List;

public class PotionFlaskTooltipComponent implements ClientTooltipComponent {

	private static final Identifier BORDER_SPRITE = TwilightForestMod.prefix("flask_bar_border");
	private static final Identifier DOSE_SPRITE = TwilightForestMod.prefix("flask_dose_bar");
	private static final Component EMPTY_DESCRIPTION = Component.translatable("item.twilightforest.flask.empty_description");

	public static final int WIDTH = 115; //hehe

	private final PotionFlaskComponent component;
	private final int maxDoses;

	public PotionFlaskTooltipComponent(BrittleFlaskItem.Tooltip tooltip) {
		this.component = tooltip.component();
		this.maxDoses = tooltip.maxDoses();
	}

	@Override
	public int getHeight(Font font) {
		return this.getDescriptionHeight(font) + 13 + 8;
	}

	@Override
	public int getWidth(Font font) {
		return WIDTH;
	}

	private int getDescriptionHeight(Font font) {
		if (this.component.potion().potion().isPresent()) {
			var height = 0;
			for (var component : this.getPotionTooltips()) {
				if (component.getString().isEmpty()) {
					height += font.lineHeight;
				}
				height += font.split(component, this.getWidth(font)).size() * font.lineHeight + 1;
			}

			return height;
		}
		return font.split(EMPTY_DESCRIPTION, this.getWidth(font)).size() * font.lineHeight + 1;
	}

	private List<Component> getPotionTooltips() {
		if (this.component.potion().potion().isPresent()) {
			List<Component> tooltips = new ArrayList<>();
			PotionContents contents = this.component.potion();
			tooltips.add(contents.getName("item.minecraft.potion.effect."));
			PotionContents.addPotionTooltip(contents.getAllEffects(), tooltips::add, 1.0F, Minecraft.getInstance().level.tickRateManager().tickrate());
			return tooltips;
		}
		return List.of();
	}

	private int getContentXOffset(int offs) {
		return (offs - WIDTH) / 2;
	}

	@Override
	public void renderImage(Font font, int x, int y, int xOffs, int yOffs, GuiGraphics graphics) {
		if (this.component.potion().potion().isEmpty()) {
			graphics.drawWordWrap(font, EMPTY_DESCRIPTION, x, y, WIDTH, 11184810);
		} else {
			int height = 0;
			for (var component : this.getPotionTooltips()) {
				int color = component.getStyle().getColor() != null ? component.getStyle().getColor().getValue() : 11184810;
				if (component.getString().isEmpty()) {
					height += font.lineHeight;
				} else {
					graphics.drawWordWrap(font, component, x, y + height, WIDTH, color);
				}
				height += font.split(component, WIDTH).size() * font.lineHeight + 1;
			}
		}
		this.drawPotionBar(x, y + this.getDescriptionHeight(font) + 4, font, graphics);
	}

	private void drawPotionBar(int x, int y, Font font, GuiGraphics graphics) {
		int segmentSplit = this.getWidth(font) / this.maxDoses;
		if (this.component.doses() <= 0) {
			graphics.drawCenteredString(font, Component.translatable("item.twilightforest.flask.empty"), x + (WIDTH / 2) + 1, y + 3, 16777215);
		}

		int filledWidth = this.component.doses() * segmentSplit - 1;
		if (filledWidth > 0) {
			int color = 0xFF000000 | (this.component.potion().getColor() & 0xFFFFFF);
			graphics.fill(x + 1, y, x + 1 + filledWidth, y + 13, color);
		}
		if (this.component.breakage() > 0) {
			int xPos = x + segmentSplit * (3 - this.component.breakage());
			graphics.fill(xPos, y, xPos + (segmentSplit * this.component.breakage()), y + 13, 0xAA434343);
		}
		int widthProg = segmentSplit;
		for (int i = 1; i < this.maxDoses; i++) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DOSE_SPRITE, x + widthProg, y, 1, 13);
			widthProg += segmentSplit;
		}

		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BORDER_SPRITE, x, y, WIDTH, 13);
	}
}
