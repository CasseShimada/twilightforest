package twilightforest.client.model.block.patch;

import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import twilightforest.client.model.block.ModelBakingUtil;

public class UnbakedPatchModel implements UnbakedModel {
	private final UnbakedModel baseModel;
	private final boolean shaggify;

	public UnbakedPatchModel(UnbakedModel baseModel, boolean shaggify) {
		this.baseModel = baseModel;
		this.shaggify = shaggify;
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
			Material.Baked texture = ModelBakingUtil.resolveMaterial(baker, textureSlots, "texture", name);
			Material.Baked particle = ModelBakingUtil.resolveMaterial(baker, textureSlots, "particle", name);
			PatchModel model = new PatchModel(texture, shaggify, particle, baseModel.ambientOcclusion() != null && baseModel.ambientOcclusion(), baker);
			QuadCollection quads = model.buildQuads(false, false, false, false, RandomSource.create(0));
			return quads == null ? QuadCollection.EMPTY : quads;
		};
	}

	@Override
	public Identifier parent() {
		return baseModel.parent();
	}

	public PatchModel bakeToBlockStateModel(ModelBaker baker, ResolvedModel resolvedModel) {
		TextureSlots slots = resolvedModel.getTopTextureSlots();
		Material.Baked texture = ModelBakingUtil.resolveMaterial(baker, slots, "texture", resolvedModel);
		Material.Baked particle = ModelBakingUtil.resolveMaterial(baker, slots, "particle", resolvedModel);
		return new PatchModel(texture, shaggify, particle, resolvedModel.getTopAmbientOcclusion(), baker);
	}
}
