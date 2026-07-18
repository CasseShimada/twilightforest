package twilightforest.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import twilightforest.test.MinecraftBootstrapExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MinecraftBootstrapExtension.class)
class ClassificationApiContractTests {
	@BeforeAll
	static void bindFixtureItemComponents() {
		ApiTestItems.bindStackable(Items.STICK);
		ApiTestItems.bindDamageable(
			Items.LEATHER_HELMET,
			Items.LEATHER_CHESTPLATE,
			Items.LEATHER_LEGGINGS,
			Items.LEATHER_BOOTS,
			Items.IRON_HELMET,
			Items.IRON_CHESTPLATE,
			Items.IRON_SWORD
		);
	}

	@AfterEach
	void resetRegistrations() {
		TwilightForestApi.resetForTests();
	}

	@Test
	void travellerPartsAreUnionedInEnumOrderAndImmutable() {
		TravellerGearApi.registerClassifier(id("wings"), stack -> Set.of(TravellerGearPart.WINGS));
		TravellerGearApi.registerClassifier(id("head_and_boots"), stack ->
			Set.of(TravellerGearPart.BOOTS, TravellerGearPart.GOGGLES));

		Set<TravellerGearPart> parts = TravellerGearApi.parts(new ItemStack(Items.LEATHER_BOOTS));
		assertEquals(List.of(TravellerGearPart.GOGGLES, TravellerGearPart.WINGS, TravellerGearPart.BOOTS),
			new ArrayList<>(parts));
		assertTrue(TravellerGearApi.isTravellerGear(new ItemStack(Items.LEATHER_BOOTS)));
		assertThrows(UnsupportedOperationException.class, () -> parts.add(TravellerGearPart.BELT));
	}

	@Test
	void emptyTravellerRegistryDoesNotClassify() {
		assertFalse(TravellerGearApi.isTravellerGear(new ItemStack(Items.STICK)));
	}

	@Test
	void travellerEquipmentMappingReturnsIndependentDefensiveSnapshots() {
		ItemStack head = new ItemStack(Items.LEATHER_HELMET);
		ItemStack chest = new ItemStack(Items.LEATHER_CHESTPLATE);
		ItemStack legs = new ItemStack(Items.LEATHER_LEGGINGS);
		ItemStack feet = new ItemStack(Items.LEATHER_BOOTS);
		LivingEntity wearer = wearerWith(head, chest, legs, feet);
		TravellerGearApi.registerClassifier(id("six_parts"), stack -> {
			if (stack.is(Items.LEATHER_HELMET)) {
				return Set.of(TravellerGearPart.GOGGLES);
			}
			if (stack.is(Items.LEATHER_CHESTPLATE)) {
				return Set.of(TravellerGearPart.VEST, TravellerGearPart.GLOVES);
			}
			if (stack.is(Items.LEATHER_LEGGINGS)) {
				return Set.of(TravellerGearPart.WINGS, TravellerGearPart.BELT);
			}
			if (stack.is(Items.LEATHER_BOOTS)) {
				return Set.of(TravellerGearPart.BOOTS);
			}
			return Set.of();
		});

		assertEquals(EquipmentSlot.HEAD, TravellerGearPart.GOGGLES.equipmentSlot());
		assertEquals(EquipmentSlot.CHEST, TravellerGearPart.VEST.equipmentSlot());
		assertEquals(EquipmentSlot.CHEST, TravellerGearPart.GLOVES.equipmentSlot());
		assertEquals(EquipmentSlot.LEGS, TravellerGearPart.WINGS.equipmentSlot());
		assertEquals(EquipmentSlot.LEGS, TravellerGearPart.BELT.equipmentSlot());
		assertEquals(EquipmentSlot.FEET, TravellerGearPart.BOOTS.equipmentSlot());

		Map<TravellerGearPart, ItemStack> equipped = TravellerGearApi.equippedParts(wearer);
		assertEquals(List.of(
			TravellerGearPart.GOGGLES,
			TravellerGearPart.VEST,
			TravellerGearPart.GLOVES,
			TravellerGearPart.WINGS,
			TravellerGearPart.BELT,
			TravellerGearPart.BOOTS
		), new ArrayList<>(equipped.keySet()));
		assertNotSame(equipped.get(TravellerGearPart.VEST), equipped.get(TravellerGearPart.GLOVES));
		assertThrows(UnsupportedOperationException.class, equipped::clear);

		equipped.get(TravellerGearPart.GOGGLES).setCount(4);
		assertEquals(1, head.getCount());
		ItemStack directSnapshot = TravellerGearApi.equippedStack(wearer, TravellerGearPart.GOGGLES);
		directSnapshot.setCount(3);
		assertEquals(1, head.getCount());
	}

