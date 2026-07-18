package twilightforest.client.renderer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import twilightforest.item.travellers_gear.TravellersArmorBeltItem;

import java.util.List;

public final class TravellersBeltTooltipComponent implements ClientTooltipComponent {
	private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_background");
	private static final int SLOT_WIDTH = 18;
	private static final int SLOT_HEIGHT = 20;
	private static final int SLOT_COUNT = 9;

	private final List<ItemStack> contents;

	public TravellersBeltTooltipComponent(TravellersArmorBeltItem.Tooltip tooltip) {
		ItemContainerContents container = tooltip.contents();
		NonNullList<ItemStack> stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
		container.copyInto(stacks);
		this.contents = stacks;
	}

	@Override
	public void extractImage(Font font, int x, int y, int xOffs, int yOffs, GuiGraphicsExtractor graphics) {
		for (int index = 0; index < SLOT_COUNT; index++) {
			int renderX = x + index * SLOT_WIDTH;
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, renderX, y, SLOT_WIDTH, SLOT_HEIGHT);
			ItemStack stack = this.contents.get(index);
			if (!stack.isEmpty()) {
				graphics.item(stack, renderX + 1, y + 1, index);
				graphics.itemDecorations(font, stack, renderX + 1, y + 1);
			}
		}
	}

	@Override
	public int getHeight(Font font) {
		return SLOT_HEIGHT + 4;
	}

	@Override
	public int getWidth(Font font) {
		return SLOT_COUNT * SLOT_WIDTH;
	}
}
