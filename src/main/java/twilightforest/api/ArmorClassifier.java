package twilightforest.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * Reports stable, namespaced traits for an armor stack.
 *
 * <p>Classifiers are observational: they must not mutate the supplied stack and
 * must return a non-null set containing no null elements.</p>
 */
@FunctionalInterface
public interface ArmorClassifier {
	/**
	 * Observes a stack on the caller's logical side and reports namespaced armor
	 * traits. Runtime and linkage failures are isolated by {@link ArmorApi} and do
	 * not stop later classifiers.
	 *
	 * @param stack stack to observe without mutation
	 * @return non-null trait set containing no null values
	 */
	Set<Identifier> classify(ItemStack stack);
}
