package twilightforest;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import twilightforest.api.ArmorApi;
import twilightforest.api.TravellerGearApi;
import twilightforest.api.TravellerGearClassifier;
import twilightforest.api.TravellerGearPart;
import twilightforest.api.WeaponApi;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.item.FieryArmorItem;
import twilightforest.item.MazebreakerPickItem;
import twilightforest.item.MinotaurAxeItem;
import twilightforest.item.YetiArmorItem;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Registers Twilight Forest's own implementations of its public classification APIs. */
final class TwilightForestApiImplementation {
	private static final Map<Item, Identifier> ARMOR_MATERIAL_TRAITS = Map.ofEntries(
		Map.entry(TFItems.NAGA_CHESTPLATE, ArmorApi.NAGA),
		Map.entry(TFItems.NAGA_LEGGINGS, ArmorApi.NAGA),
		Map.entry(TFItems.IRONWOOD_HELMET, ArmorApi.IRONWOOD),
		Map.entry(TFItems.IRONWOOD_CHESTPLATE, ArmorApi.IRONWOOD),
		Map.entry(TFItems.IRONWOOD_LEGGINGS, ArmorApi.IRONWOOD),
		Map.entry(TFItems.IRONWOOD_BOOTS, ArmorApi.IRONWOOD),
		Map.entry(TFItems.STEELEAF_HELMET, ArmorApi.STEELEAF),
		Map.entry(TFItems.STEELEAF_CHESTPLATE, ArmorApi.STEELEAF),
		Map.entry(TFItems.STEELEAF_LEGGINGS, ArmorApi.STEELEAF),
		Map.entry(TFItems.STEELEAF_BOOTS, ArmorApi.STEELEAF),
		Map.entry(TFItems.FIERY_HELMET, ArmorApi.FIERY),
		Map.entry(TFItems.FIERY_CHESTPLATE, ArmorApi.FIERY),
		Map.entry(TFItems.FIERY_LEGGINGS, ArmorApi.FIERY),
		Map.entry(TFItems.FIERY_BOOTS, ArmorApi.FIERY),
		Map.entry(TFItems.KNIGHTMETAL_HELMET, ArmorApi.KNIGHTMETAL),
		Map.entry(TFItems.KNIGHTMETAL_CHESTPLATE, ArmorApi.KNIGHTMETAL),
		Map.entry(TFItems.KNIGHTMETAL_LEGGINGS, ArmorApi.KNIGHTMETAL),
		Map.entry(TFItems.KNIGHTMETAL_BOOTS, ArmorApi.KNIGHTMETAL),
		Map.entry(TFItems.PHANTOM_HELMET, ArmorApi.PHANTOM),
		Map.entry(TFItems.PHANTOM_CHESTPLATE, ArmorApi.PHANTOM),
		Map.entry(TFItems.ARCTIC_HELMET, ArmorApi.ARCTIC),
		Map.entry(TFItems.ARCTIC_CHESTPLATE, ArmorApi.ARCTIC),
		Map.entry(TFItems.ARCTIC_LEGGINGS, ArmorApi.ARCTIC),
		Map.entry(TFItems.ARCTIC_BOOTS, ArmorApi.ARCTIC),
		Map.entry(TFItems.YETI_HELMET, ArmorApi.YETI),
		Map.entry(TFItems.YETI_CHESTPLATE, ArmorApi.YETI),
		Map.entry(TFItems.YETI_LEGGINGS, ArmorApi.YETI),
		Map.entry(TFItems.YETI_BOOTS, ArmorApi.YETI)
	);
	private static final Set<Item> TRAVELLERS_GEAR_ITEMS = Set.of(
		TFItems.TRAVELLERS_GOGGLES,
		TFItems.TRAVELLERS_VEST,
		TFItems.TRAVELLERS_GLOVES,
		TFItems.TRAVELLERS_WINGS,
		TFItems.TRAVELLERS_BELT,
		TFItems.TRAVELLERS_BOOTS
	);
	private static final Set<Item> WEAPON_ITEMS = Set.of(
		TFItems.IRONWOOD_SWORD,
		TFItems.IRONWOOD_AXE,
		TFItems.STEELEAF_SWORD,
		TFItems.STEELEAF_AXE,
		TFItems.FIERY_SWORD,
		TFItems.FIERY_PICKAXE,
		TFItems.KNIGHTMETAL_SWORD,
		TFItems.KNIGHTMETAL_PICKAXE,
		TFItems.KNIGHTMETAL_AXE,
		TFItems.BLOCK_AND_CHAIN,
		TFItems.GOLDEN_MINOTAUR_AXE,
		TFItems.DIAMOND_MINOTAUR_AXE,
		TFItems.MAZEBREAKER_PICKAXE,
		TFItems.TRIPLE_BOW,
		TFItems.SEEKER_BOW,
		TFItems.ICE_BOW,
		TFItems.ENDER_BOW,
		TFItems.ICE_SWORD,
		TFItems.GLASS_SWORD,
		TFItems.GIANT_PICKAXE,
		TFItems.GIANT_SWORD
	);
	private static final Set<Item> IGNITING_WEAPONS = Set.of(TFItems.FIERY_SWORD, TFItems.FIERY_PICKAXE);
	private static final Set<Item> ARMORED_TARGET_WEAPONS = Set.of(TFItems.KNIGHTMETAL_SWORD, TFItems.KNIGHTMETAL_PICKAXE);
	private static final Set<Item> CHARGE_WEAPONS = Set.of(TFItems.GOLDEN_MINOTAUR_AXE, TFItems.DIAMOND_MINOTAUR_AXE);

	private static boolean registered;

	private TwilightForestApiImplementation() {
	}

