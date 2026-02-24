package twilightforest.client.model.block.aurorablock;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.util.RandomSource;
import twilightforest.util.SimplexNoiseHelper;
import twilightforest.client.model.block.BlockModelContext;

import java.util.List;

public final class NoiseVaryingBlockStateModel implements BlockStateModel {
	private final BlockModelPart[] variants;
	private final TextureAtlasSprite particle;

	public NoiseVaryingBlockStateModel(BlockModelPart[] variants, TextureAtlasSprite particle) {
		this.variants = variants;
		this.particle = particle;
	}

	@Override
	public void collectParts(RandomSource random, List<BlockModelPart> output) {
		output.add(new NoiseVaryingPart());
	}

	@Override
	public TextureAtlasSprite particleIcon() {
		return particle;
	}

	private final class NoiseVaryingPart implements BlockModelPart {
		@Override
		public List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(net.minecraft.core.Direction direction) {
			BlockModelContext.Context ctx = BlockModelContext.get();
			int variant = ctx != null ? SimplexNoiseHelper.calcVariant(ctx.pos(), variants.length) : 0;
			return variants[variant].getQuads(direction);
		}

		@Override
		public boolean useAmbientOcclusion() {
			return variants[0].useAmbientOcclusion();
		}

		@Override
		public TextureAtlasSprite particleIcon() {
			return particle;
		}
	}

	public record Unbaked(Variant variant) implements CustomUnbakedBlockStateModel {
		public static final MapCodec<Unbaked> MAP_CODEC = Variant.MAP_CODEC.xmap(Unbaked::new, Unbaked::variant);

		@Override
		public BlockStateModel bake(ModelBaker baker) {
			ResolvedModel resolved = baker.getModel(variant.modelLocation());
			if (resolved.wrapped() instanceof UnbakedNoiseVaryingModel unbaked) {
				return unbaked.bakeToBlockStateModel(baker, resolved, variant.modelState().asModelState());
			}

			return new SingleVariant(variant.bake(baker));
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			variant.resolveDependencies(resolver);
		}

		@Override
		public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
			return MAP_CODEC;
		}
	}
}
