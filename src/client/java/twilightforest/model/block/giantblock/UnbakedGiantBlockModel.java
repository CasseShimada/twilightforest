package twilightforest.client.model.block.giantblock;

import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import twilightforest.client.model.block.ModelBakingUtil;

public class UnbakedGiantBlockModel implements UnbakedModel {
	private final UnbakedModel baseModel;

	public UnbakedGiantBlockModel(UnbakedModel baseModel) {
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
		Material.Baked[] sprites = new Material.Baked[]{
			ModelBakingUtil.resolveMaterial(baker, slots, "down", resolvedModel),
			ModelBakingUtil.resolveMaterial(baker, slots, "up", resolvedModel),
			ModelBakingUtil.resolveMaterial(baker, slots, "north", resolvedModel),
			ModelBakingUtil.resolveMaterial(baker, slots, "south", resolvedModel),
			ModelBakingUtil.resolveMaterial(baker, slots, "west", resolvedModel),
			ModelBakingUtil.resolveMaterial(baker, slots, "east", resolvedModel)
		};

		Material.Baked particle = slots.getMaterial("particle") != null
			? ModelBakingUtil.resolveMaterial(baker, slots, "particle", resolvedModel)
			: sprites[0];

		return new GiantBlockStateModel(sprites, particle, resolvedModel.getTopAmbientOcclusion(), baker);
	}
}
