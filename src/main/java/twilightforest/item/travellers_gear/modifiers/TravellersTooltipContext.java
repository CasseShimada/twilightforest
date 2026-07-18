package twilightforest.item.travellers_gear.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public final class TravellersTooltipContext {
	private static volatile BooleanSupplier shiftDown = () -> false;
	private static volatile Function<String, MutableComponent> translationResolver = Component::translatable;

	private TravellersTooltipContext() {
	}

	public static boolean hasShiftDown() {
		return shiftDown.getAsBoolean();
	}

	public static MutableComponent translate(String key) {
		return translationResolver.apply(key);
	}

	public static void registerClient(BooleanSupplier shiftDown, Function<String, MutableComponent> translationResolver) {
		TravellersTooltipContext.shiftDown = Objects.requireNonNull(shiftDown);
		TravellersTooltipContext.translationResolver = Objects.requireNonNull(translationResolver);
	}
}
