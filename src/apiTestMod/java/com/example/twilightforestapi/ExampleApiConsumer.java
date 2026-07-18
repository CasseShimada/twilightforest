package com.example.twilightforestapi;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
	 * Registers no-op providers during common initialization to prove that a
	 * consumer can compile without importing implementation classes.
	 */
	@Override
	public void onInitialize() {
		AccessoryApi.registerItemConsumer(id("accessory_storage"), context -> AccessoryConsumptionResult.pass());
		TravellerGearApi.registerClassifier(id("traveller_gear"), new TravellerGearClassifier() {
			/** {@inheritDoc} */
			@Override
			public Set<TravellerGearPart> classify(ItemStack stack) {
				return Set.of();
			}

			/** {@inheritDoc} */
			@Override
			public Set<Identifier> activeEffects(LivingEntity wearer, ItemStack equippedStack) {
				return Set.of();
			}
		});
		ArmorApi.registerClassifier(id("armor"), stack -> Set.of());
		WeaponApi.registerClassifier(id("weapon"), stack -> Set.of());
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}
}
