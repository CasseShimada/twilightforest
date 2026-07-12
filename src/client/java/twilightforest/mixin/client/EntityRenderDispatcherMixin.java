package twilightforest.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.client.BakedMultiPartRenderers;
import twilightforest.client.state.PartEntityState;
import twilightforest.util.multiparts.MultipartEntityClientUtil;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@Inject(method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", at = @At("RETURN"), cancellable = true)
	private void twilightforest$resolveMultipartRenderer(Entity entity, CallbackInfoReturnable<EntityRenderer<?, ?>> cir) {
		EntityRenderer<?, ?> resolved = MultipartEntityClientUtil.tryLookupTFPartRenderer(cir.getReturnValue(), entity);
		if (resolved != null) {
			cir.setReturnValue(resolved);
		}
	}

	@Inject(method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", at = @At("RETURN"), cancellable = true)
	private void twilightforest$resolveMultipartRenderer(EntityRenderState state, CallbackInfoReturnable<EntityRenderer<?, ?>> cir) {
		if (state instanceof PartEntityState partState && partState.rendererId != null) {
			EntityRenderer<?, ?> resolved = BakedMultiPartRenderers.lookup(partState.rendererId);
			if (resolved != null) {
				cir.setReturnValue(resolved);
			}
		}
	}
}
