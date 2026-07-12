package twilightforest.init;

import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.advancements.predicate.ItemColorPredicate;

public class TFItemSubPredicates {
	public static final DataComponentPredicate.Type<ItemColorPredicate> COLOR = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE,
		TwilightForestMod.prefix("color"),
		new DataComponentPredicate.ConcreteType<>(ItemColorPredicate.CODEC)
	);

	public static void init() {
	}
}
