package twilightforest.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.util.multiparts.MultipartEntityUtil;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMultipartMixin {
	@Inject(method = "getEntityOrPart", at = @At("RETURN"), cancellable = true)
	private void twilightforest$resolveMultipartParts(int entityId, CallbackInfoReturnable<Entity> cir) {
		if (cir.getReturnValue() != null) {
			return;
		}

		ServerLevel level = (ServerLevel) (Object) this;
		Entity part = MultipartEntityUtil.getEntityOrPart(level, entityId);
		if (part != null) {
			cir.setReturnValue(part);
		}
	}
}
