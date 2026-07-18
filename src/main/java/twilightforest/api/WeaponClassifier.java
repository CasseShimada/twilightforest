package twilightforest.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * Reports stable, namespaced traits for a weapon stack.
 *
 * <p>Classifiers are observational: they must not mutate the supplied stack and
 * must return a non-null set containing no null elements.</p>
 */
@FunctionalInterface
public interface WeaponClassifier {
	/**
	 * Observes a stack on the caller's logical side and reports namespaced weapon
	 * traits. Runtime and linkage failures are isolated by {@link WeaponApi} and do
	 * not stop later classifiers.
	 *
	 * @param stack stack to observe without mutation
	 * @return non-null trait set containing no null values
	 */
	Set<Identifier> classify(ItemStack stack);
}
