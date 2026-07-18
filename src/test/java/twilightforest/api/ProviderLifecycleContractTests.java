package twilightforest.api;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderLifecycleContractTests {
	@AfterEach
	void resetRegistrations() {
		TwilightForestApi.resetForTests();
	}

	@Test
	void apiVersionIsExplicitSemanticVersion() {
		assertEquals("1.0.0", TwilightForestApi.API_VERSION);
		assertEquals(1, TwilightForestApi.API_MAJOR_VERSION);
		assertTrue(TwilightForestApi.API_VERSION.matches("[0-9]+\\.[0-9]+\\.[0-9]+"));
	}

	@Test
	void builtInTraitIdsAreStableAndNamespaced() {
		assertEquals("twilightforest:armor", ArmorApi.TWILIGHT_FOREST_ARMOR.toString());
		assertEquals("twilightforest:armor/naga", ArmorApi.NAGA.toString());
		assertEquals("twilightforest:armor/ironwood", ArmorApi.IRONWOOD.toString());
		assertEquals("twilightforest:armor/steeleaf", ArmorApi.STEELEAF.toString());
		assertEquals("twilightforest:armor/fiery", ArmorApi.FIERY.toString());
		assertEquals("twilightforest:armor/knightmetal", ArmorApi.KNIGHTMETAL.toString());
		assertEquals("twilightforest:armor/phantom", ArmorApi.PHANTOM.toString());
		assertEquals("twilightforest:armor/arctic", ArmorApi.ARCTIC.toString());
		assertEquals("twilightforest:armor/yeti", ArmorApi.YETI.toString());
		assertEquals("twilightforest:armor/travellers_gear", ArmorApi.TRAVELLERS_GEAR.toString());
		assertEquals("twilightforest:armor/fiery_reactive", ArmorApi.FIERY_REACTIVE.toString());
		assertEquals("twilightforest:armor/chill_aura", ArmorApi.CHILL_AURA.toString());
		assertEquals("twilightforest:armor/fiery_step_immunity", ArmorApi.FIERY_STEP_IMMUNITY.toString());

		assertEquals("twilightforest:weapon", WeaponApi.TWILIGHT_FOREST_WEAPON.toString());
		assertEquals("twilightforest:weapon/ignites_targets", WeaponApi.IGNITES_TARGETS.toString());
		assertEquals("twilightforest:weapon/bonus_against_armored", WeaponApi.BONUS_AGAINST_ARMORED.toString());
		assertEquals("twilightforest:weapon/bonus_against_unarmored", WeaponApi.BONUS_AGAINST_UNARMORED.toString());
		assertEquals("twilightforest:weapon/sprint_charge_bonus", WeaponApi.SPRINT_CHARGE_BONUS.toString());
		assertEquals("twilightforest:weapon/smelts_block_drops", WeaponApi.SMELTS_BLOCK_DROPS.toString());
		assertEquals("twilightforest:weapon/mazestone_wear_exempt", WeaponApi.MAZESTONE_WEAR_EXEMPT.toString());
		assertEquals("twilightforest:weapon/resets_arrow_invulnerability", WeaponApi.RESETS_ARROW_INVULNERABILITY.toString());
	}

	@Test
	void emptySnapshotsAreImmutable() {
		assertTrue(AccessoryApi.itemConsumerIds().isEmpty());
		assertTrue(TravellerGearApi.classifierIds().isEmpty());
		assertTrue(ArmorApi.classifierIds().isEmpty());
		assertTrue(WeaponApi.classifierIds().isEmpty());
		assertThrows(UnsupportedOperationException.class,
			() -> AccessoryApi.itemConsumerIds().add(id("consumer")));
	}

	@Test
	void providerIdsAreSortedAndSnapshotsDoNotChangeRetroactively() {
		AccessoryApi.registerItemConsumer(id("zeta"), context -> AccessoryConsumptionResult.pass());
		List<Identifier> firstSnapshot = AccessoryApi.itemConsumerIds();
		AccessoryApi.registerItemConsumer(id("alpha"), context -> AccessoryConsumptionResult.pass());

		assertEquals(List.of(id("zeta")), firstSnapshot);
		assertEquals(List.of(id("alpha"), id("zeta")), AccessoryApi.itemConsumerIds());
		assertThrows(UnsupportedOperationException.class, () -> firstSnapshot.add(id("other")));
	}

	@Test
	void duplicateIdsFailImmediatelyWithoutReplacingTheOriginal() {
		AccessoryItemConsumer original = context -> AccessoryConsumptionResult.pass();
		AccessoryApi.registerItemConsumer(id("duplicate"), original);

		assertThrows(IllegalArgumentException.class,
			() -> AccessoryApi.registerItemConsumer(id("duplicate"), context -> AccessoryConsumptionResult.pass()));
		assertEquals(List.of(id("duplicate")), AccessoryApi.itemConsumerIds());
	}

	@Test
	void globalFreezeIsIdempotentAndRejectsLateRegistration() {
		ArmorApi.registerClassifier(id("armor"), stack -> Set.of());
		assertFalse(TwilightForestApi.registrationsFrozen());

		TwilightForestApi.freezeRegistrations();
		TwilightForestApi.freezeRegistrations();

		assertTrue(TwilightForestApi.registrationsFrozen());
		assertThrows(IllegalStateException.class,
			() -> ArmorApi.registerClassifier(id("late"), stack -> Set.of()));
		assertEquals(List.of(id("armor")), ArmorApi.classifierIds());
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("api_contract_test", path);
	}
}
