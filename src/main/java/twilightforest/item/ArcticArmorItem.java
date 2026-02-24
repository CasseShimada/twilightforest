package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import twilightforest.init.TFItems;

import java.util.function.Consumer;

public class ArcticArmorItem extends Item {
	private static final MutableComponent TOOLTIP = Component.translatable("item.twilightforest.arctic_armor.desc").withStyle(ChatFormatting.GRAY);
	public static final int DEFAULT_COLOR = 0xFFBDCFD9;

	public ArcticArmorItem(ArmorMaterial material, ArmorType type, Properties properties) {
		super(properties.humanoidArmor(material, type));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		if (!stack.has(DataComponents.DYED_COLOR)) {
			tooltip.accept(TOOLTIP);
		}
	}
}
