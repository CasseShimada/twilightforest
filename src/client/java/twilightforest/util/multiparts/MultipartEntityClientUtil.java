package twilightforest.util.multiparts;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import twilightforest.client.BakedMultiPartRenderers;
import twilightforest.entity.TFPart;

import java.util.Iterator;

public final class MultipartEntityClientUtil {
	private MultipartEntityClientUtil() {
	}

	public static Iterator<Entity> injectTFPartEntities(Iterator<Entity> iter) {
		return new MultipartEntityIteratorWrapper(iter);
	}

	@Nullable
	public static EntityRenderer<?, ?> tryLookupTFPartRenderer(@Nullable EntityRenderer<?, ?> renderer, Entity entity) {
		if (entity instanceof TFPart<?> part) {
			return BakedMultiPartRenderers.lookup(part.renderer());
		}
		return renderer;
	}
}
