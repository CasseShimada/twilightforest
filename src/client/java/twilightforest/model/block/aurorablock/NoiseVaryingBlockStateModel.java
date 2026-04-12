package twilightforest.client.model.block.aurorablock;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;
import twilightforest.util.SimplexNoiseHelper;
import twilightforest.client.model.block.BlockModelContext;
import twilightforest.client.model.block.ModelBakingUtil;

import java.util.List;

public final class NoiseVaryingBlockStateModel implements BlockStateModel {
	private final BlockStateModelPart[] variants;
	private final Material.Baked particle;
	private final int materialFlags;

	public NoiseVaryingBlockStateModel(BlockStateModelPart[] variants, Material.Baked particle) {
		this.variants = variants;
		this.particle = particle;
		this.materialFlags = ModelBakingUtil.materialFlags(variants);
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
		output.add(new NoiseVaryingPart());
	}

	@Override
	public Material.Baked particleMaterial() {
		return particle;
	}

	@Override
	public int materialFlags() {
		return materialFlags;
	}

	private final class NoiseVaryingPart implements BlockStateModelPart {
		@Override
		public List<net.minecraft.client.resources.model.geometry.BakedQuad> getQuads(net.minecraft.core.Direction direction) {
			BlockModelContext.Context ctx = BlockModelContext.get();
			int variant = ctx != null ? SimplexNoiseHelper.calcVariant(ctx.pos(), variants.length) : 0;
			return variants[variant].getQuads(direction);
		}

		@Override
		public boolean useAmbientOcclusion() {
			return variants[0].useAmbientOcclusion();
		}

		@Override
		public Material.Baked particleMaterial() {
			return particle;
		}

		@Override
		public int materialFlags() {
			return NoiseVaryingBlockStateModel.this.materialFlags;
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
