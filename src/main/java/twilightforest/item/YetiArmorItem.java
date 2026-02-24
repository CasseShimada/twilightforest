package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFItems;

import java.util.function.Consumer;

public class YetiArmorItem extends Item {
	private static final MutableComponent TOOLTIP = Component.translatable("item.twilightforest.yeti_armor.desc").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));

	public YetiArmorItem(ArmorMaterial material, ArmorType type, Properties properties) {
		super(properties.humanoidArmor(material, type));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		tooltip.accept(TOOLTIP);
	}
}
