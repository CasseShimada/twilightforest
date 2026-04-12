package twilightforest.client.model.block.forcefield;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import twilightforest.client.model.block.ModelBakingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnbakedForceFieldModel implements UnbakedModel {
	private final UnbakedModel baseModel;
	private final Map<CuboidModelElement, ForceFieldModelLoader.Condition> elementsAndConditions;

	public UnbakedForceFieldModel(UnbakedModel baseModel, Map<CuboidModelElement, ForceFieldModelLoader.Condition> elementsAndConditions) {
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
			for (CuboidModelElement element : elementsAndConditions.keySet()) {
				for (var faceEntry : element.faces().entrySet()) {
					Direction side = faceEntry.getKey();
					CuboidFace face = faceEntry.getValue();
					if (face == null) continue;
					Material.Baked sprite = ModelBakingUtil.resolveMaterial(baker, textureSlots, stripReference(face.texture()), name);
					BakedQuad quad = FaceBakery.bakeQuad(
						baker,
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
		int materialFlags = 0;

		for (Map.Entry<CuboidModelElement, ForceFieldModelLoader.Condition> entry : this.elementsAndConditions.entrySet()) {
			CuboidModelElement element = entry.getKey();
			for (var faceEntry : element.faces().entrySet()) {
				Direction side = faceEntry.getKey();
				CuboidFace face = faceEntry.getValue();
				if (face == null) continue;
				Material.Baked sprite = ModelBakingUtil.resolveMaterial(baker, slots, stripReference(face.texture()), resolvedModel);
				BakedQuad quad = FaceBakery.bakeQuad(
					baker,
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
				materialFlags |= quad.materialInfo().flags();
				bakedParts.add(new ForceFieldBlockStateModel.Part(side, face.cullForDirection(), quad, entry.getValue()));
			}
		}

		Material.Baked particle = slots.getMaterial("particle") != null
			? ModelBakingUtil.resolveMaterial(baker, slots, "particle", resolvedModel)
			: ModelBakingUtil.resolveMaterial(baker, slots, stripReference(firstTextureSlot()), resolvedModel);

		return new ForceFieldBlockStateModel(bakedParts, particle, resolvedModel.getTopAmbientOcclusion(), materialFlags);
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
}
