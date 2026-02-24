package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class FierySwordItem extends Item {

	public FierySwordItem(ToolMaterial material, Properties properties) {
		super(properties.sword(material, 3.0F, -2.4F));
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);

		if (!target.level().isClientSide() && !target.fireImmune()) {
			target.igniteForSeconds(15);
		} else {
			for (int var1 = 0; var1 < 20; ++var1) {
				double px = target.getX() + target.level().getRandom().nextFloat() * target.getBbWidth() * 2.0F - target.getBbWidth();
				double py = target.getY() + target.level().getRandom().nextFloat() * target.getBbHeight();
				double pz = target.getZ() + target.level().getRandom().nextFloat() * target.getBbWidth() * 2.0F - target.getBbWidth();
				target.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0.02, 0.02, 0.02);
			}
		}

	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
	}
}
