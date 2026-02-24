package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import twilightforest.enchantment.RechargeScepterEffect;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFEnchantments;
import twilightforest.init.TFSounds;
import twilightforest.util.TFItemStackUtils;

import java.util.function.Consumer;

public class FortificationWandItem extends Item {

	public FortificationWandItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (TFItemStackUtils.isAtZeroDurability(stack) && !player.hasInfiniteMaterials()) {
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			TFDataAttachments.get(player, TFDataAttachments.FORTIFICATION_SHIELDS).setShields(player, 5, true);
			if (!player.hasInfiniteMaterials()) {
				TFItemStackUtils.hurtWithoutBreaking(stack, 1, player);
			}
		}
		player.playSound(TFSounds.SHIELD_ADD.get(), 1.0F, (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);

		if (!player.hasInfiniteMaterials())
			player.getCooldowns().addCooldown(stack, 1200);
		return InteractionResult.SUCCESS;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
		boolean isSelected = slot == EquipmentSlot.MAINHAND;
		if (entity.tickCount % 20 == 0 && stack.has(DataComponents.ENCHANTMENTS) && !isSelected) {
			int renewal = stack.get(DataComponents.ENCHANTMENTS).getLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TFEnchantments.RENEWAL));
			if (renewal > 0) {
				RechargeScepterEffect.applyRecharge(level, stack, entity);
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, display, tooltip, flags);
		tooltip.accept(Component.translatable("item.twilightforest.scepter.desc", stack.getMaxDamage() - stack.getDamageValue()).withStyle(ChatFormatting.GRAY));
	}
}
