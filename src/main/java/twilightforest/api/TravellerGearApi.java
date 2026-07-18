package twilightforest.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Common classification API for Traveller's Gear.
 *
 * <p>Queries may run on either logical side and synchronously invoke classifiers
 * on the caller's thread. This API does not persist classifications or send
 * network payloads.</p>
 */
public final class TravellerGearApi {
	private static final String EXTENSION_POINT = "Traveller's Gear classification";
	private static final String ACTIVE_EFFECT_EXTENSION_POINT = "Traveller's Gear active-effect query";
	private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(
		EquipmentSlot.HEAD,
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET
	);
	private static final ProviderRegistry<TravellerGearClassifier> CLASSIFIERS = new ProviderRegistry<>(EXTENSION_POINT);
	private static final ThreadLocal<Boolean> DISPATCHING = ThreadLocal.withInitial(() -> Boolean.FALSE);

	private TravellerGearApi() {
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
	public static void registerClassifier(Identifier id, TravellerGearClassifier classifier) {
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
	 * Returns the immutable union of parts reported by all classifiers. Providers
	 * run synchronously on the caller's logical side; runtime and linkage failures
	 * are logged with the provider ID and skipped. Each provider receives a
	 * defensive stack snapshot. A recursive Traveller's Gear query on the same
	 * thread returns an empty set without invoking classifiers again.
	 *
	 * @param stack stack to classify without mutation
	 * @return immutable union ordered by enum declaration, or an empty set during
	 * recursive dispatch
	 */
	public static Set<TravellerGearPart> parts(ItemStack stack) {
		Objects.requireNonNull(stack, "stack");
		if (DISPATCHING.get()) {
			return Set.of();
		}

		DISPATCHING.set(Boolean.TRUE);
		try {
			EnumSet<TravellerGearPart> parts = EnumSet.noneOf(TravellerGearPart.class);
			for (ProviderRegistry.Entry<TravellerGearClassifier> entry : CLASSIFIERS.entries()) {
				try {
					parts.addAll(validateParts(entry.provider().classify(stack.copy())));
				} catch (RuntimeException | LinkageError failure) {
					ApiLog.providerFailure(EXTENSION_POINT, entry.id(), failure);
				}
			}
			return parts.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(parts));
		} finally {
			DISPATCHING.remove();
		}
	}

	/**
	 * Returns whether any classifier recognizes the stack as Traveller's Gear,
	 * using the same caller-thread and failure-isolation rules as
	 * {@link #parts(ItemStack)}.
	 *
	 * @param stack stack to classify without mutation
	 * @return true when at least one part is reported
	 */
	public static boolean isTravellerGear(ItemStack stack) {
		return !parts(stack).isEmpty();
	}

	/**
	 * Returns whether a recognized Traveller's Gear stack is at its disabled
	 * durability threshold. The threshold matches existing gear behavior: a
	 * damageable stack is broken at maximum damage minus one. Classifiers run on
	 * the caller's logical side and failures are isolated as in
	 * {@link #parts(ItemStack)}.
	 *
	 * @param stack stack to inspect without mutation
	 * @return true only for recognized gear at the broken threshold
	 */
	public static boolean isBroken(ItemStack stack) {
		Objects.requireNonNull(stack, "stack");
		return isTravellerGear(stack)
			&& stack.isDamageableItem()
			&& stack.getMaxDamage() - 1 <= stack.getDamageValue();
	}

	/**
	 * Returns a defensive snapshot of the entity's equipped stack for one part.
	 * A stack is returned only when a classifier reports that part in its fixed
	 * armor slot; otherwise this returns an empty stack. The query runs on the
	 * caller's logical side and does not persist or synchronize the snapshot.
	 *
	 * @param wearer entity whose equipment should be queried
	 * @param part fixed Traveller's Gear part
	 * @return defensive stack snapshot, or an empty stack
	 */
	public static ItemStack equippedStack(LivingEntity wearer, TravellerGearPart part) {
		Objects.requireNonNull(wearer, "wearer");
		Objects.requireNonNull(part, "part");
		ItemStack equipped = Objects.requireNonNull(wearer.getItemBySlot(part.equipmentSlot()), "equipped stack");
		return parts(equipped).contains(part) ? equipped.copy() : ItemStack.EMPTY;
	}

	/**
	 * Returns defensive snapshots of all recognized Traveller's Gear currently
	 * equipped by an entity. The immutable map is ordered by the six-part enum;
	 * merged vest/gloves and wings/belt entries receive independent copies of
	 * their shared equipped stack. Providers run synchronously on the caller's
	 * logical side. Nothing is persisted or transmitted.
	 *
	 * @param wearer entity whose four armor slots should be queried
	 * @return immutable map from recognized parts to defensive stack snapshots
	 */
	public static Map<TravellerGearPart, ItemStack> equippedParts(LivingEntity wearer) {
		Objects.requireNonNull(wearer, "wearer");
		EnumMap<TravellerGearPart, ItemStack> equippedParts = new EnumMap<>(TravellerGearPart.class);
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			ItemStack equipped = Objects.requireNonNull(wearer.getItemBySlot(slot), "equipped stack");
			if (equipped.isEmpty()) {
				continue;
			}
			for (TravellerGearPart part : parts(equipped)) {
				if (part.equipmentSlot() == slot) {
					equippedParts.put(part, equipped.copy());
				}
			}
		}
		return equippedParts.isEmpty()
			? Map.of()
			: Collections.unmodifiableMap(new EnumMap<>(equippedParts));
	}

