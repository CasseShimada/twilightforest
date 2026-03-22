package twilightforest.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.util.multiparts.MultipartEntityUtil;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Level.class)
public abstract class LevelMultipartMixin {
	@Inject(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
	private void twilightforest$includeMultipartParts(Entity except, AABB box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
		Level level = (Level) (Object) this;
		cir.setReturnValue(MultipartEntityUtil.injectTFPartEntities(level, except, box, predicate, cir.getReturnValue()));
	}
}
