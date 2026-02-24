package twilightforest.client.model.block.giantblock;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;

public class UnbakedGiantBlockModel implements UnbakedModel {
	private final BlockModel baseModel;

	public UnbakedGiantBlockModel(BlockModel baseModel) {
		this.baseModel = baseModel;
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
		return baseModel.geometry();
	}

	@Override
	public Identifier parent() {
		return baseModel.parent();
	}

	public GiantBlockStateModel bakeToBlockStateModel(ModelBaker baker, ResolvedModel resolvedModel, ModelState state) {
		TextureSlots slots = resolvedModel.getTopTextureSlots();
		TextureAtlasSprite[] sprites = new TextureAtlasSprite[]{
			resolveSprite(baker, slots, "down", resolvedModel),
			resolveSprite(baker, slots, "up", resolvedModel),
			resolveSprite(baker, slots, "north", resolvedModel),
			resolveSprite(baker, slots, "south", resolvedModel),
			resolveSprite(baker, slots, "west", resolvedModel),
			resolveSprite(baker, slots, "east", resolvedModel)
		};

		TextureAtlasSprite particle = slots.getMaterial("particle") != null
			? resolveSprite(baker, slots, "particle", resolvedModel)
			: sprites[0];

		return new GiantBlockStateModel(sprites, particle, resolvedModel.getTopAmbientOcclusion(), baker.parts());
	}

	private static TextureAtlasSprite resolveSprite(ModelBaker baker, TextureSlots slots, String key, ResolvedModel resolvedModel) {
		return baker.sprites().resolveSlot(slots, key, resolvedModel);
	}
}
