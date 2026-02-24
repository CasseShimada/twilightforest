package twilightforest.client.model;

import java.util.IdentityHashMap;
import java.util.Map;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.client.resources.model.UnbakedGeometry;
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
			if (!(model instanceof BlockModel blockModel)) {
				return model;
			}
			if (blockModel.geometry() instanceof EmissiveGeometry) {
				return model;
			}
			return new BlockModel(
					new EmissiveGeometry(blockModel.geometry(), EMISSIVE_SLOTS),
					blockModel.guiLight(),
					blockModel.ambientOcclusion(),
					blockModel.transforms(),
					blockModel.textureSlots(),
					blockModel.parent()
			);
		});
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

			SpriteGetter sprites = modelBaker.sprites();
			Map<TextureAtlasSprite, Integer> emissiveSprites = new IdentityHashMap<>();
			for (Map.Entry<String, Integer> entry : emissiveSlots.entrySet()) {
				TextureAtlasSprite sprite = sprites.resolveSlot(textureSlots, entry.getKey(), debugName);
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
			Integer emissive = emissiveSprites.get(existing.sprite());
			if (emissive == null) {
				return existing;
			}

			int emission = Math.max(existing.lightEmission(), Math.min(15, Math.max(0, emissive)));
			if (emission == existing.lightEmission()) {
				return existing;
			}

			return new BakedQuad(
				existing.position0(),
				existing.position1(),
				existing.position2(),
				existing.position3(),
				existing.packedUV0(),
				existing.packedUV1(),
				existing.packedUV2(),
				existing.packedUV3(),
				existing.tintIndex(),
				existing.direction(),
				existing.sprite(),
				existing.shade(),
				emission
			);
		});
	}
}
