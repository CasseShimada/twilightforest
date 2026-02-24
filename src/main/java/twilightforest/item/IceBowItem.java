package twilightforest.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import twilightforest.entity.projectile.IceArrow;

public class IceBowItem extends BowItem {

	public IceBowItem(Properties properties) {
		super(properties);
	}

	@Override
	protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean isCrit) {
		Projectile projectile = super.createProjectile(level, shooter, weaponStack, projectileStack, isCrit);
		if (projectile instanceof AbstractArrow arrow) {
			return new IceArrow(arrow, projectileStack.copyWithCount(1), weaponStack);
		}
		return projectile;
	}
}
