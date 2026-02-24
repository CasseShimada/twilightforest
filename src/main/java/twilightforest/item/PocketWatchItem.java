package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class PocketWatchItem extends Item {
	private static final MutableComponent TOOLTIP = Component.translatable("item.twilightforest.pocket_watch.desc").withStyle(ChatFormatting.GRAY);

	public PocketWatchItem(Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
		if (entity instanceof LivingEntity living) {
			if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
				living.addEffect(new MobEffectInstance(MobEffects.SPEED, 5, 0, false, false, false));
				living.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 5, 0, false, false, false));
			}

			if (living.isHolding(this)) {
				living.addEffect(new MobEffectInstance(MobEffects.HASTE, 5, 0, false, false, false));
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(TOOLTIP);
	}
}
