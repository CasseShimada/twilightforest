package twilightforest;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import twilightforest.entity.boss.UrGhast;

// TODO: Think about reorganizing each group into their own class or subclass of ASMHooks
@SuppressWarnings({"JavadocReference", "unused", "RedundantSuppression", "deprecation"})
public class ASMHooks {

	public static boolean isEntityInUrGhastTears(Entity entity) {
		if (!(entity instanceof Player) || !entity.level().hasChunkAt(entity.blockPosition())) {
			return false;
		}

		if (!entity.level().canSeeSkyFromBelowWater(entity.blockPosition())) {
			return false;
		}

		AABB tearArea = entity.getBoundingBox().inflate(32.0D, 32.0D, 32.0D);
		for (UrGhast urGhast : entity.level().getEntitiesOfClass(UrGhast.class, tearArea, UrGhast::isInTantrum)) {
			if (urGhast.getBoundingBox().move(0.0D, -16.0D, 0.0D).inflate(0.0D, 16.0D, 0.0D).intersects(entity.getBoundingBox())) {
				return true;
			}
		}

		return false;
	}

}
