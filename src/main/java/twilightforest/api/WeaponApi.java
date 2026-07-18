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
 * Common trait-query API for weapon items.
 *
 * <p>This initial contract intentionally exposes classification only. It does
 * not let providers replace damage, cancel attacks, or mutate combat state.
 * Queries may run on either logical side and invoke classifiers synchronously
 * on the caller's thread. Traits are not persisted or transmitted.</p>
 */
public final class WeaponApi {
	/** Trait reported for each built-in weapon or combat tool recognized by this API. */
	public static final Identifier TWILIGHT_FOREST_WEAPON = trait("weapon");
	/** Main-hand behavior trait that ignites a successfully damaged target. */
	public static final Identifier IGNITES_TARGETS = trait("weapon/ignites_targets");
	/** Main-hand behavior trait that adds Knightmetal damage against armored targets. */
	public static final Identifier BONUS_AGAINST_ARMORED = trait("weapon/bonus_against_armored");
	/** Main-hand behavior trait that adds Knightmetal damage against unarmored targets. */
	public static final Identifier BONUS_AGAINST_UNARMORED = trait("weapon/bonus_against_unarmored");
	/** Main-hand behavior trait that adds the sprinting Minotaur charge bonus. */
	public static final Identifier SPRINT_CHARGE_BONUS = trait("weapon/sprint_charge_bonus");
	/** Tool behavior trait that smelts eligible block drops. */
	public static final Identifier SMELTS_BLOCK_DROPS = trait("weapon/smelts_block_drops");
	/** Tool behavior trait exempting the stack from extra Mazestone/Castle wear. */
	public static final Identifier MAZESTONE_WEAR_EXEMPT = trait("weapon/mazestone_wear_exempt");
	/** Used-item behavior trait that clears arrow invulnerability between multishot hits. */
	public static final Identifier RESETS_ARROW_INVULNERABILITY = trait("weapon/resets_arrow_invulnerability");

	private static final String EXTENSION_POINT = "weapon trait classification";
	private static final ProviderRegistry<WeaponClassifier> CLASSIFIERS = new ProviderRegistry<>(EXTENSION_POINT);
	private static final ThreadLocal<Boolean> DISPATCHING = ThreadLocal.withInitial(() -> Boolean.FALSE);

	private WeaponApi() {
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
	public static void registerClassifier(Identifier id, WeaponClassifier classifier) {
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
	 * Returns the immutable, lexicographically ordered union of weapon traits.
	 * Providers run synchronously on the caller's logical side with a defensive
	 * stack snapshot; runtime and linkage failures are logged with the provider ID
	 * and skipped. A recursive weapon query on the same thread returns an empty set
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
			for (ProviderRegistry.Entry<WeaponClassifier> entry : CLASSIFIERS.entries()) {
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
	 * Returns whether the stack has the requested weapon trait, using the same
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
	 * Returns whether weapon registrations are frozen. This thread-safe query does
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
