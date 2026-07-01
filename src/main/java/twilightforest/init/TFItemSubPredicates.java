package twilightforest.init;

import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.advancements.predicate.ItemColorPredicate;

public class TFItemSubPredicates {

	public static final DataComponentPredicate.Type<ItemColorPredicate> COLOR = new DataComponentPredicate.ConcreteType<>(ItemColorPredicate.CODEC);
	private static boolean registered;

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		Registry.register(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, TwilightForestMod.prefix("color"), COLOR);
	}
}
