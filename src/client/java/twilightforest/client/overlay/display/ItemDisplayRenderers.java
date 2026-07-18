package twilightforest.client.overlay.display;

import net.minecraft.resources.Identifier;
import twilightforest.components.item.ItemDisplayContents;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class ItemDisplayRenderers {
	private static final Map<Identifier, Supplier<? extends ItemDisplay>> FACTORIES = new HashMap<>();

	private ItemDisplayRenderers() {
	}

	public static void init() {
		if (!FACTORIES.isEmpty()) {
			return;
		}
		register(ItemDisplayContents.MAP_ID, MapDisplay::new);
		register(ItemDisplayContents.COMPASS_ID, CompassDisplay::new);
		register(ItemDisplayContents.CLOCK_ID, ClockDisplay::new);
		register(ItemDisplayContents.MOON_DIAL_ID, MoonDialDisplay::new);
	}

	public static Optional<ItemDisplay> create(Identifier id) {
		Supplier<? extends ItemDisplay> factory = FACTORIES.get(id);
		return factory == null ? Optional.empty() : Optional.of(factory.get());
	}

	private static void register(Identifier id, Supplier<? extends ItemDisplay> factory) {
		Supplier<? extends ItemDisplay> previous = FACTORIES.putIfAbsent(id, factory);
		if (previous != null) {
			throw new IllegalStateException("Duplicate item display renderer for " + id);
		}
	}
}
