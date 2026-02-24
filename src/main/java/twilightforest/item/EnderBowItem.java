package twilightforest.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import twilightforest.init.TFDataAttachments;

public class EnderBowItem extends BowItem {
	public EnderBowItem(Properties properties) {
		super(properties);
	}

	@Override
	protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean isCrit) {
		Projectile projectile = super.createProjectile(level, shooter, weaponStack, projectileStack, isCrit);
		if (projectile instanceof AbstractArrow arrow) {
			TFDataAttachments.set(arrow, TFDataAttachments.ENDER_BOW_ARROW, true);
		}
		return projectile;
	}
}
