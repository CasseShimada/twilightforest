package twilightforest.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * Classifies a stack as one or more Traveller's Gear parts.
 *
 * <p>Classifiers are observational: they must not mutate the supplied stack and
 * must return a non-null set containing no null elements.</p>
 */
@FunctionalInterface
public interface TravellerGearClassifier {
	/**
	 * Observes a stack on the caller's logical side and reports zero or more
	 * Traveller's Gear parts. Runtime and linkage failures are isolated by
	 * {@link TravellerGearApi} and do not stop later classifiers.
	 *
	 * @param stack stack to observe without mutation
	 * @return non-null part set containing no null values
	 */
	Set<TravellerGearPart> classify(ItemStack stack);

	/**
	 * Reports namespaced effect IDs that are currently active for a recognized
	 * equipped stack. The default preserves this interface's single abstract
	 * method and reports no effects. Implementations are observational and run
	 * synchronously on the query caller's logical side.
	 *
	 * <p>The provider decides activity, including spectator and broken-item rules.
	 * Failures are isolated by {@link TravellerGearApi}. IDs are diagnostic/query
	 * values only and are not persisted or transmitted by the API.</p>
	 *
	 * @param wearer entity wearing the stack; do not mutate it
	 * @param equippedStack defensive snapshot of the recognized equipped stack
	 * @return non-null set of currently active, namespaced effect IDs
	 */
	default Set<Identifier> activeEffects(LivingEntity wearer, ItemStack equippedStack) {
		return Set.of();
	}
}
