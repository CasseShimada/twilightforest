package twilightforest.item.travellers_gear;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import twilightforest.init.TFArmorMaterials;
import twilightforest.init.TFAttributeModifiers;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.modifiers.TravellersModifiable;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;
import twilightforest.item.travellers_gear.modifiers.TravellersTooltipContext;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TravellersArmorItem extends Item implements TravellersModifiable {
	private static final MutableComponent GLOVES_TOOLTIP = Component.translatable("item.twilightforest.travellers_gloves.desc").withStyle(ChatFormatting.GRAY);
	private final int insertableModifierSlots;

	public TravellersArmorItem(int insertableModifierSlots, Properties properties) {
		super(properties.component(TFDataComponents.IS_TRAVELLERS_GEAR, Unit.INSTANCE));
		this.insertableModifierSlots = insertableModifierSlots;
	}

	@Override
	public Component getName(ItemStack stack) {
		if (isTravellersArmorAndBroken(stack)) {
			return super.getName(stack).copy().append(Component.translatable("travellers_gear.broken").withStyle(ChatFormatting.GRAY));
		}
		return super.getName(stack);
	}

	public static Properties gogglesProperties(Properties properties) {
		return properties.attributes(defaultArmorProperties(ArmorType.HELMET).build())
			.component(TFDataComponents.ZOOM_ABILITY_MODIFIER, 0.3F);
	}

	public static Properties chestProperties(Properties properties) {
		return properties
			.component(TFDataComponents.TRAVELLERS_HAS_CHESTPLATE, Unit.INSTANCE)
			.component(TFDataComponents.SWIFT_SWIM, Unit.INSTANCE)
			.attributes(defaultArmorProperties(ArmorType.CHESTPLATE)
				.add(Attributes.WATER_MOVEMENT_EFFICIENCY, TFAttributeModifiers.TRAVELLERS_SWIFT_SWIM, EquipmentSlotGroup.CHEST)
				.build());
	}

	public static Properties glovesProperties(Properties properties) {
		return properties.component(TFDataComponents.TRAVELLERS_HAS_GLOVES, Unit.INSTANCE);
	}

	public static Properties wingsProperties(Properties properties) {
		return properties.attributes(defaultArmorProperties(ArmorType.LEGGINGS).build())
			.component(TFDataComponents.TRAVELLERS_HAS_WINGS, Unit.INSTANCE)
			.component(TFDataComponents.HIGH_JUMP_AMPLIFIER, 1);
	}

	public static Properties bootsProperties(Properties properties) {
		return properties
			.component(TFDataComponents.TRAVELLERS_HAS_BOOTS, Unit.INSTANCE)
			.component(TFDataComponents.HIGH_STEP, Unit.INSTANCE)
			.attributes(defaultArmorProperties(ArmorType.BOOTS)
				.add(Attributes.STEP_HEIGHT, TFAttributeModifiers.TRAVELLERS_HIGH_STEP, EquipmentSlotGroup.FEET)
				.build());
	}

	public static Properties baseProperties(Properties properties, ArmorType type, boolean damageable) {
		ArmorMaterial material = TFArmorMaterials.TRAVELLERS_GEAR;
		if (damageable) {
			properties.durability(type.getDurability(material.durability()));
		}
		return properties
			.attributes(material.createAttributes(type))
			.component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot())
				.setEquipSound(material.equipSound())
				.setAsset(material.assetId())
				.build())
			.repairable(material.repairIngredient());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, builder, flag);
		HolderLookup.Provider registries = context.registries();
		if (registries == null) {
			return;
		}

		List<Holder.Reference<TravellersModifier>> abilities = TravellersModifiersManager.findAllAbilityModifiers(registries, stack);
		for (Holder.Reference<TravellersModifier> modifier : abilities) {
			builder.accept(Component.translatable("travellers_gear.ability", TravellersModifiersManager.getModifierTooltipComponent(modifier).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GOLD));
		}

		List<Holder.Reference<TravellersModifier>> insertable = TravellersModifiersManager.findAllInsertableModifiers(registries, stack);
		boolean shiftDown = TravellersTooltipContext.hasShiftDown();
		for (Holder.Reference<TravellersModifier> modifier : insertable) {
			builder.accept(Component.literal("- ").append(TravellersModifiersManager.getModifierTooltipComponent(modifier).withStyle(ChatFormatting.GRAY)));
			if (shiftDown) {
				modifier.value().getDescription().forEach(description -> builder.accept(Component.literal("")
					.append(Component.translatable("travellers_gear.info_indent").withStyle(ChatFormatting.BOLD))
					.append(description)));
			}
		}

		for (int i = insertable.size(); i < this.getModifierSlots(); i++) {
			builder.accept(Component.literal("- ").append(Component.translatable("travellers_gear.modifier.empty").withStyle(ChatFormatting.DARK_GRAY)));
		}
		if (this == TFItems.TRAVELLERS_GLOVES) {
			builder.accept(GLOVES_TOOLTIP);
		}
		if (!shiftDown && Stream.concat(abilities.stream(), insertable.stream())
			.map(Holder.Reference::value)
			.map(TravellersModifier::getDescription)
			.anyMatch(descriptions -> !descriptions.isEmpty())) {
			builder.accept(Component.translatable(
				"travellers_gear.shift_info",
				Component.literal("Shift").withStyle(ChatFormatting.YELLOW)
			).withStyle(ChatFormatting.WHITE));
		}

	}

	public static boolean isTravellersArmorAndBroken(ItemStack stack) {
		return stack.has(TFDataComponents.IS_TRAVELLERS_GEAR)
			&& stack.isDamageableItem()
			&& stack.getMaxDamage() - 1 <= stack.getDamageValue();
	}

	public static ItemAttributeModifiers.Builder defaultArmorProperties(ArmorType type) {
		ArmorMaterial material = TFArmorMaterials.TRAVELLERS_GEAR;
		int defense = material.defense().getOrDefault(type, 0);
		ItemAttributeModifiers.Builder modifiers = ItemAttributeModifiers.builder();
		EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
		Identifier modifierId = Identifier.withDefaultNamespace("armor." + type.getName());
		modifiers.add(Attributes.ARMOR, new AttributeModifier(modifierId, defense, AttributeModifier.Operation.ADD_VALUE), slotGroup);
		modifiers.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, material.toughness(), AttributeModifier.Operation.ADD_VALUE), slotGroup);
		if (material.knockbackResistance() > 0.0F) {
			modifiers.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(modifierId, material.knockbackResistance(), AttributeModifier.Operation.ADD_VALUE), slotGroup);
		}
		return modifiers;
	}

	@Override
	public int getModifierSlots() {
		return this.insertableModifierSlots;
	}
}
