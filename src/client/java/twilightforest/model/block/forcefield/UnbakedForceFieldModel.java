package twilightforest.client.model.block.forcefield;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnbakedForceFieldModel implements UnbakedModel {
	private final BlockModel baseModel;
	private final Map<BlockElement, ForceFieldModelLoader.Condition> elementsAndConditions;

	public UnbakedForceFieldModel(BlockModel baseModel, Map<BlockElement, ForceFieldModelLoader.Condition> elementsAndConditions) {
		this.baseModel = baseModel;
		this.elementsAndConditions = elementsAndConditions;
	}

	@Override
	public Boolean ambientOcclusion() {
		return baseModel.ambientOcclusion();
	}

	@Override
	public GuiLight guiLight() {
		return baseModel.guiLight();
	}

	@Override
	public ItemTransforms transforms() {
		return baseModel.transforms();
	}

	@Override
	public TextureSlots.Data textureSlots() {
		return baseModel.textureSlots();
	}

	@Override
	public UnbakedGeometry geometry() {
		return (textureSlots, baker, modelState, name) -> {
			QuadCollection.Builder builder = new QuadCollection.Builder();
			for (BlockElement element : elementsAndConditions.keySet()) {
				for (var faceEntry : element.faces().entrySet()) {
					Direction side = faceEntry.getKey();
					BlockElementFace face = faceEntry.getValue();
					if (face == null) continue;
					TextureAtlasSprite sprite = resolveSprite(baker, textureSlots, stripReference(face.texture()), name);
					BakedQuad quad = FaceBakery.bakeQuad(
						baker.parts(),
						element.from(),
						element.to(),
						face,
						sprite,
						side,
						modelState,
						element.rotation(),
						element.shade(),
						element.lightEmission()
					);
					if (face.cullForDirection() == null) {
						builder.addUnculledFace(quad);
					} else {
						builder.addCulledFace(face.cullForDirection(), quad);
					}
				}
			}
			return builder.build();
		};
	}

	@Override
	public Identifier parent() {
		return baseModel.parent();
	}

	public ForceFieldBlockStateModel bakeToBlockStateModel(ModelBaker baker, ResolvedModel resolvedModel, ModelState state) {
		TextureSlots slots = resolvedModel.getTopTextureSlots();
		List<ForceFieldBlockStateModel.Part> bakedParts = new ArrayList<>();

		for (Map.Entry<BlockElement, ForceFieldModelLoader.Condition> entry : this.elementsAndConditions.entrySet()) {
			BlockElement element = entry.getKey();
			for (var faceEntry : element.faces().entrySet()) {
				Direction side = faceEntry.getKey();
				BlockElementFace face = faceEntry.getValue();
				if (face == null) continue;
				TextureAtlasSprite sprite = resolveSprite(baker, slots, stripReference(face.texture()), resolvedModel);
				BakedQuad quad = FaceBakery.bakeQuad(
					baker.parts(),
					element.from(),
					element.to(),
					face,
					sprite,
					side,
					state,
					element.rotation(),
					element.shade(),
					element.lightEmission()
				);
				bakedParts.add(new ForceFieldBlockStateModel.Part(side, face.cullForDirection(), quad, entry.getValue()));
			}
		}

		TextureAtlasSprite particle = slots.getMaterial("particle") != null
			? resolveSprite(baker, slots, "particle", resolvedModel)
			: resolveSprite(baker, slots, stripReference(firstTextureSlot()), resolvedModel);

		return new ForceFieldBlockStateModel(bakedParts, particle, resolvedModel.getTopAmbientOcclusion());
	}

	private static String stripReference(String texture) {
		return texture.startsWith("#") ? texture.substring(1) : texture;
	}

	private String firstTextureSlot() {
		for (String key : baseModel.textureSlots().values().keySet()) {
			return key;
		}
		return "particle";
	}

	private static TextureAtlasSprite resolveSprite(ModelBaker baker, TextureSlots slots, String key, ModelDebugName name) {
		return baker.sprites().resolveSlot(slots, key, name);
	}
}
