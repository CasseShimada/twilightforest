package twilightforest.client.model.block.patch;

import net.minecraft.client.renderer.block.model.BlockModel;
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
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public class UnbakedPatchModel implements UnbakedModel {
	private final BlockModel baseModel;
	private final boolean shaggify;

	public UnbakedPatchModel(BlockModel baseModel, boolean shaggify) {
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
			TextureAtlasSprite texture = resolveSprite(baker, textureSlots, "texture", name);
			TextureAtlasSprite particle = resolveSprite(baker, textureSlots, "particle", name);
			PatchModel model = new PatchModel(texture, shaggify, particle, baseModel.ambientOcclusion() != null && baseModel.ambientOcclusion(), baker.parts());
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
		TextureAtlasSprite texture = resolveSprite(baker, slots, "texture", resolvedModel);
		TextureAtlasSprite particle = resolveSprite(baker, slots, "particle", resolvedModel);
		return new PatchModel(texture, shaggify, particle, resolvedModel.getTopAmbientOcclusion(), baker.parts());
	}

	private static TextureAtlasSprite resolveSprite(ModelBaker baker, TextureSlots slots, String key, ModelDebugName name) {
		return baker.sprites().resolveSlot(slots, key, name);
	}
}
