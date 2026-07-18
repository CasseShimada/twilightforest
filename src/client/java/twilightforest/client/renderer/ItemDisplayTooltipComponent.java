package twilightforest.client.renderer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import twilightforest.TFRegistries;
import twilightforest.components.item.ItemDisplayContents;
import twilightforest.item.travellers_gear.TravellersGogglesItem;
import twilightforest.item.travellers_gear.modifiers.display.ItemDisplayType;

public final class ItemDisplayTooltipComponent implements ClientTooltipComponent {
	private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_background");
	private static final int SLOT_WIDTH = 18;
	private static final int SLOT_HEIGHT = 20;

	private final NonNullList<ItemStack> contents;

	public ItemDisplayTooltipComponent(TravellersGogglesItem.Tooltip tooltip) {
		this.contents = tooltip.contents().items();
	}

	@Override
	public void extractImage(Font font, int x, int y, int xOffs, int yOffs, GuiGraphicsExtractor graphics) {
		for (int index = 0; index < ItemDisplayContents.LAYOUT.size(); index++) {
			this.renderSlot(x + index * SLOT_WIDTH, y, index, graphics, font);
		}
	}

	private void renderSlot(int x, int y, int itemIndex, GuiGraphicsExtractor graphics, Font font) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, x, y, SLOT_WIDTH, SLOT_HEIGHT);
		ItemStack stack = itemIndex < this.contents.size() ? this.contents.get(itemIndex) : ItemStack.EMPTY;
		if (stack.isEmpty()) {
			this.renderBlankSlot(graphics, itemIndex, x, y);
			return;
		}
		graphics.item(stack, x + 1, y + 1, itemIndex);
		graphics.itemDecorations(font, stack, x + 1, y + 1);
	}

	private void renderBlankSlot(GuiGraphicsExtractor graphics, int index, int x, int y) {
		if (index < 0 || index >= ItemDisplayContents.LAYOUT.size()) {
			return;
		}
		ItemDisplayType type = TFRegistries.ITEM_DISPLAY_TYPE.getValue(ItemDisplayContents.LAYOUT.get(index));
		if (type != null) {
			type.slotTexture().ifPresent(texture -> graphics.blit(
				RenderPipelines.GUI_TEXTURED, texture, x + 1, y + 1, 0, 0, 16, 16, 16, 16));
		}
	}

	@Override
	public int getHeight(Font font) {
		return SLOT_HEIGHT + 4;
	}

	@Override
	public int getWidth(Font font) {
		return ItemDisplayContents.LAYOUT.size() * SLOT_WIDTH;
	}
}
