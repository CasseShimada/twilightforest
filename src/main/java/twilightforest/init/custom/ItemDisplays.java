package twilightforest.init.custom;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.components.item.ItemDisplayContents;
import twilightforest.init.TFItems;
import twilightforest.item.travellers_gear.modifiers.display.ItemDisplayType;

import java.util.Optional;

public final class ItemDisplays {
	public static final ItemDisplayType MAP = register(ItemDisplayContents.MAP_ID, new ItemDisplayType(stack -> stack.getItem() instanceof MapItem, slotTexture("map_display")));
	public static final ItemDisplayType COMPASS = register(ItemDisplayContents.COMPASS_ID, new ItemDisplayType(stack -> stack.is(Items.COMPASS), slotTexture("compass_display")));
	public static final ItemDisplayType CLOCK = register(ItemDisplayContents.CLOCK_ID, new ItemDisplayType(stack -> stack.is(Items.CLOCK), slotTexture("clock_display")));
	public static final ItemDisplayType MOON_DIAL = register(ItemDisplayContents.MOON_DIAL_ID, new ItemDisplayType(stack -> stack.is(TFItems.MOON_DIAL), slotTexture("moon_dial_display")));

	private ItemDisplays() {
	}

	private static Optional<Identifier> slotTexture(String name) {
		return Optional.of(TwilightForestMod.prefix("textures/item/" + name + ".png"));
	}

	private static ItemDisplayType register(Identifier id, ItemDisplayType type) {
		return Registry.register(TFRegistries.ITEM_DISPLAY_TYPE, id, type);
	}

	public static void init() {
	}
}