	/**
	 * Returns the immutable, lexicographically ordered union of namespaced effect
	 * IDs that classifiers report as active across recognized equipped gear.
	 * Classifiers run synchronously on the caller's logical side. Runtime and
	 * linkage failures are logged with the provider ID and skipped. IDs are not
	 * written to saved data or sent over the network. A recursive Traveller's Gear
	 * query on the same thread returns an empty set without invoking classifiers
	 * again.
	 *
	 * @param wearer entity whose equipped Traveller effects should be queried
	 * @return immutable ordered set of active effect IDs, or an empty set during
	 * recursive dispatch
	 */
	public static Set<Identifier> activeEffectIds(LivingEntity wearer) {
		Objects.requireNonNull(wearer, "wearer");
		if (DISPATCHING.get()) {
			return Set.of();
		}

		DISPATCHING.set(Boolean.TRUE);
		try {
			TreeSet<Identifier> activeEffects = new TreeSet<>((left, right) -> left.toString().compareTo(right.toString()));
			for (EquipmentSlot slot : ARMOR_SLOTS) {
				ItemStack equipped = Objects.requireNonNull(wearer.getItemBySlot(slot), "equipped stack");
				if (equipped.isEmpty()) {
					continue;
				}
				for (ProviderRegistry.Entry<TravellerGearClassifier> entry : CLASSIFIERS.entries()) {
					try {
						EnumSet<TravellerGearPart> recognized = validateParts(entry.provider().classify(equipped.copy()));
						if (recognized.stream().noneMatch(part -> part.equipmentSlot() == slot)) {
							continue;
						}
						Set<Identifier> reported = Objects.requireNonNull(
							entry.provider().activeEffects(wearer, equipped.copy()),
							"active-effect result"
						);
						TreeSet<Identifier> validated = new TreeSet<>((left, right) -> left.toString().compareTo(right.toString()));
						for (Identifier effect : reported) {
							validated.add(Objects.requireNonNull(effect, "active effect ID"));
						}
						activeEffects.addAll(validated);
					} catch (RuntimeException | LinkageError failure) {
						ApiLog.providerFailure(ACTIVE_EFFECT_EXTENSION_POINT, entry.id(), failure);
					}
				}
			}
			return activeEffects.isEmpty()
				? Set.of()
				: Collections.unmodifiableSet(new LinkedHashSet<>(activeEffects));
		} finally {
			DISPATCHING.remove();
		}
	}

	/**
	 * Returns whether Traveller's Gear registrations are frozen. This thread-safe
	 * query does not invoke classifiers.
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

	private static EnumSet<TravellerGearPart> validateParts(Set<TravellerGearPart> classified) {
		Objects.requireNonNull(classified, "classifier result");
		EnumSet<TravellerGearPart> validated = EnumSet.noneOf(TravellerGearPart.class);
		for (TravellerGearPart part : classified) {
			validated.add(Objects.requireNonNull(part, "classified part"));
		}
		return validated;
	}
}
