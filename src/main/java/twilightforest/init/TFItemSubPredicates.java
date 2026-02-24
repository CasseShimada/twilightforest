package twilightforest.init;

import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import twilightforest.util.registry.DeferredHolder;
import twilightforest.util.registry.DeferredRegister;
import twilightforest.TwilightForestMod;
import twilightforest.advancements.predicate.ItemColorPredicate;

public class TFItemSubPredicates {

	public static final DeferredRegister<DataComponentPredicate.Type<?>> TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_PREDICATE_TYPE, TwilightForestMod.ID);

	public static final DeferredHolder<DataComponentPredicate.Type<?>, DataComponentPredicate.Type<ItemColorPredicate>> COLOR = TYPES.register("color", () -> new DataComponentPredicate.ConcreteType<>(ItemColorPredicate.CODEC));
}
