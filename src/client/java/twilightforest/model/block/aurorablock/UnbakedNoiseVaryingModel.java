package twilightforest.client.model.block.aurorablock;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;

import java.util.Arrays;

public class UnbakedNoiseVaryingModel implements UnbakedModel, ResolvableModel {
	private final BlockModel baseModel;
	private final Identifier[] variants;

	public UnbakedNoiseVaryingModel(BlockModel baseModel, String[] variants) {
		this.baseModel = baseModel;
		this.variants = Arrays.stream(variants).map(Identifier::parse).toArray(Identifier[]::new);
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
			if (variants.length == 0) {
				return baseModel.geometry().bake(textureSlots, baker, modelState, name);
			}
			ResolvedModel resolved = baker.getModel(variants[0]);
			TextureSlots slots = resolved.getTopTextureSlots();
			return resolved.bakeTopGeometry(slots, baker, modelState);
		};
	}

	@Override
	public Identifier parent() {
		return baseModel.parent();
	}

	@Override
	public void resolveDependencies(ResolvableModel.Resolver resolver) {
		for (Identifier variant : variants) {
			resolver.markDependency(variant);
		}
	}

	public NoiseVaryingBlockStateModel bakeToBlockStateModel(ModelBaker baker, ResolvedModel resolvedModel, ModelState state) {
		BlockModelPart[] parts = new BlockModelPart[this.variants.length];
		for (int i = 0; i < parts.length; i++) {
			parts[i] = SimpleModelWrapper.bake(baker, variants[i], state);
		}

		TextureAtlasSprite particle = parts.length > 0
			? parts[0].particleIcon()
			: resolvedModel.resolveParticleSprite(resolvedModel.getTopTextureSlots(), baker);

		return new NoiseVaryingBlockStateModel(parts, particle);
	}
}
