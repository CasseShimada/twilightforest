package twilightforest.mixin.client;

import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.ASMHooksClient;

import java.util.Iterator;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {
	@Redirect(
		method = "extractVisibleEntities",
		at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;")
	)
	private Iterator<Entity> twilightforest$injectMultipartEntities(Iterable<Entity> iterable) {
		return ASMHooksClient.resolveEntitiesForRendering(iterable.iterator());
	}
}
