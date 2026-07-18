package twilightforest.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import twilightforest.TFRegistries;
import twilightforest.client.overlay.display.ItemDisplay;
import twilightforest.client.overlay.display.ItemDisplayRenderers;
import twilightforest.components.item.ItemDisplayContents;
import twilightforest.config.TFConfig;
import twilightforest.init.TFDataComponents;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.modifiers.display.ItemDisplayType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ItemDisplayOverlay {
	private ItemDisplayOverlay() {
	}

	public static void render(GuiGraphicsExtractor graphics, Minecraft minecraft, Gui gui, Player player) {
		if (gui.hud.isHidden() || gui.hud.getDebugOverlay().showDebugScreen()) {
			return;
		}

		ItemStack goggles = player.getItemBySlot(EquipmentSlot.HEAD);
		if (!TravellersModifiersManager.isModifierActive(player, goggles, TravellersModifiersManager.ITEM_DISPLAY_MODIFIER)) {
			return;
		}
		ItemDisplayContents contents = goggles.get(TFDataComponents.ITEM_DISPLAY);
		if (contents == null || contents.isEmpty()) {
			return;
		}

		List<DisplayHolder> displays = new ArrayList<>();
		int widest = fillDisplayHolders(displays, contents, minecraft, gui, player);
		if (!displays.isEmpty()) {
			renderHolders(graphics, minecraft, gui, player, displays, widest);
		}
	}

	private static int fillDisplayHolders(List<DisplayHolder> displays, ItemDisplayContents contents,
									  Minecraft minecraft, Gui gui, Player player) {
		int widest = 0;
		int slots = Math.min(ItemDisplayContents.LAYOUT.size(), contents.items().size());
		int activeMapSlot = contents.findActiveMapSlot();
		for (int i = 0; i < slots; i++) {
			ItemStack stack = contents.items().get(i);
			Identifier typeId = ItemDisplayContents.LAYOUT.get(i);
			if (stack.isEmpty() || typeId.equals(ItemDisplayContents.MAP_ID) && i != activeMapSlot) {
				continue;
			}
			ItemDisplayType type = TFRegistries.ITEM_DISPLAY_TYPE.getValue(typeId);
			if (type == null || !type.validItems().test(stack)) {
				continue;
			}
			ItemDisplay display = ItemDisplayRenderers.create(typeId).orElse(null);
			if (display == null) {
				continue;
			}
			ItemDisplay.Bounds bounds = display.getWidgetSize(stack, minecraft, gui, player, widest);
			widest = Math.max(widest, bounds.width());
			displays.add(new DisplayHolder(stack, display, bounds));
		}
		return widest;
	}

	private static void renderHolders(GuiGraphicsExtractor graphics, Minecraft minecraft, Gui gui, Player player,
								  List<DisplayHolder> displays, int widest) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(TFConfig.itemDisplayXOffs, TFConfig.itemDisplayYOffs);
		float scale = (float) TFConfig.itemDisplayScale;
		graphics.pose().scale(scale, scale);
		displays.sort(Comparator.comparing(holder -> holder.display().displayPosition()));
		for (DisplayHolder holder : displays) {
			graphics.pose().pushMatrix();
			holder.display().render(holder.stack(), graphics, minecraft, gui, player, widest);
			graphics.pose().popMatrix();
			graphics.pose().translate(0.0F, holder.bounds().height());
		}
		graphics.pose().popMatrix();
	}

	private record DisplayHolder(ItemStack stack, ItemDisplay display, ItemDisplay.Bounds bounds) {
	}
}
