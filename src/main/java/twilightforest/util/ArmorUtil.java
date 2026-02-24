package twilightforest.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import twilightforest.init.TFDataComponents;

public class ArmorUtil {

	public float getShroudedArmorPercentage(LivingEntity entity) {
		int shroudedArmor = 0;
		int nonShroudedArmor = 0;

		for (EquipmentSlot slot : EquipmentSlot.VALUES) {
			if (!slot.isArmor()) continue;
			ItemStack stack = entity.getItemBySlot(slot);
			if (!stack.isEmpty() && stack.get(TFDataComponents.EMPERORS_CLOTH.get()) != null) {
				shroudedArmor++;
			}
			nonShroudedArmor++;
		}

		return nonShroudedArmor > 0 && shroudedArmor > 0 ? (float) shroudedArmor / (float) nonShroudedArmor : 0.0F;
	}

}