	static void registerBuiltins() {
		if (registered) {
			return;
		}

		TravellerGearApi.registerClassifier(
			TwilightForestMod.prefix("built_in_traveller_gear"),
			new BuiltInTravellerGearClassifier()
		);
		ArmorApi.registerClassifier(TwilightForestMod.prefix("built_in_armor"), TwilightForestApiImplementation::classifyArmor);
		WeaponApi.registerClassifier(TwilightForestMod.prefix("built_in_weapon"), TwilightForestApiImplementation::classifyWeapon);
		registered = true;
	}

	private static Set<Identifier> classifyArmor(ItemStack stack) {
		Identifier materialTrait = ARMOR_MATERIAL_TRAITS.get(stack.getItem());
		if (stack.getItem() instanceof FieryArmorItem) {
			materialTrait = ArmorApi.FIERY;
		} else if (stack.getItem() instanceof YetiArmorItem) {
			materialTrait = ArmorApi.YETI;
		}
		boolean travellersGear = TRAVELLERS_GEAR_ITEMS.contains(stack.getItem());
		if (materialTrait == null && !travellersGear) {
			return Set.of();
		}

		Set<Identifier> traits = new LinkedHashSet<>();
		traits.add(ArmorApi.TWILIGHT_FOREST_ARMOR);
		if (materialTrait != null) {
			traits.add(materialTrait);
		}
		if (travellersGear) {
			traits.add(ArmorApi.TRAVELLERS_GEAR);
		}
		if (ArmorApi.FIERY.equals(materialTrait)) {
			traits.add(ArmorApi.FIERY_REACTIVE);
		}
		if (ArmorApi.YETI.equals(materialTrait)) {
			traits.add(ArmorApi.CHILL_AURA);
		}
		if (stack.is(TFItems.FIERY_BOOTS)) {
			traits.add(ArmorApi.FIERY_STEP_IMMUNITY);
		}
		return Set.copyOf(traits);
	}

	private static Set<Identifier> classifyWeapon(ItemStack stack) {
		Item item = stack.getItem();
		boolean minotaurAxe = item instanceof MinotaurAxeItem;
		boolean mazebreaker = item instanceof MazebreakerPickItem;
		if (!WEAPON_ITEMS.contains(item) && !minotaurAxe && !mazebreaker) {
			return Set.of();
		}

		Set<Identifier> traits = new LinkedHashSet<>();
		traits.add(WeaponApi.TWILIGHT_FOREST_WEAPON);
		if (IGNITING_WEAPONS.contains(item)) {
			traits.add(WeaponApi.IGNITES_TARGETS);
		}
		if (ARMORED_TARGET_WEAPONS.contains(item)) {
			traits.add(WeaponApi.BONUS_AGAINST_ARMORED);
		}
		if (stack.is(TFItems.KNIGHTMETAL_AXE)) {
			traits.add(WeaponApi.BONUS_AGAINST_UNARMORED);
		}
		if (CHARGE_WEAPONS.contains(item) || minotaurAxe) {
			traits.add(WeaponApi.SPRINT_CHARGE_BONUS);
		}
		if (stack.is(TFItems.FIERY_PICKAXE)) {
			traits.add(WeaponApi.SMELTS_BLOCK_DROPS);
		}
		if (stack.is(TFItems.MAZEBREAKER_PICKAXE) || mazebreaker) {
			traits.add(WeaponApi.MAZESTONE_WEAR_EXEMPT);
		}
		if (stack.is(TFItems.TRIPLE_BOW)) {
			traits.add(WeaponApi.RESETS_ARROW_INVULNERABILITY);
		}
		return Set.copyOf(traits);
	}

	private static final class BuiltInTravellerGearClassifier implements TravellerGearClassifier {
		@Override
		public Set<TravellerGearPart> classify(ItemStack stack) {
			if (!stack.has(TFDataComponents.IS_TRAVELLERS_GEAR)) {
				return Set.of();
			}

			Set<TravellerGearPart> parts = new LinkedHashSet<>();
			if (stack.is(TFItems.TRAVELLERS_GOGGLES)) {
				parts.add(TravellerGearPart.GOGGLES);
			}
			if (stack.has(TFDataComponents.TRAVELLERS_HAS_CHESTPLATE)) {
				parts.add(TravellerGearPart.VEST);
			}
			if (stack.has(TFDataComponents.TRAVELLERS_HAS_GLOVES)) {
				parts.add(TravellerGearPart.GLOVES);
			}
			if (stack.has(TFDataComponents.TRAVELLERS_HAS_WINGS)) {
				parts.add(TravellerGearPart.WINGS);
			}
			if (stack.has(TFDataComponents.TRAVELLERS_HAS_BELT)) {
				parts.add(TravellerGearPart.BELT);
			}
			if (stack.has(TFDataComponents.TRAVELLERS_HAS_BOOTS)) {
				parts.add(TravellerGearPart.BOOTS);
			}
			return parts.isEmpty() ? Set.of() : Set.copyOf(parts);
		}

		@Override
		public Set<Identifier> activeEffects(net.minecraft.world.entity.LivingEntity wearer, ItemStack equippedStack) {
			Set<TravellerGearPart> equippedParts = this.classify(equippedStack);
			return wearer.registryAccess()
				.lookupOrThrow(TFRegistries.Keys.TRAVELLERS_MODIFIERS)
				.listElements()
				.filter(holder -> equippedParts.stream().anyMatch(part -> holder.value().group().test(part.equipmentSlot())))
				.filter(holder -> holder.value().isActive(equippedStack, holder.key(), wearer.isSpectator()))
				.map(holder -> holder.key().identifier())
				.collect(Collectors.toUnmodifiableSet());
		}
	}
}
