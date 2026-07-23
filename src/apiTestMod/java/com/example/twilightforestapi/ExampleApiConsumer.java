package com.example.twilightforestapi;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import twilightforest.api.AccessoryApi;
import twilightforest.api.AccessoryConsumptionResult;
import twilightforest.api.ArmorApi;
import twilightforest.api.TravellerGearApi;
import twilightforest.api.TravellerGearClassifier;
import twilightforest.api.TravellerGearPart;
import twilightforest.api.WeaponApi;

import java.util.Set;

/**
 * Compile-only consumer fixture. Its imports intentionally use only the public
 * Twilight Forest API package.
 */
public final class ExampleApiConsumer implements ModInitializer {
	private static final String ID = "twilightforest_api_testmod";

	/**
	 * Registers deterministic test providers only in the isolated runtime API
	 * scenario. Other development runs retain the no-consumer API state.
	 */
	@Override
	public void onInitialize() {
		if (!"api-consumer".equals(System.getProperty("twilightforest.runtimeScenario"))) {
			return;
		}
		AccessoryApi.registerItemConsumer(id("accessory_storage"), context -> AccessoryConsumptionResult.pass());
		TravellerGearApi.registerClassifier(id("traveller_gear"), new TravellerGearClassifier() {
			/** {@inheritDoc} */
			@Override
			public Set<TravellerGearPart> classify(ItemStack stack) {
				return stack.is(Items.STICK) ? Set.of(TravellerGearPart.BOOTS) : Set.of();
			}

			/** {@inheritDoc} */
			@Override
			public Set<Identifier> activeEffects(LivingEntity wearer, ItemStack equippedStack) {
				return Set.of();
			}
		});
		ArmorApi.registerClassifier(id("armor"), stack ->
			stack.is(Items.STICK) ? Set.of(id("runtime/armor")) : Set.of());
		ArmorApi.registerClassifier(id("armor_failure"), stack -> {
			if (stack.is(Items.POISONOUS_POTATO)) {
				throw new IllegalStateException("intentional API runtime consumer failure");
			}
			return Set.of();
		});
		ArmorApi.registerClassifier(id("armor_recovery"), stack ->
			stack.is(Items.POISONOUS_POTATO) ? Set.of(id("runtime/recovery")) : Set.of());
		ArmorApi.registerClassifier(id("armor_recursive"), stack -> {
			if (!stack.is(Items.STICK)) {
				return Set.of();
			}
			if (!ArmorApi.traits(stack).isEmpty()) {
				throw new IllegalStateException("recursive ArmorApi dispatch was not isolated");
			}
			return Set.of(id("runtime/recursive"));
		});
		WeaponApi.registerClassifier(id("weapon"), stack ->
			stack.is(Items.DIAMOND_SWORD) ? Set.of(id("runtime/weapon")) : Set.of());
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}
}
