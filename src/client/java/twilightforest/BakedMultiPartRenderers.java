package twilightforest.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.Identifier;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.HydraHeadModel;
import twilightforest.client.model.entity.HydraNeckModel;
import twilightforest.client.model.entity.NagaModel;
import twilightforest.client.renderer.entity.*;
import twilightforest.entity.TFPart;
import twilightforest.entity.boss.HydraHead;
import twilightforest.entity.boss.HydraNeck;
import twilightforest.entity.boss.NagaSegment;
import twilightforest.entity.boss.SnowQueenIceShield;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class BakedMultiPartRenderers {
	private static final Map<Identifier, CachedSupplier<EntityRenderer<?, ?>>> renderers = new HashMap<>();
	private static boolean initialized = false;

	public static void bakeMultiPartRenderers(EntityRendererProvider.Context context) {
		renderers.put(TFPart.RENDERER, new CachedSupplier<>(() -> new NoopRenderer<>(context)));
		renderers.put(HydraHead.RENDERER, new CachedSupplier<>(() -> new HydraHeadRenderer(context, new HydraHeadModel(context.bakeLayer(TFModelLayers.HYDRA_HEAD)))));
		renderers.put(HydraNeck.RENDERER, new CachedSupplier<>(() -> new HydraNeckRenderer(context, new HydraNeckModel(context.bakeLayer(TFModelLayers.HYDRA_NECK)))));
		renderers.put(SnowQueenIceShield.RENDERER, new CachedSupplier<>(() -> new SnowQueenIceShieldRenderer(context)));
		renderers.put(NagaSegment.RENDERER, new CachedSupplier<>(() -> new NagaSegmentRenderer(context, new NagaModel<>(context.bakeLayer(TFModelLayers.NAGA_BODY)))));
	}

	public static void ensureInitialized(EntityRendererProvider.Context context) {
		if (!initialized) {
			initialized = true;
			bakeMultiPartRenderers(context);
		}
	}

	public static EntityRenderer<?, ?> lookup(Identifier location) {
		CachedSupplier<EntityRenderer<?, ?>> supplier = renderers.get(location);
		return supplier == null ? null : supplier.get();
	}

	private static final class CachedSupplier<T> implements Supplier<T> {
		private final Supplier<T> supplier;
		private T value;
		private boolean loaded;

		private CachedSupplier(Supplier<T> supplier) {
			this.supplier = Objects.requireNonNull(supplier, "supplier");
		}

		@Override
		public T get() {
			if (!loaded) {
				value = supplier.get();
				loaded = true;
			}
			return value;
		}
	}
}
