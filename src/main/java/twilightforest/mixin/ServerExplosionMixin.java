package twilightforest.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import twilightforest.events.EntityEvents;

import java.util.List;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
	@ModifyVariable(
		method = "hurtEntities",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/server/level/ServerLevel;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
		)
	)
	private List<Entity> twilightforest$filterExplosionEntities(List<Entity> entities) {
		if (entities == null || entities.isEmpty()) {
			return entities;
		}
		Explosion explosion = (Explosion) (Object) this;
		entities.removeIf(entity -> EntityEvents.shouldFilterExplosionEntity(explosion, entity));
		return entities;
	}
}
