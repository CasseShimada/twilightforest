package twilightforest.util.multiparts;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tamaized.beanification.junit.MockitoFixer;
import twilightforest.entity.TFPart;
import net.minecraft.resources.Identifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoFixer.class)
public class MultipartEntityUtilTests {
	@Test
	public void tryLookupTFPartRenderer() {
		Identifier location = Identifier.withDefaultNamespace("test");
		EntityRenderer<?, ?> partRenderer = mock(EntityRenderer.class);
		putRenderer(location, partRenderer);

		TFPart<?> part = mock(TFPart.class);
		when(part.renderer()).thenReturn(location);

		EntityRenderer<?, ?> originalRenderer = mock(EntityRenderer.class);

		EntityRenderer<?, ?> result = MultipartEntityClientUtil.tryLookupTFPartRenderer(originalRenderer, part);

		assertNotNull(result);
		assertSame(partRenderer, result);
		assertNotSame(originalRenderer, result);
	}

	@Test
	public void tryLookupTFPartRendererNonTFPart() {
		Identifier location = Identifier.withDefaultNamespace("test");
		EntityRenderer<?, ?> partRenderer = mock(EntityRenderer.class);
		putRenderer(location, partRenderer);

		EntityRenderer<?, ?> originalRenderer = mock(EntityRenderer.class);

		EntityRenderer<?, ?> result = MultipartEntityClientUtil.tryLookupTFPartRenderer(originalRenderer, mock(Entity.class));

		assertNotNull(result);
		assertNotSame(partRenderer, result);
		assertSame(originalRenderer, result);
	}

	@SuppressWarnings("unchecked")
	private static void putRenderer(Identifier location, EntityRenderer<?, ?> renderer) {
		try {
			var field = Class.forName("twilightforest.client.BakedMultiPartRenderers").getDeclaredField("renderers");
			field.setAccessible(true);
			var renderers = (java.util.Map<Identifier, Object>) field.get(null);
			renderers.clear();

			var cachedSupplierClass = Class.forName("twilightforest.client.BakedMultiPartRenderers$CachedSupplier");
			var ctor = cachedSupplierClass.getDeclaredConstructor(java.util.function.Supplier.class);
			ctor.setAccessible(true);
			Object supplier = ctor.newInstance((java.util.function.Supplier<EntityRenderer<?, ?>>) () -> renderer);
			renderers.put(location, supplier);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to inject multipart renderer for tests.", e);
		}
	}

}
