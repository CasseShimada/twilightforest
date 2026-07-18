package twilightforest.events;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.junit.jupiter.api.BeforeAll;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.junit.jupiter.api.Test;
import twilightforest.init.TFDataComponents;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TravellersGearEventsTests {
	@BeforeAll
	static void bindTestItemComponents() {
		bindDamageableComponents(Items.IRON_HELMET, Items.IRON_CHESTPLATE);
	}

	private static void bindDamageableComponents(ItemLike... itemLikes) {
		DataComponentMap components = DataComponentMap.builder()
			.set(DataComponents.MAX_STACK_SIZE, 1)
			.set(DataComponents.MAX_DAMAGE, 100)
			.set(DataComponents.DAMAGE, 0)
			.build();
		for (ItemLike itemLike : itemLikes) {
			var holder = itemLike.asItem().builtInRegistryHolder();
			if (!holder.areComponentsBound()) {
				holder.bindComponents(components);
			}
		}
	}

	private static ItemAttributeModifiers attributes() {
		return new ItemAttributeModifiers(List.of(new ItemAttributeModifiers.Entry(
			Attributes.ARMOR,
			new AttributeModifier(Identifier.fromNamespaceAndPath("twilightforest", "test_broken_attribute"), 1.0D, AttributeModifier.Operation.ADD_VALUE),
			EquipmentSlotGroup.HEAD
		)));
	}

	@Test
	void brokenGearStoresItsAttributesAndSuppressesOnlyComponentModifiers() {
		ItemStack stack = new ItemStack(Items.IRON_HELMET);
		ItemAttributeModifiers current = attributes();
		stack.set(TFDataComponents.IS_TRAVELLERS_GEAR, Unit.INSTANCE);
		stack.set(DataComponents.ATTRIBUTE_MODIFIERS, current);
		stack.setDamageValue(stack.getMaxDamage() - 1);

		assertSame(ItemAttributeModifiers.EMPTY, TravellersGearEvents.activeAttributeModifiers(stack, current));
		assertEquals(current, stack.get(TFDataComponents.STORED_BROKEN_ATTRIBUTES));
	}

	@Test
	void repairedGearRestoresStoredAttributesAndRemovesTheCompatibilityComponent() {
		ItemStack stack = new ItemStack(Items.IRON_HELMET);
		ItemAttributeModifiers stored = attributes();
		stack.set(TFDataComponents.IS_TRAVELLERS_GEAR, Unit.INSTANCE);
		stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		stack.set(TFDataComponents.STORED_BROKEN_ATTRIBUTES, stored);
		stack.setDamageValue(stack.getMaxDamage() - 2);

		ItemAttributeModifiers restored = TravellersGearEvents.activeAttributeModifiers(stack, ItemAttributeModifiers.EMPTY);
		assertEquals(stored, restored);
		assertEquals(restored, stack.get(DataComponents.ATTRIBUTE_MODIFIERS));
		assertFalse(stack.has(TFDataComponents.STORED_BROKEN_ATTRIBUTES));
	}

	@Test
	void anvilOnlyBlocksTwoTravellersGearInputs() {
		ItemStack left = new ItemStack(Items.IRON_HELMET);
		ItemStack right = new ItemStack(Items.IRON_CHESTPLATE);
		left.set(TFDataComponents.IS_TRAVELLERS_GEAR, Unit.INSTANCE);
		right.set(TFDataComponents.IS_TRAVELLERS_GEAR, Unit.INSTANCE);

		assertTrue(TravellersGearEvents.blocksAnvilCombination(left, right));
		right.remove(TFDataComponents.IS_TRAVELLERS_GEAR);
		assertFalse(TravellersGearEvents.blocksAnvilCombination(left, right));
	}
}
