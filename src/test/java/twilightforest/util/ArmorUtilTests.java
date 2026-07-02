package twilightforest.util;

import net.minecraft.util.Unit;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tamaized.beanification.junit.MockitoFixer;
import twilightforest.init.TFDataComponents;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoFixer.class)
public class ArmorUtilTests {

	private ArmorUtil instance;

	@BeforeAll
	public static void ensureDataComponents() {
		bindItemComponents(Items.LEATHER_BOOTS, Items.STICK);
	}

	@BeforeEach
	public void setup() {
		instance = new ArmorUtil();
	}

	@Test
	public void getShroudedArmorPercentage() {
		LivingEntity entity = mock(LivingEntity.class);
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			when(entity.getItemBySlot(slot)).thenReturn(ItemStack.EMPTY);
		}

		ItemStack shroudedBoots = new ItemStack(Items.LEATHER_BOOTS);
		shroudedBoots.set(TFDataComponents.EMPERORS_CLOTH, Unit.INSTANCE);
		when(entity.getItemBySlot(EquipmentSlot.FEET)).thenReturn(shroudedBoots);
		when(entity.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(new ItemStack(Items.STICK));

		float result = instance.getShroudedArmorPercentage(entity);

		long armorSlots = EquipmentSlotGroup.ARMOR.slots().size();
		assertEquals(1F / (float) armorSlots, result);
	}

	private static void bindItemComponents(ItemLike... itemLikes) {
		for (ItemLike itemLike : itemLikes) {
			var item = itemLike.asItem();
			var holder = item.builtInRegistryHolder();
			if (!holder.areComponentsBound()) {
				holder.bindComponents(DataComponentMap.EMPTY);
			}
		}
	}

}
