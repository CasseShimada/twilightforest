package twilightforest.util;

import net.minecraft.util.Unit;
import net.minecraft.core.component.DataComponentMap;
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
import twilightforest.util.registry.DeferredHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoFixer.class)
public class ArmorUtilTests {

	private ArmorUtil instance;

	@BeforeAll
	public static void ensureDataComponents() {
		try {
			TFDataComponents.COMPONENTS.register();
		} catch (RuntimeException ignored) {
			// Registry may already be frozen in tests; fall back to manual binding.
		}
		ensureBound(TFDataComponents.EMPERORS_CLOTH);
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
		shroudedBoots.set(TFDataComponents.EMPERORS_CLOTH.get(), Unit.INSTANCE);
		when(entity.getItemBySlot(EquipmentSlot.FEET)).thenReturn(shroudedBoots);
		when(entity.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(new ItemStack(Items.STICK));

		float result = instance.getShroudedArmorPercentage(entity);

		long armorSlots = EquipmentSlot.values().length == 0 ? 0 : java.util.Arrays.stream(EquipmentSlot.values()).filter(EquipmentSlot::isArmor).count();
		assertEquals(1F / (float) armorSlots, result);
	}

	private static void ensureBound(DeferredHolder<?, ?> holder) {
		try {
			holder.get();
			return;
		} catch (IllegalStateException ignored) {
			// Will bind below.
		}
		try {
			var factoryField = DeferredHolder.class.getDeclaredField("factory");
			factoryField.setAccessible(true);
			var factory = (java.util.function.Supplier<?>) factoryField.get(holder);
			var value = factory.get();
			var bind = DeferredHolder.class.getDeclaredMethod("bind", Object.class);
			bind.setAccessible(true);
			bind.invoke(holder, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to bind deferred value for tests.", e);
		}
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
