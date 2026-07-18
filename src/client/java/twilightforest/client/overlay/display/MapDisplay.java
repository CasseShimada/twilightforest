package twilightforest.client.overlay.display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public final class MapDisplay implements ItemDisplay {
	private static final Identifier MAP_BACKGROUND_CHECKERBOARD =
		Identifier.withDefaultNamespace("textures/map/map_background_checkerboard.png");
	private static final int MAP_SIZE = 100;

	@Override
	public void render(ItemStack item, GuiGraphicsExtractor graphics, Minecraft minecraft, Gui gui, Player player, int widestWidgetWidth) {
		MapId mapId = item.get(DataComponents.MAP_ID);
		if (mapId == null) {
			return;
		}
		MapItemSavedData data = MapItem.getSavedData(item, minecraft.level);
		if (data == null) {
			return;
		}

		int start = Math.max(widestWidgetWidth / 2 - MAP_SIZE / 2, 0);
		graphics.blit(MAP_BACKGROUND_CHECKERBOARD, start, 0, start + MAP_SIZE, MAP_SIZE, 0.0F, 1.0F, 0.0F, 1.0F);
		MapRenderState state = new MapRenderState();
		minecraft.getMapRenderer().extractRenderState(mapId, data, state);
		graphics.pose().pushMatrix();
		graphics.pose().translate(start, 0.0F);
		graphics.pose().scale(MAP_SIZE / 128.0F, MAP_SIZE / 128.0F);
		graphics.map(state);
		graphics.pose().popMatrix();
	}

	@Override
	public DisplayPosition displayPosition() {
		return DisplayPosition.TOP;
	}

	@Override
	public Bounds getWidgetSize(ItemStack item, Minecraft minecraft, Gui gui, Player player, int widestWidgetWidth) {
		return new Bounds(Math.max(widestWidgetWidth / 2 - MAP_SIZE / 2, 0), 0, MAP_SIZE, MAP_SIZE + 2);
	}
}
