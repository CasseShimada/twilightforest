package twilightforest.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Common trait-query API for armor items.
 *
 * <p>Queries may run on either logical side and synchronously invoke classifiers
 * on the caller's thread. Traits are not persisted or transmitted.</p>
 */
public final class ArmorApi {
	/** Trait reported for each built-in armor piece recognized by this API. */
	public static final Identifier TWILIGHT_FOREST_ARMOR = trait("armor");
	/** Material/category trait for Naga Scale armor. */
	public static final Identifier NAGA = trait("armor/naga");
	/** Material/category trait for Ironwood armor. */
	public static final Identifier IRONWOOD = trait("armor/ironwood");
	/** Material/category trait for Steeleaf armor. */
	public static final Identifier STEELEAF = trait("armor/steeleaf");
	/** Material/category trait for Fiery armor. */
	public static final Identifier FIERY = trait("armor/fiery");
	/** Material/category trait for Knightmetal armor. */
	public static final Identifier KNIGHTMETAL = trait("armor/knightmetal");
	/** Material/category trait for Phantom armor. */
	public static final Identifier PHANTOM = trait("armor/phantom");
	/** Material/category trait for Arctic armor. */
	public static final Identifier ARCTIC = trait("armor/arctic");
	/** Material/category trait for Yeti armor. */
	public static final Identifier YETI = trait("armor/yeti");
	/** Category trait for Traveller's Gear pieces that occupy armor slots. */
	public static final Identifier TRAVELLERS_GEAR = trait("armor/travellers_gear");
	/**
	 * Server-side behavior trait whose equipped pieces may ignite an attacker
	 * after the wearer takes damage.
	 */
	public static final Identifier FIERY_REACTIVE = trait("armor/fiery_reactive");
	/** Server-side behavior trait whose equipped pieces apply the chill aura. */
	public static final Identifier CHILL_AURA = trait("armor/chill_aura");
	/** Server-side behavior trait that prevents Fiery Block step damage. */
	public static final Identifier FIERY_STEP_IMMUNITY = trait("armor/fiery_step_immunity");

	private static final String EXTENSION_POINT = "armor trait classification";
	private static final ProviderRegistry<ArmorClassifier> CLASSIFIERS = new ProviderRegistry<>(EXTENSION_POINT);
	private static final ThreadLocal<Boolean> DISPATCHING = ThreadLocal.withInitial(() -> Boolean.FALSE);

	private ArmorApi() {
	}

	/**
	 * Registers an observational classifier under a stable, namespaced ID. Call
	 * this from a common mod initializer before the server-start freeze.
	 *
	 * @param id stable provider ID owned by the registering mod
	 * @param classifier observational classifier
	 *
	 * @throws IllegalArgumentException when the ID is already registered
	 * @throws IllegalStateException when registrations are frozen
	 */
	public static void registerClassifier(Identifier id, ArmorClassifier classifier) {
		CLASSIFIERS.register(id, classifier);
	}

	/**
	 * Returns classifier IDs in an immutable, deterministic lexicographic
	 * snapshot without invoking classifiers.
	 *
	 * @return immutable ordered provider-ID snapshot
	 */
	public static List<Identifier> classifierIds() {
		return CLASSIFIERS.ids();
	}

	/**
	 * Returns the immutable, lexicographically ordered union of armor traits.
	 * Providers run synchronously on the caller's logical side with a defensive
	 * stack snapshot; runtime and linkage failures are logged with the provider ID
	 * and skipped. A recursive armor query on the same thread returns an empty set
	 * without invoking classifiers again.
	 *
	 * @param stack stack to classify without mutation
	 * @return immutable ordered union of trait IDs, or an empty set during
	 * recursive dispatch
	 */
	public static Set<Identifier> traits(ItemStack stack) {
		Objects.requireNonNull(stack, "stack");
		if (DISPATCHING.get()) {
			return Set.of();
		}

		DISPATCHING.set(Boolean.TRUE);
		try {
			TreeSet<Identifier> traits = new TreeSet<>((left, right) -> left.toString().compareTo(right.toString()));
			for (ProviderRegistry.Entry<ArmorClassifier> entry : CLASSIFIERS.entries()) {
				try {
					Set<Identifier> classified = Objects.requireNonNull(entry.provider().classify(stack.copy()), "classifier result");
					TreeSet<Identifier> validated = new TreeSet<>((left, right) -> left.toString().compareTo(right.toString()));
					for (Identifier trait : classified) {
						validated.add(Objects.requireNonNull(trait, "classified trait"));
					}
					traits.addAll(validated);
				} catch (RuntimeException | LinkageError failure) {
					ApiLog.providerFailure(EXTENSION_POINT, entry.id(), failure);
				}
			}
			return traits.isEmpty() ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(traits));
		} finally {
			DISPATCHING.remove();
		}
	}

	/**
	 * Returns whether the stack has the requested armor trait, using the same
	 * caller-thread and failure-isolation rules as {@link #traits(ItemStack)}.
	 *
	 * @param stack stack to classify without mutation
	 * @param trait trait ID to find
	 * @return true when any classifier reports the trait
	 */
	public static boolean hasTrait(ItemStack stack, Identifier trait) {
		Objects.requireNonNull(trait, "trait");
		return traits(stack).contains(trait);
	}

	/**
	 * Returns whether armor registrations are frozen. This thread-safe query does
	 * not invoke classifiers.
	 *
	 * @return true after the classifier registry has frozen
	 */
	public static boolean registrationsFrozen() {
		return CLASSIFIERS.isFrozen();
	}

	static void freezeRegistrations() {
		CLASSIFIERS.freeze();
	}

	static void resetForTests() {
		CLASSIFIERS.resetForTests();
		DISPATCHING.remove();
	}

	private static Identifier trait(String path) {
		return Identifier.fromNamespaceAndPath("twilightforest", path);
	}
}