	@Test
	void travellerBrokenThresholdRequiresRecognizedDamageableGear() {
		ItemStack helmet = new ItemStack(Items.IRON_HELMET);
		TravellerGearApi.registerClassifier(id("helmet"), stack ->
			stack.is(Items.IRON_HELMET) ? Set.of(TravellerGearPart.GOGGLES) : Set.of());

		helmet.setDamageValue(helmet.getMaxDamage() - 2);
		assertFalse(TravellerGearApi.isBroken(helmet));
		helmet.setDamageValue(helmet.getMaxDamage() - 1);
		assertTrue(TravellerGearApi.isBroken(helmet));
		assertFalse(TravellerGearApi.isBroken(new ItemStack(Items.STICK)));
	}

	@Test
	void activeEffectIdsAreProviderIsolatedSortedAndImmutable() {
		Identifier alpha = id("alpha_effect");
		Identifier zeta = id("zeta_effect");
		LivingEntity wearer = wearerWith(new ItemStack(Items.IRON_HELMET), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
		TravellerGearApi.registerClassifier(id("alpha_failure"), new TravellerGearClassifier() {
			/** {@inheritDoc} */
			@Override
			public Set<TravellerGearPart> classify(ItemStack stack) {
				return stack.is(Items.IRON_HELMET) ? Set.of(TravellerGearPart.GOGGLES) : Set.of();
			}

			/** {@inheritDoc} */
			@Override
			public Set<Identifier> activeEffects(LivingEntity entity, ItemStack equippedStack) {
				throw new IllegalStateException("expected contract-test failure");
			}
		});
		TravellerGearApi.registerClassifier(id("beta_success"), new TravellerGearClassifier() {
			/** {@inheritDoc} */
			@Override
			public Set<TravellerGearPart> classify(ItemStack stack) {
				return stack.is(Items.IRON_HELMET) ? Set.of(TravellerGearPart.GOGGLES) : Set.of();
			}

			/** {@inheritDoc} */
			@Override
			public Set<Identifier> activeEffects(LivingEntity entity, ItemStack equippedStack) {
				return Set.of(zeta, alpha);
			}
		});
		AtomicInteger unrecognizedCalls = new AtomicInteger();
		TravellerGearApi.registerClassifier(id("zeta_unrecognized"), new TravellerGearClassifier() {
			/** {@inheritDoc} */
			@Override
			public Set<TravellerGearPart> classify(ItemStack stack) {
				return Set.of();
			}

			/** {@inheritDoc} */
			@Override
			public Set<Identifier> activeEffects(LivingEntity entity, ItemStack equippedStack) {
				unrecognizedCalls.incrementAndGet();
				return Set.of(id("must_not_appear"));
			}
		});

		Set<Identifier> effects = TravellerGearApi.activeEffectIds(wearer);
		assertEquals(List.of(alpha, zeta), new ArrayList<>(effects));
		assertEquals(0, unrecognizedCalls.get());
		assertThrows(UnsupportedOperationException.class, effects::clear);
	}

	@Test
	void recursiveClassifierQueriesReturnEmptyWithoutReinvokingProviders() {
		Identifier armorTrait = id("recursive_armor");
		AtomicInteger armorCalls = new AtomicInteger();
		ArmorApi.registerClassifier(id("recursive_armor_provider"), stack -> {
			armorCalls.incrementAndGet();
			assertTrue(ArmorApi.traits(stack).isEmpty());
			return Set.of(armorTrait);
		});

		assertEquals(Set.of(armorTrait), ArmorApi.traits(new ItemStack(Items.IRON_CHESTPLATE)));
		assertEquals(1, armorCalls.get());

		Identifier weaponTrait = id("recursive_weapon");
		AtomicInteger weaponCalls = new AtomicInteger();
		WeaponApi.registerClassifier(id("recursive_weapon_provider"), stack -> {
			weaponCalls.incrementAndGet();
			assertTrue(WeaponApi.traits(stack).isEmpty());
			return Set.of(weaponTrait);
		});

		assertEquals(Set.of(weaponTrait), WeaponApi.traits(new ItemStack(Items.IRON_SWORD)));
		assertEquals(1, weaponCalls.get());

		AtomicInteger travellerCalls = new AtomicInteger();
		TravellerGearApi.registerClassifier(id("recursive_traveller_provider"), stack -> {
			travellerCalls.incrementAndGet();
			assertTrue(TravellerGearApi.parts(stack).isEmpty());
			return Set.of(TravellerGearPart.GOGGLES);
		});

		assertEquals(Set.of(TravellerGearPart.GOGGLES),
			TravellerGearApi.parts(new ItemStack(Items.IRON_HELMET)));
		assertEquals(1, travellerCalls.get());
	}

	@Test
	void recursiveTravellerEffectQueriesReturnEmptyWithoutReinvokingProviders() {
		Identifier effect = id("recursive_effect");
		AtomicInteger classifierCalls = new AtomicInteger();
		AtomicInteger effectCalls = new AtomicInteger();
		LivingEntity wearer = wearerWith(new ItemStack(Items.IRON_HELMET), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
		TravellerGearApi.registerClassifier(id("recursive_effect_provider"), new TravellerGearClassifier() {
			/** {@inheritDoc} */
			@Override
			public Set<TravellerGearPart> classify(ItemStack stack) {
				classifierCalls.incrementAndGet();
				assertTrue(TravellerGearApi.parts(stack).isEmpty());
				return Set.of(TravellerGearPart.GOGGLES);
			}

			/** {@inheritDoc} */
			@Override
			public Set<Identifier> activeEffects(LivingEntity entity, ItemStack equippedStack) {
				effectCalls.incrementAndGet();
				assertTrue(TravellerGearApi.activeEffectIds(entity).isEmpty());
				return Set.of(effect);
			}
		});

		assertEquals(Set.of(effect), TravellerGearApi.activeEffectIds(wearer));
		assertEquals(1, classifierCalls.get());
		assertEquals(1, effectCalls.get());
	}

	@Test
	void armorTraitsAreMergedSortedAndFailuresAreIsolated() {
		Identifier cold = id("cold_resistance");
		Identifier fiery = id("fiery");
		ArmorApi.registerClassifier(id("alpha_failure"), stack -> {
			throw new IllegalArgumentException("expected contract-test failure");
		});
		ArmorApi.registerClassifier(id("beta_traits"), stack -> Set.of(fiery, cold));

		Set<Identifier> traits = ArmorApi.traits(new ItemStack(Items.IRON_CHESTPLATE));
		assertEquals(List.of(cold, fiery), new ArrayList<>(traits));
		assertTrue(ArmorApi.hasTrait(new ItemStack(Items.IRON_CHESTPLATE), fiery));
		assertThrows(UnsupportedOperationException.class, () -> traits.add(id("late_mutation")));
	}

	@Test
	void weaponTraitsAreMergedSortedAndImmutable() {
		Identifier charged = id("charged");
		Identifier giant = id("giant");
		WeaponApi.registerClassifier(id("traits"), stack -> Set.of(giant, charged));

		Set<Identifier> traits = WeaponApi.traits(new ItemStack(Items.IRON_SWORD));
		assertEquals(List.of(charged, giant), new ArrayList<>(traits));
		assertTrue(WeaponApi.hasTrait(new ItemStack(Items.IRON_SWORD), giant));
		assertThrows(UnsupportedOperationException.class, () -> traits.clear());
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("api_contract_test", path);
	}

	private static LivingEntity wearerWith(ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet) {
		LivingEntity wearer = mock(LivingEntity.class);
		when(wearer.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(head);
		when(wearer.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
		when(wearer.getItemBySlot(EquipmentSlot.LEGS)).thenReturn(legs);
		when(wearer.getItemBySlot(EquipmentSlot.FEET)).thenReturn(feet);
		return wearer;
	}
}
