package twilightforest.client.model;

import java.util.IdentityHashMap;
import java.util.Map;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public final class ThreeLayerDeviceEmissiveModelPlugin implements ModelLoadingPlugin {
	private static final Identifier MODEL_ID = Identifier.fromNamespaceAndPath("twilightforest", "block/util/three_layer_device_active");
	private static final Map<String, Integer> EMISSIVE_SLOTS = Map.of(
		"top2", 10,
		"side2", 15,
		"top3", 7,
		"side3", 10
	);

	public static void register() {
		ModelLoadingPlugin.register(new ThreeLayerDeviceEmissiveModelPlugin());
	}

	@Override
	public void initialize(Context pluginContext) {
		pluginContext.modifyModelOnLoad().register(ModelModifier.DEFAULT_PHASE, (model, context) -> {
			if (!MODEL_ID.equals(context.id())) {
				return model;
			}
			if (!(model instanceof UnbakedModel unbakedModel)) {
				return model;
			}
			if (unbakedModel.geometry() instanceof EmissiveGeometry) {
				return model;
			}
			return new EmissiveUnbakedModel(unbakedModel, new EmissiveGeometry(unbakedModel.geometry(), EMISSIVE_SLOTS));
		});
	}

	private record EmissiveUnbakedModel(UnbakedModel delegate, UnbakedGeometry geometry) implements UnbakedModel {
		@Override
		public Boolean ambientOcclusion() {
			return this.delegate.ambientOcclusion();
		}

		@Override
		public GuiLight guiLight() {
			return this.delegate.guiLight();
		}

		@Override
		public net.minecraft.client.resources.model.cuboid.ItemTransforms transforms() {
			return this.delegate.transforms();
		}

		@Override
		public TextureSlots.Data textureSlots() {
			return this.delegate.textureSlots();
		}

		@Override
		public Identifier parent() {
			return this.delegate.parent();
		}
	}

	private static final class EmissiveGeometry implements UnbakedGeometry {
		private final UnbakedGeometry delegate;
		private final Map<String, Integer> emissiveSlots;

		private EmissiveGeometry(UnbakedGeometry delegate, Map<String, Integer> emissiveSlots) {
			this.delegate = delegate;
			this.emissiveSlots = emissiveSlots;
		}

		@Override
		public QuadCollection bake(TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName debugName) {
			QuadCollection base = delegate.bake(textureSlots, modelBaker, modelState, debugName);
			if (base == QuadCollection.EMPTY) {
				return base;
			}

			Map<TextureAtlasSprite, Integer> emissiveSprites = new IdentityHashMap<>();
			for (Map.Entry<String, Integer> entry : emissiveSlots.entrySet()) {
				TextureAtlasSprite sprite = modelBaker.materials().resolveSlot(textureSlots, entry.getKey(), debugName).sprite();
				if (sprite != null) {
					emissiveSprites.put(sprite, entry.getValue());
				}
			}

			if (emissiveSprites.isEmpty()) {
				return base;
			}

			return applyEmissive(base, emissiveSprites);
		}
	}

	private static QuadCollection applyEmissive(QuadCollection base, Map<TextureAtlasSprite, Integer> emissiveSprites) {
		IdentityHashMap<BakedQuad, BakedQuad> remapped = new IdentityHashMap<>();
		QuadCollection.Builder builder = new QuadCollection.Builder();

		for (BakedQuad quad : base.getQuads(null)) {
			builder.addUnculledFace(remapQuad(quad, emissiveSprites, remapped));
		}

		for (Direction direction : Direction.values()) {
			for (BakedQuad quad : base.getQuads(direction)) {
				builder.addCulledFace(direction, remapQuad(quad, emissiveSprites, remapped));
			}
		}

		return builder.build();
	}

	private static BakedQuad remapQuad(BakedQuad quad, Map<TextureAtlasSprite, Integer> emissiveSprites, IdentityHashMap<BakedQuad, BakedQuad> remapped) {
		return remapped.computeIfAbsent(quad, existing -> {
			Integer emissive = emissiveSprites.get(existing.materialInfo().sprite());
			if (emissive == null) {
				return existing;
			}

			int currentEmission = existing.materialInfo().lightEmission();
			int emission = Math.max(currentEmission, Math.min(15, Math.max(0, emissive)));
			if (emission == currentEmission) {
				return existing;
			}

			BakedQuad.MaterialInfo materialInfo = new BakedQuad.MaterialInfo(
				existing.materialInfo().sprite(),
				existing.materialInfo().layer(),
				existing.materialInfo().itemRenderType(),
				existing.materialInfo().tintIndex(),
				existing.materialInfo().shade(),
				emission
			);

			return new BakedQuad(
				existing.position0(),
				existing.position1(),
				existing.position2(),
				existing.position3(),
				existing.packedUV0(),
				existing.packedUV1(),
				existing.packedUV2(),
				existing.packedUV3(),
				existing.direction(),
				materialInfo
			);
		});
	}
}
