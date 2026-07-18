package twilightforest.init.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.components.item.ItemDisplayContents;
import twilightforest.init.TFAttributeModifiers;
import twilightforest.init.TFDataComponents;
import twilightforest.item.travellers_gear.modifiers.*;
import twilightforest.item.travellers_gear.TravellersArmorBeltItem;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class TravellersModifiersManager {
	public static final ResourceKey<TravellersModifier> AUTO_REPAIR_MODIFIER = makeKey("auto_repair");
	public static final ResourceKey<TravellersModifier> ZOOM_ABILITY = makeKey("zoom");
	public static final ResourceKey<TravellersModifier> AQUATIC_AGILITY_MODIFIER = makeKey("aquatic_agility");
	public static final ResourceKey<TravellersModifier> RED_THREAD_VISION_MODIFIER = makeKey("red_thread_vision");
	public static final ResourceKey<TravellersModifier> ALL_NIGHT_GOGGLES_MODIFIER = makeKey("all_night_goggles");
	public static final ResourceKey<TravellersModifier> ITEM_DISPLAY_MODIFIER = makeKey("item_display");
	public static final ResourceKey<TravellersModifier> SWIFT_SWIM_ABILITY = makeKey("swift_swim");
	public static final ResourceKey<TravellersModifier> STEALTH_MODIFIER = makeKey("stealth");
	public static final ResourceKey<TravellersModifier> ARROW_MAGNETISM_MODIFIER = makeKey("arrow_magnetism");
	public static final ResourceKey<TravellersModifier> EFFICIENT_EATER_MODIFIER = makeKey("efficient_eater");
	public static final ResourceKey<TravellersModifier> PERFECT_DODGE_MODIFIER = makeKey("perfect_dodge");
	public static final ResourceKey<TravellersModifier> HASTE_MODIFIER = makeKey("haste");
	public static final ResourceKey<TravellersModifier> SWAP_HOTBAR_ABILITY = makeKey("swap_hotbar_ability");
	public static final ResourceKey<TravellersModifier> SWAP_HOTBAR_MODIFIER = makeKey("swap_hotbar");
	public static final ResourceKey<TravellersModifier> HIGH_JUMP_ABILITY = makeKey("high_jump");
	public static final ResourceKey<TravellersModifier> GRADUAL_GLIDE_MODIFIER = makeKey("gradual_glide");
	public static final ResourceKey<TravellersModifier> AGILE_RANGER_MODIFIER = makeKey("agile_ranger");
	public static final ResourceKey<TravellersModifier> DOUBLE_JUMP_MODIFIER = makeKey("double_jump");
	public static final ResourceKey<TravellersModifier> SIDESTEP_MODIFIER = makeKey("side_step");
	public static final ResourceKey<TravellersModifier> STEP_UP_ABILITY = makeKey("step_up");
	public static final ResourceKey<TravellersModifier> STRAIGHT_AHEAD_MODIFIER = makeKey("straight_ahead");
	public static final ResourceKey<TravellersModifier> SLIMY_SOLES_MODIFIER = makeKey("slimy_soles");
	public static final ResourceKey<TravellersModifier> UNRESTRAINED_MODIFIER = makeKey("unrestrained");
	public static final ResourceKey<TravellersModifier> WATER_WALK_MODIFIER = makeKey("water_walk");

	public static final Set<ResourceKey<TravellersModifier>> ALWAYS_ACTIVE = Set.of(AUTO_REPAIR_MODIFIER);

	private TravellersModifiersManager() {
	}

	private static ResourceKey<TravellersModifier> makeKey(String name) {
		return ResourceKey.create(TFRegistries.Keys.TRAVELLERS_MODIFIERS, TwilightForestMod.prefix(name));
	}

	public static void bootstrap(BootstrapContext<TravellersModifier> context) {
		context.register(AUTO_REPAIR_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.ARMOR, TFDataComponents.AUTO_REPAIR_PROBABILITY, 0.001F, componentText(AUTO_REPAIR_MODIFIER)));
		context.register(ZOOM_ABILITY, new BuiltinTravellersComponentModifier(EquipmentSlotGroup.HEAD, TFDataComponents.ZOOM_ABILITY_MODIFIER));
		context.register(AQUATIC_AGILITY_MODIFIER, new TravellersEntryModifier(EquipmentSlotGroup.HEAD, List.of(
			new ItemAttributeModifiers.Entry(Attributes.OXYGEN_BONUS, TFAttributeModifiers.TRAVELLERS_AQUATIC_AGILITY_OXYGEN, EquipmentSlotGroup.HEAD),
			new ItemAttributeModifiers.Entry(Attributes.SUBMERGED_MINING_SPEED, TFAttributeModifiers.TRAVELLERS_AQUATIC_AGILITY_MINING, EquipmentSlotGroup.HEAD)
		), TFDataComponents.AQUATIC_AGILITY, componentText(AQUATIC_AGILITY_MODIFIER), false));
		context.register(RED_THREAD_VISION_MODIFIER, component(EquipmentSlotGroup.HEAD, TFDataComponents.RED_THREAD_VISION, Unit.INSTANCE, RED_THREAD_VISION_MODIFIER));
		context.register(ALL_NIGHT_GOGGLES_MODIFIER, component(EquipmentSlotGroup.HEAD, TFDataComponents.ALL_NIGHT_GOGGLES, Unit.INSTANCE, ALL_NIGHT_GOGGLES_MODIFIER));
		context.register(ITEM_DISPLAY_MODIFIER, component(EquipmentSlotGroup.HEAD, TFDataComponents.ITEM_DISPLAY, ItemDisplayContents.EMPTY, ITEM_DISPLAY_MODIFIER));

		context.register(SWIFT_SWIM_ABILITY, new TravellersEntryModifier(EquipmentSlotGroup.CHEST, List.of(
			new ItemAttributeModifiers.Entry(Attributes.WATER_MOVEMENT_EFFICIENCY, TFAttributeModifiers.TRAVELLERS_SWIFT_SWIM, EquipmentSlotGroup.CHEST)
		), TFDataComponents.SWIFT_SWIM, List.of(), true));
		context.register(STEALTH_MODIFIER, component(EquipmentSlotGroup.CHEST, TFDataComponents.STEALTH_CROUCHING, Unit.INSTANCE, STEALTH_MODIFIER));
		context.register(ARROW_MAGNETISM_MODIFIER, component(EquipmentSlotGroup.CHEST, TFDataComponents.ARROW_MAGNETISM, Unit.INSTANCE, ARROW_MAGNETISM_MODIFIER));
		context.register(EFFICIENT_EATER_MODIFIER, component(EquipmentSlotGroup.CHEST, TFDataComponents.EFFICIENT_EATER, 2.0F, EFFICIENT_EATER_MODIFIER));
		context.register(PERFECT_DODGE_MODIFIER, component(EquipmentSlotGroup.CHEST, TFDataComponents.PERFECT_DODGE_PROBABILITY, 0.3F, PERFECT_DODGE_MODIFIER));
		context.register(HASTE_MODIFIER, component(EquipmentSlotGroup.CHEST, TFDataComponents.HASTE_AMPLIFIER, 1, HASTE_MODIFIER));

		context.register(SWAP_HOTBAR_ABILITY, new BuiltinTravellersComponentModifier(EquipmentSlotGroup.LEGS, TFDataComponents.SWAP_HOTBAR_ABILITY));
		context.register(SWAP_HOTBAR_MODIFIER, new TransferableComponentModifier(EquipmentSlotGroup.LEGS, TFDataComponents.SWAP_HOTBAR_MODIFIER, DataComponents.CONTAINER, TravellersArmorBeltItem.DEFAULT_EMPTY_BELT_CONTAINER, componentText(SWAP_HOTBAR_MODIFIER)));
		context.register(HIGH_JUMP_ABILITY, new BuiltinTravellersComponentModifier(EquipmentSlotGroup.LEGS, TFDataComponents.HIGH_JUMP_AMPLIFIER));
		context.register(GRADUAL_GLIDE_MODIFIER, component(EquipmentSlotGroup.LEGS, TFDataComponents.GRADUALLY_GLIDING_MULTIPLIER, 1.0F - 1.0F / 6.0F, GRADUAL_GLIDE_MODIFIER));
		context.register(AGILE_RANGER_MODIFIER, component(EquipmentSlotGroup.LEGS, TFDataComponents.AGILE_RANGER_MODIFIER, 5.0F, AGILE_RANGER_MODIFIER));
		context.register(DOUBLE_JUMP_MODIFIER, component(EquipmentSlotGroup.LEGS, TFDataComponents.DOUBLE_JUMP, Unit.INSTANCE, DOUBLE_JUMP_MODIFIER));
		context.register(SIDESTEP_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.LEGS, TFDataComponents.SIDESTEP_COOLDOWN, 40L, componentText(SIDESTEP_MODIFIER, Component.keybind("key.left"), Component.keybind("key.right"))));

		context.register(STEP_UP_ABILITY, new TravellersEntryModifier(EquipmentSlotGroup.FEET, List.of(
			new ItemAttributeModifiers.Entry(Attributes.STEP_HEIGHT, TFAttributeModifiers.TRAVELLERS_HIGH_STEP, EquipmentSlotGroup.FEET)
		), TFDataComponents.HIGH_STEP, List.of(), true));
		context.register(STRAIGHT_AHEAD_MODIFIER, component(EquipmentSlotGroup.FEET, TFDataComponents.STRAIGHT_AHEAD_MULTIPLIER, 1.4D, STRAIGHT_AHEAD_MODIFIER));
		context.register(SLIMY_SOLES_MODIFIER, component(EquipmentSlotGroup.FEET, TFDataComponents.SLIMY_SOLES_COEFFICIENT, 0.5F, SLIMY_SOLES_MODIFIER));
		context.register(UNRESTRAINED_MODIFIER, component(EquipmentSlotGroup.FEET, TFDataComponents.UNRESTRAINED, Unit.INSTANCE, UNRESTRAINED_MODIFIER));
		context.register(WATER_WALK_MODIFIER, component(EquipmentSlotGroup.FEET, TFDataComponents.WATER_WALK, Unit.INSTANCE, WATER_WALK_MODIFIER));
	}

	private static <T> TravellersComponentModifier component(EquipmentSlotGroup group, net.minecraft.core.component.DataComponentType<T> type, T value, ResourceKey<TravellersModifier> key) {
		return new TravellersComponentModifier(group, type, value, componentText(key));
	}

	private static List<Component> componentText(ResourceKey<TravellersModifier> modifier, Object... args) {
		return List.of(Component.translatable(modifier.identifier().toLanguageKey("travellers_gear.modifier", "description"), args));
	}

	public static boolean isModifierActive(HolderLookup.Provider registries, ItemStack stack, ResourceKey<TravellersModifier> key, boolean spectator) {
		return getModifier(registries, key).map(modifier -> modifier.isActive(stack, key, spectator)).orElse(false);
	}

	public static boolean isModifierActive(Entity entity, ItemStack stack, ResourceKey<TravellersModifier> key) {
		return isModifierActive(entity.registryAccess(), stack, key, entity.isSpectator());
	}

	public static boolean isModifierActive(Entity entity, ResourceKey<TravellersModifier> key) {
		return entity instanceof LivingEntity living && isModifierActive(living, key);
	}

	public static boolean isModifierActive(LivingEntity living, ResourceKey<TravellersModifier> key) {
		Optional<TravellersModifier> modifier = getModifier(living.registryAccess(), key);
		if (modifier.isEmpty()) {
			return false;
		}
		ItemStack stack = getStackForGroup(living, modifier.get().group());
		return !stack.isEmpty() && modifier.get().isActive(stack, key, living.isSpectator());
	}

	public static boolean hasTravellersModifier(HolderLookup.Provider registries, ItemStack stack, ResourceKey<TravellersModifier> key) {
		return getModifier(registries, key).map(modifier -> hasTravellersModifier(stack, modifier)).orElse(false);
	}

	public static boolean addModifier(HolderLookup.Provider registries, ItemStack stack, ResourceKey<TravellersModifier> key) {
		return getModifier(registries, key).map(modifier -> addModifier(stack, modifier)).orElse(false);
	}

	public static boolean transferModifier(HolderLookup.Provider registries, ItemStack stack, List<Ingredient> ingredients, ResourceKey<TravellersModifier> key) {
		return getModifier(registries, key)
			.filter(TransferableTravellersModifier.class::isInstance)
			.map(TransferableTravellersModifier.class::cast)
			.map(modifier -> modifier.transfer(stack, ingredients))
			.orElse(false);
	}

	public static boolean hasTravellersModifier(ItemStack stack, TravellersModifier modifier) {
		return modifier.hasModifier(stack);
	}

	public static boolean addModifier(ItemStack stack, TravellersModifier modifier) {
		return modifier instanceof InsertableTravellersModifier insertable && insertable.addModifier(stack);
	}

	public static boolean transferModifier(ItemStack stack, List<ItemStack> inputs, TravellersModifier modifier) {
		return modifier instanceof TransferableTravellersModifier transferable && transferable.transferFromStacks(stack, inputs);
	}

	public static int getModifierDataComponentProviders(List<ItemStack> inputs, TravellersModifier modifier) {
		return modifier instanceof TransferableComponentModifier transferable
			? transferable.findDataComponentProviderStacks(inputs).size()
			: 0;
	}

	public static List<Holder.Reference<TravellersModifier>> findAllInsertableModifiers(HolderLookup.Provider registries, ItemStack stack) {
		return registries.lookupOrThrow(TFRegistries.Keys.TRAVELLERS_MODIFIERS).listElements()
			.filter(holder -> holder.value() instanceof InsertableTravellersModifier && !holder.value().isAbility() && holder.value().hasModifier(stack))
			.toList();
	}

	public static List<Holder.Reference<TravellersModifier>> findAllInsertableModifiers(Level level, ItemStack stack) {
		return findAllInsertableModifiers(level.registryAccess(), stack);
	}

	public static List<Holder.Reference<TravellersModifier>> findAllAbilityModifiers(HolderLookup.Provider registries, ItemStack stack) {
		return registries.lookupOrThrow(TFRegistries.Keys.TRAVELLERS_MODIFIERS).listElements()
			.filter(holder -> holder.value().isAbility() && holder.value().hasModifier(stack))
			.toList();
	}

	public static long countInsertableModifiers(HolderLookup.Provider registries, ItemStack stack) {
		return findAllInsertableModifiers(registries, stack).size();
	}

	public static boolean isModifierEnabled(HolderLookup.Provider registries, ResourceKey<TravellersModifier> key) {
		return getModifier(registries, key).isPresent();
	}

	public static MutableComponent getModifierTooltipComponent(Holder.Reference<TravellersModifier> modifier) {
		return TravellersTooltipContext.translate(modifier.key().identifier().toLanguageKey(modifier.value().getPrefix()));
	}

	private static ItemStack getStackForGroup(LivingEntity living, EquipmentSlotGroup group) {
		EquipmentSlot match = null;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (!slot.isArmor() || !group.test(slot)) {
				continue;
			}
			if (match != null) {
				return ItemStack.EMPTY;
			}
			match = slot;
		}
		return match == null ? ItemStack.EMPTY : living.getItemBySlot(match);
	}

	private static Optional<TravellersModifier> getModifier(HolderLookup.Provider registries, ResourceKey<TravellersModifier> key) {
		return registries.lookupOrThrow(TFRegistries.Keys.TRAVELLERS_MODIFIERS).get(key).map(Holder.Reference::value);
	}
}
